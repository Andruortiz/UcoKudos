# Manual de puesta a punto de UCO Kudos

Explica cómo instalar, configurar, probar, ejecutar y desplegar la plataforma, y cómo validar el flujo obligatorio **Publicar Kudo**. Está pensado para que una persona con los requisitos de la sección 1 pueda reproducir el proyecto desde cero.

Convenciones: los bloques de código con `bash` se ejecutan en una terminal. Donde algo **no se pudo comprobar**, el texto lo dice con la marca **(por confirmar)**.

## 1. Qué incluye el proyecto

| Pieza | Repositorio | Tecnología |
|---|---|---|
| API (back-end) | `https://github.com/Andruortiz/UcoKudos` | Java 27, Spring Boot 4.1.2, Maven, PostgreSQL, Flyway |
| Aplicación web (front-end) | `https://github.com/Andruortiz/front-kudos` | React 19, TypeScript, Vite, MSAL |

Sitio publicado: `https://kudos.ucokudos.qd.je`.

## 2. Requisitos

| Herramienta | Versión | Para qué |
|---|---|---|
| JDK | 27 (Temurin recomendado) | Compilar y ejecutar la API. El `JAVA_HOME` debe apuntar a un JDK 27: compruébelo con `./mvnw -v` |
| Node.js | 24 | Compilar y ejecutar la aplicación web |
| Docker | Reciente (Docker Desktop en Windows) | PostgreSQL y Mailpit en local, y construir la imagen |
| Git | Reciente | Clonar los repositorios |

No hace falta instalar Maven: el repositorio trae el wrapper `./mvnw`.

## 3. Obtener el código

```bash
git clone https://github.com/Andruortiz/UcoKudos.git
```

```bash
git clone https://github.com/Andruortiz/front-kudos.git
```

## 4. Ejecutar en local

### 4.1 Base de datos y correo de desarrollo

```bash
docker run -d --name kudos-db -e POSTGRES_DB=kudos -e POSTGRES_USER=kudos -e POSTGRES_PASSWORD=kudos -p 5432:5432 postgres:17
```

```bash
docker run -d --name mailpit -p 1025:1025 -p 8025:8025 axllent/mailpit
```

Mailpit recibe los correos que envía la API y los muestra en `http://localhost:8025`, sin enviar nada real.

### 4.2 API

Sin perfil explícito, la API usa el perfil **`local`** (`spring.profiles.default=local`). Ese perfil:
- se conecta a `jdbc:postgresql://localhost:5432/kudos` con el usuario y la clave `kudos` (cámbielos con `DB_URL`, `DB_USER` y `DB_PASSWORD`);
- aplica las migraciones de Flyway y además las **semillas de prueba** (`db/local`): las categorías y tres usuarios, Ana (estudiante), Luis (docente) y Carlos (estudiante);
- envía el correo a Mailpit (`MAIL_HOST` y `MAIL_PORT`);
- **enciende el despacho de notificaciones** (`kudos.dispatch.enabled=true`).

Para trabajar **sin iniciar sesión con Microsoft**, active la autenticación de desarrollo, que identifica al usuario por la cabecera `X-Dev-User`:

```bash
KUDOS_DEV_AUTH_ENABLED=true ./mvnw spring-boot:run
```

En PowerShell:

```powershell
$env:KUDOS_DEV_AUTH_ENABLED = "true"; ./mvnw spring-boot:run
```

Sin esa variable, la API exige un token real de Microsoft Entra (sección 6). Comprobación: `http://localhost:8080/actuator/health` debe responder `{"status":"UP"}`.

### 4.3 Aplicación web

```bash
cd front-kudos
```

```bash
npm install
```

Copie `.env.example` a `.env` y ajuste:

| Variable | Valor local | Significado |
|---|---|---|
| `VITE_API_URL` | `http://localhost:8080` | Dirección de la API |
| `VITE_ENTRA_TENANT_ID`, `VITE_ENTRA_CLIENT_ID` | Los de la sección 6 | Con ambos, la aplicación pide iniciar sesión con Microsoft. **Sin ellos corre sin login** |
| `VITE_DEV_USER_ID` | `00000000-0000-0000-0000-000000000001` | Solo sin login: actúa como Ana; use `...0002` para Luis y `...0003` para Carlos |

```bash
npm run dev
```

La aplicación queda en `http://localhost:5173`, que es el único origen que la API local acepta por CORS.

## 5. Variables de entorno de la API

Cada propiedad de `application.properties` se puede sobrescribir con una variable de entorno (por ejemplo `kudos.whatsapp.token` es `KUDOS_WHATSAPP_TOKEN`). **Los secretos nunca van en el repositorio.**

| Variable | ¿Secreto? | Obligatoria en producción | Función |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | No | Sí | Ponga un valor distinto de `local` (por ejemplo `prod`). Sin él, **en producción se aplicarían los valores de local** |
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | La clave sí | Sí | Conexión a PostgreSQL |
| `KUDOS_DISPATCH_ENABLED` | No | Sí, con valor `true` | Enciende el despacho de notificaciones. Por defecto está apagado fuera de `local`; si se olvida, **ningún aviso sale** |
| `KUDOS_DISPATCH_LINK_BASE_URL` | No | Sí | Base del enlace de los avisos, por ejemplo `https://kudos.ucokudos.qd.je/kudos/` |
| `KUDOS_CORS_ALLOWED_ORIGINS` | No | Sí | Origen de la aplicación web, por ejemplo `https://kudos.ucokudos.qd.je` |
| `ENTRA_TENANT_ID`, `ENTRA_CLIENT_ID` | No (son públicos) | Opcional | Identifican el directorio y la aplicación; ya tienen valor por defecto en el repositorio |
| `REGISTRATION_ALLOWED_DOMAINS`, `LOGIN_ALLOWED_DOMAINS` | No | Opcional | Dominios de correo permitidos, separados por comas |
| `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD` | La clave sí | Sí | Servidor SMTP. El adaptador de correo **solo existe si `SPRING_MAIL_HOST` tiene valor**. Con Gmail se necesita una contraseña de aplicación **(por confirmar)** |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH`, `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE` | No | Según el proveedor | Normalmente `true` en ambos |
| `MAIL_FROM` | No | Sí | Remitente de los avisos |
| `KUDOS_WHATSAPP_TOKEN`, `KUDOS_WHATSAPP_PHONE_NUMBER_ID` | El token sí | Sí | El adaptador de WhatsApp **solo existe si ambas tienen valor**. El token debe ser permanente (ADR-0009) |
| `KUDOS_GRAPH_CLIENT_ID`, `KUDOS_GRAPH_CLIENT_SECRET` | El secreto sí | Sí | Aplicación de Entra que invita a personas nuevas. Sin ellas, pedir acceso responde un error de acceso no disponible |
| `KUDOS_VONAGE_API_KEY`, `KUDOS_VONAGE_API_SECRET`, `KUDOS_VONAGE_FROM` | El secreto sí | Sí | SMS con el código del celular |
| `KUDOS_MESSAGES_URL` | No | No | Dirección HTTPS de un JSON con textos de mensajes que se recarga cada minuto. Vacía, solo se usa `messages.properties` |

Valores con límites configurables (con su valor actual): `kudos.daily-limit` (50 Kudos al día), `kudos.idempotency-window` (1 hora), `kudos.dispatch.max-attempts` (5), `kudos.dispatch.base-delay` (1 minuto), `kudos.phone-verification.*` (código de 10 minutos, 5 envíos por hora, 5 intentos) y `kudos.recipients.limit` (20).

## 6. Microsoft Entra ID

La autenticación está en Microsoft Entra (ADR-0003). Estos son los valores del proyecto; los identificadores de directorio y de aplicación son públicos.

| Dato | Valor |
|---|---|
| Directorio (tenant) | `36f84d3d-a9fa-4302-8430-a675907c034d` |
| Aplicación (cliente) | `4acf7f6e-4bdc-4547-aa3f-2ebaa7cb48e6` |
| Ámbito que pide la aplicación web | `api://4acf7f6e-4bdc-4547-aa3f-2ebaa7cb48e6/access_as_user` |
| Emisor que valida la API | `https://login.microsoftonline.com/<tenant>/v2.0` |

Para reproducirlo en otro directorio:
1. Registrar una aplicación y, como plataforma **aplicación de una sola página**, agregar como URI de redirección `http://localhost:5173` y la dirección pública del sitio.
2. Exponer una API con el ámbito `access_as_user` (URI `api://<cliente>`) y configurar la aplicación para emitir tokens versión 2 **(por confirmar en el manifiesto)**.
3. Para las invitaciones, una aplicación (puede ser la misma) con un secreto de cliente y el permiso de aplicación de Microsoft Graph para invitar usuarios (`User.Invite.All`) con consentimiento de administrador **(por confirmar)**.
4. Cargar las variables de la sección 5 y las `VITE_ENTRA_*` del front-end.

## 7. Pruebas

```bash
./mvnw -B verify
```

Ejecuta las pruebas unitarias y de integración (H2 en memoria, perfil `test`) y genera la cobertura con JaCoCo en `target/site/jacoco`. Estado verificado: **200 pruebas, 0 fallos, 5 omitidas**.

En el front-end:

```bash
npm run lint
```

```bash
npm test
```

```bash
npm run build
```

Estado verificado: **41 pruebas, 0 fallos**.

Pruebas de extremo a extremo con un navegador real: **no existen** todavía (la pirámide de pruebas del enunciado las pide).

## 8. Integración y despliegue continuos

Ambos repositorios tienen `.github/workflows/ci-cd.yml`. Se ejecuta en cada `pull request` y en cada `push` a `master`.

| Repositorio | Pasos de integración | Despliegue (solo `master`) |
|---|---|---|
| Back-end | Compila y prueba (`./mvnw -B verify`), analiza con SonarCloud, escanea dependencias y secretos con Trivy | Construye la imagen, la escanea con Trivy y la publica en `ghcr.io` con las etiquetas del commit y `latest` |
| Front-end | `npm ci`, lint, pruebas, Trivy y compilación | Publica el sitio en Azure Static Web Apps y verifica que `/`, `/kudos/abc` y `/perfil` respondan 200 |

Configuración necesaria en GitHub:

| Dónde | Nombre | Tipo | Uso |
|---|---|---|---|
| Back-end | `SONAR_TOKEN` | Secreto | Si falta, el paso de Sonar se omite |
| Back-end | `SONAR_HOST_URL` | Secreto, opcional | Por defecto, `https://sonarcloud.io` |
| Back-end | `SONAR_ORGANIZATION`, `SONAR_PROJECT_KEY` | Variables | Organización y proyecto en SonarCloud |
| Front-end | `AZURE_STATIC_WEB_APPS_API_TOKEN` | Secreto | Publicar el sitio |

Las variables `VITE_*` de producción están dentro del propio flujo del front-end porque Vite las incrusta al compilar; no son secretos.

## 9. Despliegue en Azure

| Pieza | Servicio | Notas |
|---|---|---|
| Aplicación web | Azure Static Web Apps | Dominio `kudos.ucokudos.qd.je`. `public/staticwebapp.config.json` hace que un enlace directo abra la aplicación y no un 404, y fija las cabeceras de seguridad |
| API | Azure Container Apps | Ejecuta la imagen del back-end. Mantenga al menos una réplica activa durante la demostración para evitar arranques en frío (ADR-0013) |
| Base de datos | PostgreSQL | El alojamiento no está documentado en el repositorio **(por confirmar)** |

Pasos para desplegar la API:
1. Crear la base de datos PostgreSQL y un usuario con permiso sobre ella.
2. Crear la aplicación de contenedor con la imagen `ghcr.io/andruortiz/ucokudos` (si el paquete es privado, registrar las credenciales de `ghcr.io`).
3. Definir las variables de la sección 5 como configuración y secretos de la aplicación de contenedor, **incluidos `SPRING_PROFILES_ACTIVE` y `KUDOS_DISPATCH_ENABLED=true`**.
4. Exponer el puerto `8080` con ingreso HTTPS. Flyway crea el esquema y las 15 categorías al arrancar.
5. Comprobar `GET /actuator/health`.
6. Actualizar `VITE_API_URL` en el flujo del front-end con la dirección pública de la API y agregar la dirección del sitio como URI de redirección en Entra.

**Por confirmar:** el flujo del back-end publica la imagen pero **no contiene un paso que la despliegue** en Azure. Cómo llega la imagen nueva a Container Apps (actualización manual, revisión automática u otro mecanismo) debe quedar documentado, porque el enunciado exige un despliegue automatizado.

Cuentas de demostración: en producción **no se siembran usuarios** (las semillas solo corren con el perfil `local`). Para que el docente pueda probar: pedir acceso con su correo (la API invita y Microsoft envía la invitación), iniciar sesión y registrarse. Los Kudos solo se pueden enviar entre roles permitidos (un estudiante escribe a estudiantes y docentes).

## 10. Validar el flujo Publicar Kudo

Con la aplicación publicada o en local con Mailpit:

1. Inicie sesión con una cuenta de estudiante y entre a **Enviar un Kudo**.
2. Elija un destinatario (al abrir el selector aparecen sus recientes y compañeros; con 3 letras busca en toda la universidad), una categoría y escriba el mensaje.
3. Pulse **Enviar**: la aplicación muestra el estado de cada canal (`PENDING`).
4. En 30 segundos como máximo, el despacho envía los avisos. Verifique:
   - **Correo:** el buzón del destinatario (o `http://localhost:8025` en local).
   - **WhatsApp:** el celular del destinatario. En el sandbox de Meta solo llega a los números registrados en la lista de prueba.
   - **Push:** abra otra sesión con el destinatario antes de publicar; debe aparecer el aviso sin recargar.
5. **Auditoría:** en la base de datos:

```sql
SELECT occurred_at, action, resource_type, resource_id, correlation_id
FROM audit_record
ORDER BY occurred_at DESC
LIMIT 10;
```

   Debe verse `KUDO_PUBLISHED` y luego `NOTIFICATION_SENT` por canal.
6. **Notificaciones:** `SELECT channel, status, attempts, last_error FROM notification ORDER BY created_at DESC LIMIT 10;`

Los adjuntos **todavía no funcionan**: la API responde 503 a cualquier Kudo con archivos hasta que exista el analizador de virus (ADR-0011).



## 11. Problemas frecuentes

| Síntoma | Causa probable | Solución |
|---|---|---|
| `./mvnw: Permission denied` en Linux o en el CI | El wrapper se guardó sin permiso de ejecución | `git update-index --chmod=+x mvnw` y commit |
| Error de versión de Java al compilar | `JAVA_HOME` apunta a un JDK menor que 27 | Instalar JDK 27 y comprobar con `./mvnw -v` |
| `Failed to configure a DataSource` | No hay base de datos configurada | Usar el perfil `local` con PostgreSQL, o definir `SPRING_DATASOURCE_*` |
| Todas las peticiones responden 401 en local | Está activo el login real y no hay token | Definir `KUDOS_DEV_AUTH_ENABLED=true` o iniciar sesión con Entra |
| Los avisos nunca salen en producción | `KUDOS_DISPATCH_ENABLED` está apagado | Definirlo como `true` |
| En producción se usan valores de local | No se definió `SPRING_PROFILES_ACTIVE` | Definirlo con un valor distinto de `local` |
| El navegador bloquea la API (CORS) | El origen no está permitido | Ajustar `KUDOS_CORS_ALLOWED_ORIGINS` |
| Un enlace directo da 404 en el sitio | Falta `staticwebapp.config.json` en el despliegue | Verificar que `public/staticwebapp.config.json` llegue al sitio |
| Pedir acceso responde un error de acceso no disponible | Faltan `KUDOS_GRAPH_CLIENT_ID` o `KUDOS_GRAPH_CLIENT_SECRET` | Definir ambos |
