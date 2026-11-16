# ADR-0008: Puerto de correo con proveedor intercambiable

- **Estado:** Aceptada (puerto y proveedor decididos)
- **Fecha:** 2026-11-11
- **Drivers y restricciones:** DRV-003; RT-14, RT-25; RN-33; FS-02
- **ABB:** Notificaciones

## Contexto
El destinatario de un Kudo recibe un correo. La **invitación** de acceso la envía Microsoft (ADR-0003), así que este canal solo manda **avisos de Kudos**. No hay dominio propio, lo que dificulta verificar un dominio de envío y puede llevar los correos a la carpeta de spam.

## Decisión
- El dominio define un **puerto `CorreoSender`**; el proveedor se conecta con un adaptador.
- Los textos salen del catálogo de mensajes (ADR-0017).
- El proveedor se elige después de **verificar costos, límites y requisitos de dominio**. Criterios: que funcione sin dominio propio para la demostración, que tenga plan gratuito suficiente y que permita ver el estado de entrega.
- Los envíos fallidos se reintentan por la bandeja de salida (ADR-0006).

## Alternativas consideradas

| Opción | Costos | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| Servicio de correo de la nube (por ejemplo, el de Azure) | Por verificar | Media | Alta | Medio: pide dominio verificado; sin él, el dominio gestionado puede caer en spam | Media | Alta |
| Proveedor de correo transaccional con plan gratuito | Por verificar | Baja | Media-alta | Medio: límites diarios y dominio | Alta | Alta |
| SMTP de un correo personal | Bajo | Baja | Baja | Alto: límites y bloqueo de cuentas | Baja | Baja |

## Consecuencias
- **Positivas:** cambiar de proveedor no toca el dominio; las pruebas usan un adaptador simulado.
- **Negativas:** el proveedor concreto queda sin decidir hasta verificar costos.
- **Pendiente:** elegir proveedor, probar la entrega en un buzón real y guardar la captura como evidencia (RT-19).

## Costos con precio oficial
Consulta del 16 de noviembre de 2026 (ver [precios-oficiales.md](precios-oficiales.md)).

| Opción | Precio oficial | Nota |
|---|---|---|
| **SMTP de la cuenta de Gmail del proyecto** | Sin costo para el proyecto. Los límites de envío de Google: **No verificado** | Es la opción de "SMTP de un correo personal" que esta tabla calificaba con riesgo alto |
| Servicio de correo de Azure (Azure Communication Services Email) | **No verificado** (no se obtuvo desde la API de precios de Azure) | Pide dominio verificado |
| Proveedor transaccional con plan gratuito | **No verificado** | Límites diarios y dominio |

## Registro de cambios
- 2026-11-11: creación como Propuesta; el puerto estaba decidido y el proveedor no.
- 2026-11-16: pasa a **Aceptada.** El puerto de correo se materializó con un adaptador SMTP (Spring Mail) y el proveedor es el **SMTP de Gmail** (`smtp.gmail.com`, puerto 587 con STARTTLS), con una cuenta de Gmail del proyecto. En local se usa Mailpit. Se eligió por no exigir dominio propio.
- 2026-11-16: **riesgo aceptado y por comprobar.** El riesgo de límites y de bloqueo de cuenta que este ADR señaló sigue vigente. **La entrega real a un buzón y su captura como evidencia no se han comprobado.**
