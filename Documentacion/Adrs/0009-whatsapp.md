# ADR-0009: Puerto de WhatsApp con el sandbox de Meta para el aviso del Kudo

- **Estado:** Aceptada
- **Fecha:** 2026-11-11
- **Drivers y restricciones:** DRV-003; RT-14, RT-26; RN-33; FS-02. El enunciado exige que el destinatario reciba un aviso por WhatsApp y lo incluye como condición de entrega ("WhatsApp funcionando").
- **ABB:** Notificaciones

## Contexto
El Kudo debe llegar por WhatsApp. Se dispone del **sandbox de Meta**: un número de prueba que solo envía a una lista corta de destinatarios verificados, y que no permite crear plantillas de autenticación. Sí se aprobó una plantilla de categoría Marketing, `kudo_recibido`, en español de Colombia, con cuatro variables (destinatario, autor, categoría y enlace al Kudo).

## Decisión
- El dominio define un **puerto `WhatsAppSender`**. Cada proveedor es un adaptador intercambiable.
- **Adaptador inicial:** API en la nube de WhatsApp (Meta), usando el sandbox y la plantilla `kudo_recibido`.
- Los destinatarios de la demostración se **siembran con los números ya verificados en la lista del sandbox** (RT-26).
- La aplicación de Meta permanece en **modo de desarrollo**; no se pasa a producción.
- El token de acceso debe ser **permanente** (de un usuario del sistema) y se guarda en el gestor de secretos (ADR-0017). El token temporal caduca a las 24 horas.
- **El código de verificación del celular no se envía por WhatsApp** (ver ADR-0010).
- **Evolución:** pasar a producción con un número propio y una plantilla de categoría Utilidad (más barata que Marketing, según las fuentes consultadas); añadir otro adaptador si se cambia de proveedor.

## Alternativas consideradas

| Opción | Costos | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| **Sandbox de Meta con la plantilla aprobada** | Bajo (por verificar la tarifa oficial; fuentes de terceros sitúan Marketing en torno a 0,015 USD por mensaje en Colombia) | Baja | Baja (solo números de la lista) | Medio: depende de la lista de destinatarios y del token permanente | Alta | Alta: se pasa a producción con otro número |
| Sandbox de WhatsApp de otro proveedor (por ejemplo, Twilio) | Por verificar | Baja | Baja | Medio: la sesión caduca y no usa plantillas propias | Alta | Media |
| Meta en producción con número propio | Por verificar | Alta (verificaciones y número propio) | Alta | Alto para el plazo | Media | Alta |
| Otros proveedores de WhatsApp | Por verificar | Media | Media-alta | Medio | Media | Alta |

## Consecuencias
- **Positivas:** WhatsApp funciona en la demostración con la plantilla ya aprobada; el puerto permite cambiar de proveedor sin tocar el dominio.
- **Negativas:** solo llegan mensajes a los números de la lista; la categoría Marketing es la más cara y puede tener límites.
- **Pendiente:** comprobar que el número de prueba puede enviar esa plantilla (debe pertenecer a su misma cuenta); verificar tu número y el del docente en la lista; crear el token permanente; guardar la captura del mensaje recibido.

## Costos con precio oficial
Consulta del 16 de noviembre de 2026 en la documentación de Meta (ver [precios-oficiales.md](precios-oficiales.md)).

| Opción | Precio oficial |
|---|---|
| **Sandbox de Meta con la plantilla aprobada** | Meta cobra **por mensaje entregado**, según la categoría (marketing, utilidad, autenticación) y el país. Las tarifas de Colombia se publican en archivos CSV y PDF, en COP y USD; **la cifra de Colombia no se obtuvo**. La cifra de 0,015 USD que cita la tabla anterior proviene de fuentes de terceros y **no está verificada** |
| Mensajes de servicio (dentro de una ventana de 24 horas) | Un nivel gratuito de **1 000 mensajes entregados por número y mes** |
| Sandbox de otro proveedor | **No verificado** |
| Meta en producción con número propio | Igual cobro por mensaje; el número propio no tiene costo de lista en Meta (**No verificado**) |

## Registro de cambios
- 2026-11-11: creación como Propuesta.
- 2026-11-16: pasa a **Aceptada.** El puerto `WhatsAppSender` y el adaptador de la API en la nube de Meta están construidos, con la plantilla `kudo_recibido`. El token y el identificador del número están en el Key Vault.
- 2026-11-16: **por comprobar.** No se verificó que el adaptador esté activo en el Container App ni que el mensaje llegue a un número de la lista del sandbox, y falta la captura de evidencia.
- 2026-11-16: **cambio de plantilla.** Meta aprobó `kudos_envios_url` (español de Colombia, `es_CO`), con tres variables en el cuerpo (destinatario, autor y categoría), el pie "Equipo UcoKudos" y un botón **Ver Kudo** de URL dinámica (`https://kudos.ucokudos.qd.je/kudos/{{1}}`). El adaptador envía ahora el cuerpo de tres variables y el identificador del Kudo como parámetro del botón; la plantilla anterior `kudo_recibido`, con el enlace en el cuerpo, deja de usarse. El token del Key Vault es un token de usuario del sistema **permanente** (no vence) con los permisos de mensajería y administración de WhatsApp.
