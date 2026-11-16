# ADR-0006: Bandeja de salida (Outbox) para notificaciones fiables

- **Estado:** Aceptada, con implementación parcial
- **Fecha:** 2026-11-11
- **Drivers y restricciones:** DRV-003; RT-06, RT-13, RT-14; RN-33; FS-01, FS-02
- **ABB:** Mensajería; Notificaciones

## Contexto
Publicar un Kudo debe guardar, enviar correo, enviar WhatsApp, enviar push si el destinatario está conectado y auditar. El guardado debe ser fiable aunque un proveedor externo falle o tarde. El Reactive Manifesto pide resiliencia y orientación a mensajes (RT-06).

## Decisión
- En **una sola transacción** se guarda el Kudo y se insertan en la tabla de **bandeja de salida** las notificaciones pendientes, una por canal (correo, WhatsApp, push), con su estado.
- Un **proceso programado dentro del mismo servicio** lee los pendientes y los envía por el puerto de cada canal.
- **Reintentos** con espera creciente y un máximo configurable (ADR-0017); al agotarse, el estado pasa a `FALLIDA` y queda visible y auditado.
- **Idempotencia:** cada notificación tiene un identificador único para no reenviar dos veces.
- Con varias instancias, cada una toma los pendientes con bloqueo (`SELECT ... FOR UPDATE SKIP LOCKED`).
- El push se intenta de inmediato si el destinatario está conectado (ADR-0007).

## Alternativas consideradas

| Opción | Costos | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| **Outbox con sondeo en el mismo servicio** | Bajo | Media | Media-alta | Bajo | Alta | Alta: se puede mover a un bus |
| Envío síncrono dentro de la petición | Bajo | Baja | Baja | Alto: un proveedor lento o caído bloquea o pierde la publicación | Media | Baja |
| Bus o cola externa (por ejemplo, un servicio de mensajería gestionado) | Por verificar | Alta | Alta | Medio: otra pieza que operar | Media | Alta |
| Captura de cambios (CDC) | Medio | Alta | Alta | Alto para el plazo | Baja | Alta |

## Consecuencias
- **Positivas:** la publicación nunca depende de un proveedor; cada canal se reintenta de forma independiente; la bandeja es evidencia de trazabilidad.
- **Negativas:** consistencia eventual: el aviso llega unos segundos después; el sondeo consume algo de base de datos.
- **Riesgos:** envíos duplicados si falla la idempotencia; mitigado con identificadores y pruebas.

## Costos con precio oficial
Consulta del 16 de noviembre de 2026 (ver [precios-oficiales.md](precios-oficiales.md)).

| Opción | Precio oficial | Estimado mensual |
|---|---|---|
| **Outbox con sondeo en el mismo servicio** | Sin servicio adicional: usa la base de datos y el contenedor existentes (ADR-0005 y ADR-0013) | Sin costo adicional |
| Envío síncrono | Sin costo adicional | Sin costo adicional |
| Azure Service Bus, nivel Standard, Central US | Unidad base **0,013441 USD por hora (10 USD al mes)** más operaciones y conexiones | 10 USD de base |
| Captura de cambios (CDC) | **No verificado** (depende de la herramienta elegida) | No verificado |

## Registro de cambios
- 2026-11-11: creación como Propuesta.
- 2026-11-16: pasa a **Aceptada, con implementación parcial.** Construido: la tabla de bandeja de salida con una notificación por canal en la misma transacción que el Kudo, un proceso programado que envía por el puerto de cada canal, reintentos con espera creciente y estado `FALLIDA`.
- 2026-11-16: **desviación declarada.** El bloqueo de filas con `SELECT ... FOR UPDATE SKIP LOCKED` **no está construido**, así que con más de una instancia podría enviarse dos veces la misma notificación. Hoy hay una sola réplica (ADR-0013).
