# ADR-0010: Verificación del celular con código por SMS (Twilio)

- **Estado:** Reemplazada por [ADR-0020](0020-verificacion-de-celular-con-vonage.md) (se conserva como historia)
- **Fecha:** 2026-11-11
- **Drivers y restricciones:** DRV-005; RT-26; RN-08, RN-09; FS-06
- **ABB:** Autenticación y verificación de identidad; Notificaciones

## Contexto
La cuenta solo se activa con el **celular verificado** (RN-09). El enunciado pide el número celular y su verificación, pero **no fija el canal** de verificación; WhatsApp aparece solo como canal de notificación del Kudo. El sandbox de Meta no permite crear una plantilla de autenticación, y el servicio de verificación de Twilio por WhatsApp exige un remitente aprobado por Meta, que no se tiene.

## Decisión
- Se envía un **código numérico de 6 dígitos por SMS**, a través de un **puerto `SmsSender`** con un adaptador de **Twilio**.
- La aplicación genera y valida el código. Se guarda **con hash**, no en claro, y **caduca** (por ejemplo, 10 minutos).
- Límites: máximo de intentos por código y de reenvíos por hora, configurables en el catálogo de parámetros (ADR-0017).
- Al acertar, se marca `celular_verificado`. Cada envío y verificación queda en la auditoría.
- Las cuentas de demostración se siembran verificadas (SUP-03). Los números reales de la demostración deben estar verificados en la cuenta de Twilio.

## Alternativas consideradas

| Opción | Costos | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| **SMS con Twilio** | Por verificar (cuenta de prueba con crédito limitado) | Baja | Media | Medio: la cuenta de prueba envía solo a números verificados y con un tope diario, según el soporte de Twilio; falta comprobar la entrega a Colombia | Alta | Alta |
| Plantilla de autenticación de Meta | Bajo | Media | Alta | Alto: el sandbox no permite crearla | Media | Alta |
| Código por el sandbox de WhatsApp de otro proveedor | Por verificar | Baja | Baja | Medio: caduca y solo llega a números unidos | Alta | Media |
| Código inverso (el usuario envía el código por WhatsApp) | Bajo | Media: requiere webhook | Media | Medio | Media | Media |
| Servicio de verificación de Twilio por WhatsApp | 0,05 USD por verificación más el canal (según su página de precios) | Media | Alta | Alto: exige remitente aprobado por Meta | Media | Alta |

## Consecuencias
- **Positivas:** desacopla la verificación de las plantillas de Meta; cumple RN-09; el puerto permite cambiar de proveedor.
- **Negativas:** una integración más (SMS) además de WhatsApp y correo; el SMS no figura entre los canales del enunciado.
- **Seguridad:** limitar intentos y reenvíos para evitar fuerza bruta y abuso de mensajes de pago.
- **Pendiente:** probar que el SMS llega a tu número y al del docente; confirmar los límites de la cuenta de prueba.

## Registro de cambios
- 2026-11-11: creación como Propuesta, con Twilio.
- 2026-11-16: pasa a **Reemplazada por [ADR-0020](0020-verificacion-de-celular-con-vonage.md)**. La cuenta de prueba de Twilio no permitió agregar más celulares verificados para la demostración. El contenido de este ADR no se modifica. El diseño del código (puerto `SmsSender`, código con hash, límites) se mantiene en el ADR-0020.
- 2026-11-16: precio oficial de Twilio para SMS a Colombia: **0,0592 USD por segmento** y número internacional desde 1,15 USD al mes (ver [precios-oficiales.md](precios-oficiales.md)). Reemplaza el "por verificar" de la tabla anterior.
