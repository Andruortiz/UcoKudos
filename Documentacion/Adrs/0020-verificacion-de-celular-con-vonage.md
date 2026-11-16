# ADR-0020: Verificación del celular por SMS con Vonage

- **Estado:** Aceptada
- **Fecha:** 2026-11-16
- **Reemplaza a:** [ADR-0010](0010-verificacion-de-celular.md) (Twilio)
- **Drivers y restricciones:** DRV-005; RT-26; RN-08, RN-09; FS-06
- **ABB:** Autenticación y verificación de identidad; Notificaciones

## Contexto
El ADR-0010 eligió **Twilio** como proveedor del SMS. Al montar la cuenta de prueba de Twilio, no fue posible agregar más celulares verificados, y la cuenta de prueba solo envía a números verificados. La demostración necesita al menos dos celulares (quien presenta y el docente). Se creó una cuenta en **Vonage**, que permite registrar números de prueba, y se comprobó el flujo completo con un celular real.

El diseño no cambia: el puerto `SmsSender` aísla el proveedor, y por eso el cambio se resolvió con un adaptador nuevo y sin tocar el dominio.

## Decisión
- El código de **6 dígitos** se envía por **SMS con la API de Vonage** (`POST /sms/json`), a través del puerto `SmsSender` y el adaptador `VonageSmsSender`.
- **Se mantiene** todo lo decidido en el ADR-0010 sobre el código: se guarda con hash (SHA-256 con el usuario y el celular), caduca a los 10 minutos, máximo 5 intentos por código, un minuto entre envíos y 5 envíos por hora, y cada envío y verificación queda en la auditoría.
- El remitente es `UCOKudos` y el texto del mensaje no lleva tildes, para evitar la conversión a un juego de caracteres más corto que encarece o corrompe el SMS.
- La clave y el secreto de la API viven como secreto del Container App (ADR-0017). Sin configuración, el envío responde 503 y no llama a Vonage.
- **Regla nueva:** un usuario solo puede enviar Kudos con el correo **y** el celular verificados.

## Alternativas consideradas

| Opción | Costos (precio oficial, consulta 2026-11-16) | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| **Vonage SMS API** | **No verificado:** la tarifa por país exige el panel de Vonage con cuenta de pago; la API de precios de la cuenta de prueba responde "Forbidden". La cuenta de prueba incluye crédito inicial | Baja | Media | Medio: la cuenta de prueba solo envía a números registrados y no se confirmó que el remitente alfanumérico `UCOKudos` se muestre en todos los operadores de Colombia | Alta (adaptador de una clase) | Alta |
| Twilio SMS (ADR-0010) | **0,0592 USD por segmento** a Colombia y 1,15 USD al mes por un número internacional ([Twilio](https://www.twilio.com/en-us/sms/pricing/co), noviembre de 2026) | Baja | Media | Alto para la demostración: la cuenta de prueba no dejó agregar más celulares verificados | Alta | Alta |
| Plantilla de autenticación de WhatsApp (Meta) | Cobro por mensaje entregado según categoría y país; las tarifas de Colombia están en el CSV oficial de Meta (**No verificado**) | Media | Alta | Alto: el entorno de prueba no permite crear la plantilla | Media | Alta |
| Verificación por correo del código | Sin costo adicional | Baja | Alta | Medio: no verifica la posesión del celular, que es lo que exige RN-09 | Alta | Media |

## Consecuencias
- **Positivas:** la demostración puede verificar varios celulares; el cambio no tocó el dominio ni el front, solo el adaptador, su prueba y tres propiedades de configuración.
- **Negativas:** la tarifa de Vonage no está verificada; en producción habría que consultarla y compararla con la de Twilio. La cuenta de prueba exige registrar cada celular de la demostración en el panel de Vonage.
- **Pendiente:** confirmar con un celular real la entrega desde Azure (se comprobó en local) y el remitente que muestra el operador.

## Registro de cambios
- 2026-11-16: creación. Reemplaza al ADR-0010 por límites de la cuenta de prueba de Twilio.
