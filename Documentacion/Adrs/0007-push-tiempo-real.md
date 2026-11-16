# ADR-0007: Push por tiempo real propio (SSE), sin servicio externo

- **Estado:** Aceptada
- **Fecha:** 2026-11-11
- **Drivers y restricciones:** DRV-001, DRV-003; RT-13, RT-14; RN-33; FS-02
- **ABB:** Notificaciones; Mensajería

## Contexto
El enunciado pide un push **si el destinatario está conectado**. No pide entrega con la aplicación cerrada. Un servicio externo de notificaciones push añadiría una dependencia y cuentas a configurar.

## Decisión
- El push se entrega por una **conexión de tiempo real propia** entre la aplicación web y la API, sin sistema externo.
- Mecanismo recomendado: **Server-Sent Events (SSE)**, porque el flujo es unidireccional (servidor a cliente), funciona sobre HTTP y es más simple que WebSocket.
- Si el destinatario no está conectado, el aviso queda registrado y llega por correo y WhatsApp (ADR-0006).
- La conexión pasa por el WAF y el API Gateway, que deben permitir conexiones largas sin almacenar la respuesta (ADR-0016).

## Alternativas consideradas

| Opción | Costos | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| **SSE propio** | Bajo | Baja | Media | Bajo | Alta | Media |
| WebSocket propio | Bajo | Media | Media | Medio | Media | Alta |
| Servicio de push del navegador o de terceros (FCM, OneSignal, otros) | Por verificar | Media | Alta | Medio: cuentas, claves y permisos del navegador | Media | Alta: llega con la pestaña cerrada |
| Servicio gestionado de tiempo real | Por verificar | Media | Alta | Medio | Media | Alta |

## Consecuencias
- **Positivas:** cumple "si está conectado" sin proveedor externo; el nivel 1 solo tiene correo, WhatsApp y Entra como externos.
- **Negativas:** no llega con la pestaña cerrada; las conexiones abiertas consumen recursos del servidor.
- **Riesgos:** con varias instancias, el usuario puede estar conectado a una distinta de la que procesa la notificación. Con una instancia no ocurre; si se escala, hay que difundir los avisos entre instancias.
- **Evolución:** push del navegador (Web Push) si el docente pide avisos con la pestaña cerrada.

## Costos con precio oficial
El SSE propio no agrega servicios: corre en el mismo contenedor de la API (ADR-0013), sin costo adicional. Los servicios de push de terceros y el servicio gestionado de tiempo real de Azure: **No verificado** (no se consultó su precio oficial).

## Registro de cambios
- 2026-11-11: creación como Aceptada, con el mecanismo SSE pendiente de confirmar.
- 2026-11-16: **SSE confirmado e implementado.** Un registro de conexiones por usuario en memoria (`PushNotificationSender`) y el endpoint `GET /api/v1/notifications/stream`. Probado con dos usuarios, con la pestaña en segundo plano y con la prueba `PushStreamIntegrationTest`.
- 2026-11-16: **diferencias declaradas:**
  - El front lee el flujo con `fetch` y no con `EventSource`, porque `EventSource` no permite enviar la cabecera `Authorization`.
  - La API envía un comentario inicial `connected` al suscribirse, para que las cabeceras lleguen antes del primer evento, y la cabecera `X-Accel-Buffering: no`, para que el gateway ([ADR-0021](0021-gateway-y-waf-con-modsecurity.md)) no retenga los eventos.
  - El registro de conexiones está en memoria: **solo sirve con una réplica**, como advertía este ADR.
  - Azure Container Apps corta las conexiones inactivas a los 4 minutos (**No verificado** con el front real); el front debe reconectar.
