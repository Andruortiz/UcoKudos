# ADR-0017: Secretos, parámetros y catálogo de mensajes

- **Estado:** Aceptada, versión reducida
- **Fecha:** 2026-11-11
- **Drivers y restricciones:** DRV-005, DRV-007; RT-07 (Twelve-Factor: configuración en el entorno), RT-18
- **ABB:** Gestión de configuración; Seguridad

## Contexto
La aplicación usa credenciales de terceros (proveedores de correo y WhatsApp, directorio de identidad, base de datos). Hay valores que cambian por entorno o con el tiempo (dominios permitidos, límites, reintentos). Los textos de errores y las plantillas de notificación deben poder cambiar sin tocar la lógica.

## Decisión

| Elemento | Decisión |
|---|---|
| **Secretos** | En un **gestor de secretos** (candidato: Azure Key Vault). Nunca en el repositorio. Se inyectan como variables de entorno al desplegar. En local, un archivo ignorado por el control de versiones |
| **Configuración por entorno** | Variables de entorno (Twelve-Factor). La misma imagen sirve para todos los entornos |
| **Catálogo de parámetros** | Tabla de la base de datos, con valores por defecto en la configuración, leída con caché. Contiene: dominios institucionales permitidos, límites de adjuntos (SUP-04), reintentos, caducidad del código del celular, tabla de aprobaciones |
| **Catálogo de mensajes** | Textos de errores en archivos de recursos por idioma (versionados en el repositorio); plantillas de correo y WhatsApp por canal e idioma |

- Los secretos se rotan y no se escriben en logs ni en respuestas de error.
- Quién puede modificar los parámetros: solo el Administrador, con auditoría.

## Alternativas consideradas

| Opción | Costos | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| **Gestor de secretos + variables de entorno + parámetros en la base de datos** | Bajo (por verificar el plan del gestor) | Media | Alta | Bajo | Alta | Alta |
| Variables de entorno y archivos de configuración solamente | Nulo | Baja | Media | Medio: los secretos quedan en la plataforma de despliegue sin rotación | Media | Media |
| Servicio de configuración gestionado (por ejemplo, App Configuration) | Por verificar | Media | Alta | Medio: otra pieza y límites de solicitudes | Media | Alta |

## Consecuencias
- **Positivas:** cumple Twelve-Factor; las credenciales no se filtran; los parámetros cambian sin desplegar.
- **Negativas:** más piezas de infraestructura; hay que cargar los valores iniciales con la semilla (FS-18).
- **Pendiente:** confirmar si los catálogos viven en la base de datos o en un servicio aparte; el diagrama los muestra como bloques separados por claridad.

## Costos con precio oficial
Consulta del 16 de noviembre de 2026 (ver [precios-oficiales.md](precios-oficiales.md)).

| Opción | Precio oficial | Estimado mensual |
|---|---|---|
| **Azure Key Vault, Standard, East US 2** | Operaciones **0,03 USD por 10 000** | Menos de 1 USD |
| Secretos en el propio Container App | Sin costo adicional | 0 USD |
| Azure App Configuration | **No verificado** (no se consultó) | No verificado |

## Registro de cambios
- 2026-11-11: creación como Propuesta.
- 2026-11-16: pasa a **Aceptada, versión reducida.** Construido:
  - **Secretos:** existe el Key Vault `kv-ucokudos` (contraseñas de la base de datos y de SMTP, secreto de Graph, token de WhatsApp). Los valores que usa la aplicación se copian como **secretos del Container App** y se inyectan con `secretref`, porque el entorno no admite referencias directas a Key Vault. Los secretos de Vonage y del almacenamiento de adjuntos se guardan **solo** en el Container App.
  - **Configuración por entorno:** variables de entorno; la misma imagen sirve en todos los entornos.
  - **Catálogo de mensajes:** textos en `messages.properties` y un catálogo recargable (`../../config/messages.json` con un proceso que lo refresca).
- 2026-11-16: **no construido:** el **catálogo de parámetros en la base de datos** (dominios, límites, reintentos y caducidad del código viven en `application.properties` con valores por defecto) y la modificación por el Administrador con auditoría. **La rotación de secretos no está automatizada**; el SAS de los adjuntos vence el **16 de noviembre de 2027** y debe renovarse a mano.
