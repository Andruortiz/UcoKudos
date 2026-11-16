# UCO Kudos: API

API de **UCO Kudos**, una plataforma institucional de reconocimientos: un estudiante, un docente o un directivo le envía a otra persona de la comunidad un mensaje de aprecio (un *Kudo*) y el destinatario se entera por correo, WhatsApp y una notificación en tiempo real.

- **Sitio publicado:** https://kudos.ucokudos.qd.je
- **Aplicación web:** repositorio [front-kudos](https://github.com/Andruortiz/front-kudos)
- **Calidad:** [SonarCloud](https://sonarcloud.io/summary/new_code?id=Andruortiz_UcoKudos)

## Qué hace

| Funcionalidad | Estado |
|---|---|
| Publicar un Kudo con idempotencia, límite diario y reglas de quién puede enviar a quién | Construida |
| Adjuntos (imágenes y PDF) guardados en Azure Blob Storage, con descarga solo para el autor y el destinatario | Construida |
| Avisos por correo, WhatsApp y *push* en tiempo real (SSE), con bandeja de salida, reintentos y estado por canal | Construida |
| Inicio de sesión con Microsoft Entra ID y alta automática mediante una invitación de Microsoft Graph | Construida |
| Verificación del celular con un código por SMS (Vonage); sin celular verificado no se pueden enviar Kudos | Construida |
| Auditoría de las acciones principales | Construida (sin consulta para el administrador) |
| Réplicas y calificaciones, aprobaciones jerárquicas, administración de usuarios, observabilidad | **Sin construir** |

El estado completo frente a la guía del trabajo está en [Documentacion](Documentacion/).

## Arquitectura

La API es **un solo servicio** con arquitectura hexagonal (puertos y adaptadores). La regla de dependencia apunta hacia adentro y la verifica ArchUnit en cada ejecución de las pruebas.

### Contexto del sistema

![Diagrama de contexto de UCO Kudos](Documentacion/Imagenes/c4-nivel-1-contexto.svg)

### Contenedores

![Diagrama de contenedores de UCO Kudos](Documentacion/Imagenes/c4-nivel-2-contenedores.svg)

### Componentes de la API

![Diagrama de componentes de la API](Documentacion/Imagenes/diagrama-de-componentes.svg)

### Paquetes

![Diagrama de paquetes del back-end](Documentacion/Imagenes/diagrama-de-paquetes.svg)

Los diagramas también están como archivos `.drawio` editables en la misma carpeta (se abren con [diagrams.net](https://app.diagrams.net)).

## Secuencias

### 1. Publicar un Kudo

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant W as Aplicación web
    participant GW as Gateway y WAF
    participant A as KudoController
    participant UC as PublishKudoUseCase
    participant R as Reglas de dominio
    participant AP as AttachmentProcessor
    participant DB as PostgreSQL
    participant B as Blob Storage

    U->>W: Elige destinatario, categoría, mensaje y adjuntos
    W->>W: Valida adjuntos, máximo 5, 10 MB, imagen o PDF
    W->>GW: POST /api/v1/kudos multipart con Idempotency-Key y Bearer
    GW->>A: Reenvía la petición si pasa las reglas del WAF
    A->>A: Valida el JWT de Entra y toma el autor del token
    A->>UC: execute con PublishKudoRequest
    UC->>DB: Busca la clave de idempotencia de la última hora
    alt La clave existe con la misma petición
        UC-->>A: Devuelve el Kudo original
    else La clave existe con otro contenido
        UC-->>A: Error 409 kudo.idempotency.conflict
    else Clave nueva
        UC->>DB: Carga autor, destinatario y categoría
        UC->>R: Valida usuarios activos, autor con correo y celular verificados, contenido, no autoenvío y matriz de roles
        UC->>R: Valida categoría activa, límite diario de 50 y adjuntos
        UC->>AP: process de los adjuntos
        Note over AP: Rechaza el archivo cuyos bytes no coinciden con su tipo
        UC->>B: Guarda cada adjunto con una URL firmada
        UC->>DB: Transacción única, guarda Kudo, 3 notificaciones PENDING, auditoría KUDO_PUBLISHED y clave de idempotencia
        UC-->>A: PublishKudoResponse
    end
    A-->>GW: 201 con el Kudo y el estado de cada canal
    GW-->>W: 201 con el Kudo y el estado de cada canal
    W-->>U: Muestra Kudo publicado
```

### 2. Despacho de notificaciones

```mermaid
sequenceDiagram
    autonumber
    participant S as NotificationDispatchScheduler
    participant UC as DispatchNotificationsUseCase
    participant DB as PostgreSQL
    participant N as NotificationDomain
    participant E as Canal de envío
    participant P as Proveedor externo

    loop Cada 30 segundos cuando kudos.dispatch.enabled es true
        S->>UC: execute
        UC->>DB: findDue de los canales con envío, lote de 50
        loop Cada notificación
            UC->>DB: Carga Kudo, destinatario, autor y categoría
            UC->>E: send con el aviso del Kudo
            alt Correo
                E->>P: SMTP al correo del destinatario
            else WhatsApp
                E->>P: HTTPS con la plantilla kudos_envios_url: tres variables y el botón Ver Kudo
            else Push
                E->>P: Evento SSE si el destinatario está conectado
            end
            alt Enviado
                UC->>N: markSent
                UC->>DB: Guarda SENT y audita NOTIFICATION_SENT
            else Sin conexión, solo push
                UC->>N: markSkipped
                UC->>DB: Guarda SKIPPED
            else Error del proveedor
                UC->>N: markFailed con espera creciente
                alt Quedan intentos
                    UC->>DB: Guarda PENDING con nueva fecha de intento
                else Se agotaron 5 intentos
                    UC->>DB: Guarda FAILED y audita NOTIFICATION_FAILED
                end
            end
        end
    end
```

### 3. Push en tiempo real

```mermaid
sequenceDiagram
    autonumber
    actor D as Destinatario
    participant W as Aplicación web
    participant PC as PushController
    participant PS as PushNotificationSender
    participant UC as DispatchNotificationsUseCase

    D->>W: Inicia sesión y abre la aplicación
    W->>PC: GET /api/v1/notifications/stream con Bearer y Accept text/event-stream
    PC->>PS: subscribe del usuario
    PS-->>W: Abre el flujo SSE y envía el comentario connected
    loop Mientras la conexión siga abierta
        PS-->>W: Comentario keep-alive cada 25 segundos
    end
    Note over UC,PS: Otro usuario publica un Kudo para el destinatario
    UC->>PS: send con kudoId, autor, categoría y enlace
    PS-->>W: Evento kudo con los datos del aviso
    W-->>D: Muestra la notificación con el enlace al Kudo
    Note over W: Si la conexión se corta, la aplicación vuelve a conectarse a los 5 segundos
```

### 4. Acceso, inicio de sesión y registro

```mermaid
sequenceDiagram
    autonumber
    actor P as Persona nueva
    participant W as Aplicación web
    participant API as API UCO Kudos
    participant G as Microsoft Graph
    participant E as Microsoft Entra ID
    participant DB as PostgreSQL

    P->>W: Abre Pedir acceso y escribe su correo institucional
    W->>API: POST /api/v1/access-requests sin sesión, con límite de uso
    API->>API: Valida que el dominio del correo sea institucional
    API->>DB: Busca el correo
    alt Ya tiene cuenta
        API-->>W: ALREADY_HAS_ACCESS
    else No tiene cuenta
        API->>E: Pide un token de aplicación con client credentials
        API->>G: POST /invitations con el correo
        G-->>P: Microsoft envía la invitación al buzón institucional
        API-->>W: INVITED
    end
    P->>E: Acepta la invitación y pulsa Entrar con Microsoft
    W->>E: Inicio de sesión con OpenID Connect y PKCE, ámbito access_as_user
    E-->>W: Token de acceso
    W->>API: Petición con Authorization Bearer
    API->>E: Valida emisor, destinatario, firma y expiración
    API->>DB: Busca el usuario por el identificador de directorio o enlaza por correo
    alt El usuario no existe todavía
        API-->>W: Autoridad UNREGISTERED, solo puede registrarse
        W->>API: POST /api/v1/registrations con el nombre
        API->>DB: Crea el usuario ACTIVE con correo verificado y celular sin verificar
        API-->>W: Registro APPROVED
    else El usuario existe
        API-->>W: Acceso a la API con su rol de la base de datos
    end
```

### 5. Verificación del celular

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant W as Aplicación web
    participant API as ProfileController
    participant UC as Casos de uso del celular
    participant DB as PostgreSQL
    participant SMS as Vonage SMS

    U->>W: Escribe su número en formato internacional
    W->>API: PUT /api/v1/me/phone
    API->>UC: UpdatePhoneUseCase valida el formato, guarda y audita PHONE_UPDATED
    U->>W: Pide el código
    W->>API: POST /api/v1/me/phone/verification
    API->>UC: RequestPhoneVerificationUseCase
    UC->>DB: Lee los envíos de la última hora
    alt Menos de 1 minuto desde el último envío
        UC-->>W: Error phone.code.too.soon
    else Ya hubo 5 envíos en la hora
        UC-->>W: Error phone.code.too.many
    else Puede enviar
        UC->>SMS: Envía el código de 6 dígitos
        UC->>DB: Guarda el hash del código, caducidad y contadores, audita PHONE_VERIFICATION_SENT
    end
    U->>W: Escribe el código recibido
    W->>API: POST /api/v1/me/phone/verification/confirm
    API->>UC: ConfirmPhoneVerificationUseCase
    UC->>DB: Lee la verificación
    alt Código vencido o 5 intentos fallidos
        UC-->>W: Error phone.code.expired o phone.code.too.many
    else Código distinto
        UC->>DB: Suma un intento
        UC-->>W: Error phone.code.invalid
    else Código correcto
        UC->>DB: phone_verified es true, borra la verificación y audita PHONE_VERIFIED
        UC-->>W: Perfil con el celular verificado
    end
```

## Tecnologías

| Pieza | Tecnología |
|---|---|
| Lenguaje y marco | Java 27, Spring Boot 4.1.2, Spring Web MVC, Spring Security (servidor de recursos OAuth2 con JWT) |
| Persistencia | PostgreSQL, Spring Data JPA con Hibernate, migraciones con Flyway, mapeadores con MapStruct |
| Integraciones | Microsoft Entra ID y Graph, SMTP, WhatsApp Cloud API (Meta), Vonage SMS, Azure Blob Storage |
| Pruebas | JUnit, Mockito, Spring Boot Test, ArchUnit, H2 |
| Entrega | Docker, GitHub Actions, SonarCloud, Trivy, Azure Container Apps |

## Ejecutar en local

Requisitos: JDK 27, Docker y Git. No hace falta instalar Maven: el repositorio trae `./mvnw`.

**1. Base de datos y correo de desarrollo**

```bash
docker run -d --name kudos-db -e POSTGRES_DB=kudos -e POSTGRES_USER=kudos -e POSTGRES_PASSWORD=kudos -p 5432:5432 postgres:17
```

```bash
docker run -d --name mailpit -p 1025:1025 -p 8025:8025 axllent/mailpit
```

Mailpit recibe los correos que envía la API y los muestra en http://localhost:8025, sin enviar nada real.

**2. La API**, con la autenticación de desarrollo (identifica al usuario por la cabecera `X-Dev-User`, sin iniciar sesión con Microsoft):

```bash
KUDOS_DEV_AUTH_ENABLED=true ./mvnw spring-boot:run
```

En PowerShell: `$env:KUDOS_DEV_AUTH_ENABLED = "true"; ./mvnw spring-boot:run`

Comprueba que responde en http://localhost:8080/actuator/health. El perfil `local` aplica las migraciones y además crea las categorías y tres usuarios de prueba: Ana (estudiante), Luis (docente) y Carlos (estudiante). Los adjuntos se guardan en memoria.

El manual completo, con las variables de entorno, la configuración de Microsoft Entra y la validación del flujo Publicar Kudo, está en [manual-de-puesta-a-punto.md](Documentacion/manual-de-puesta-a-punto.md).

## Pruebas

```bash
./mvnw test
```

Son 218 pruebas: unitarias, de integración (con H2) y 11 reglas de arquitectura con ArchUnit. ArchUnit 1.4.1 no lee clases de Java 27, así que el build compila además una copia con nivel 25 que solo usan esas reglas; el código que se despliega sigue compilado con Java 27.

La validación de las migraciones sobre un PostgreSQL real existe, pero solo corre si se define la variable `FLYWAY_CHECK_DB_URL` y no está en el CI:

```bash
FLYWAY_CHECK_DB_URL=jdbc:postgresql://localhost:5432/kudos_check ./mvnw test -Dtest=FlywayMigrationsTest
```

## Configuración

Cada propiedad de `application.properties` se puede sobrescribir con una variable de entorno. **Los secretos nunca van en el repositorio.** Las más importantes en producción:

| Variable | Función |
|---|---|
| `SPRING_PROFILES_ACTIVE` | Un valor distinto de `local` (por ejemplo `prod`); sin él se aplicarían los valores de desarrollo |
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | Conexión a PostgreSQL |
| `KUDOS_DISPATCH_ENABLED` | Con valor `true` enciende el envío de avisos; apagado, ningún aviso sale |
| `KUDOS_CORS_ALLOWED_ORIGINS` | Origen de la aplicación web |
| `SPRING_MAIL_*` y `MAIL_FROM` | Servidor SMTP |
| `KUDOS_WHATSAPP_TOKEN`, `KUDOS_WHATSAPP_PHONE_NUMBER_ID` | Activan el canal de WhatsApp |
| `KUDOS_VONAGE_API_KEY`, `KUDOS_VONAGE_API_SECRET` | SMS del código del celular |
| `KUDOS_GRAPH_CLIENT_ID`, `KUDOS_GRAPH_CLIENT_SECRET` | Invitaciones de Microsoft Graph |
| `KUDOS_ATTACHMENTS_STORAGE`, `KUDOS_ATTACHMENTS_CONTAINER_URL`, `KUDOS_ATTACHMENTS_SAS` | Almacenamiento de adjuntos (`azure` o `memory`) |

## Integración y despliegue continuos

El flujo `.github/workflows/ci-cd.yml` hace, en cada pull request y en cada push a `master`:

1. **Compilar y probar** con `./mvnw verify`, analizar con **SonarCloud** y escanear dependencias y secretos con **Trivy**.
2. **Imagen:** en `master`, construir la imagen, escanearla con Trivy y publicarla en GitHub Container Registry, etiquetada con el SHA del commit.
3. **Despliegue:** en `master`, desplegar esa imagen en Azure Container Apps. Inicia sesión en Azure con OIDC, sin guardar contraseñas, con un permiso limitado a la aplicación.

Delante de la API hay un gateway con WAF (nginx, ModSecurity y reglas OWASP CRS) que es la entrada pública.

## Documentación

| Documento | Contenido |
|---|---|
| [Adrs](Documentacion/Adrs/) | Las decisiones de arquitectura, con sus alternativas, costos con precio oficial y registro de cambios |
| [manual-de-puesta-a-punto.md](Documentacion/manual-de-puesta-a-punto.md) | Instalación, configuración, pruebas, despliegue y validación |
| [drivers-arquitectonicos-ucokudos.md](Documentacion/drivers-arquitectonicos-ucokudos.md) | Drivers arquitectónicos |
| [r-tecnicas-negocio-funcionalidades.md](Documentacion/r-tecnicas-negocio-funcionalidades.md) | Restricciones técnicas, reglas de negocio y funcionalidades |
| [tradeoff-calidad-ucokudos.md](Documentacion/tradeoff-calidad-ucokudos.md) | Atributos de calidad, escenarios y trade-offs |
