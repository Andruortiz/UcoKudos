# ADR-0014: OpenTelemetry, logs estructurados y auditoría en base de datos

- **Estado:** Propuesta (alcance reducido: solo existe la auditoría en base de datos)
- **Fecha:** 2026-11-11
- **Drivers y restricciones:** DRV-006, DRV-005; RT-18, RT-19; RN-33; FS-03
- **ABB:** Observabilidad; Logging; Auditoría

## Contexto
La arquitectura debe incorporar monitoreo, métricas, trazas y auditoría. El flujo Publicar Kudo debe quedar auditado de punta a punta, y se necesita evidencia (RT-19). Observabilidad y auditoría tienen propósitos distintos: la primera es técnica y desechable; la segunda es un registro de negocio que no se puede alterar.

## Decisión

| Aspecto | Decisión |
|---|---|
| **Logs** | Estructurados (JSON) a la salida estándar (Twelve-Factor); sin datos sensibles; con **identificador de correlación** en cada línea |
| **Métricas** | Métricas técnicas de la aplicación (Actuator) y de negocio: Kudos publicados, notificaciones por canal y estado |
| **Trazas** | Trazas del flujo Publicar Kudo con **OpenTelemetry**, enviadas a una plataforma de observabilidad (candidato: Azure Monitor, Application Insights) |
| **Auditoría** | Tabla propia en la base de datos, **solo de inserción** (el usuario de la aplicación no tiene permiso de actualizar ni borrar). Registra quién, qué, cuándo, sobre qué y el resultado |

- **Qué se audita:** publicar Kudo y cada notificación enviada o fallida, registro y aprobaciones, invitaciones, cambios de rol o de titular, vetos, descargas de adjuntos, intentos de acceso denegados.
- La consulta de la auditoría es solo para el Administrador (FS-03).

## Alternativas consideradas

| Opción | Costos | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| **OpenTelemetry + plataforma de la nube; auditoría en la base de datos** | Por verificar (hay cuota gratuita) | Media | Alta | Bajo | Alta | Alta: OpenTelemetry es neutral |
| Prometheus y Grafana propios | Medio (hay que alojarlos) | Alta | Alta | Medio | Media | Alta |
| Solo logs | Bajo | Baja | Baja | Alto: no hay métricas ni trazas | Media | Baja |
| Auditoría solo en logs | Bajo | Baja | Media | Alto: los logs se rotan y no son evidencia de negocio | Baja | Baja |

## Consecuencias
- **Positivas:** una sola traza explica el flujo completo; la auditoría persiste y es consultable; el estándar evita atarse a un proveedor.
- **Negativas:** la auditoría crece con el uso; hay que definir su retención.
- **Riesgos:** registrar datos personales en logs; se evita con una lista de campos prohibidos y pruebas.

## Costos con precio oficial
Consulta del 16 de noviembre de 2026 (ver [precios-oficiales.md](precios-oficiales.md)).

| Opción | Precio oficial | Estimado mensual |
|---|---|---|
| **Log Analytics, Central US** (ya existe `log-ucokudos`) | Ingesta: **primeros 5 GB gratis; después 2,76 USD por GB** | 0 USD con el volumen actual |
| Application Insights | **No verificado** (no se consultó; se factura por ingesta en un espacio de Log Analytics) | No verificado |
| Prometheus y Grafana propios | Costo de un contenedor adicional (ver [ADR-0013](0013-despliegue.md)) | Unos 6 a 20 USD |
| Solo logs | Sin costo adicional | 0 USD |

## Registro de cambios
- 2026-11-11: creación como Propuesta.
- 2026-11-16: **alcance reducido y estado real.** Construido: la **tabla de auditoría** (`audit_record`) con acciones de publicar Kudo, notificaciones, actualizar y verificar el celular, y el endpoint de salud de Actuator. Los registros de los contenedores llegan al espacio de Log Analytics.
- 2026-11-16: **no construido** y es un hallazgo de prioridad alta: registros en JSON con identificador de correlación, métricas de negocio, trazas con OpenTelemetry y la consulta de auditoría para el Administrador (FS-03). Tampoco se audita todavía la invitación, el alta ni la descarga de adjuntos. La restricción de "solo inserción" para el usuario de la base de datos **no está verificada**.
