# ADR-0015: Estrategia de lectura: paginación, índices y caché

- **Estado:** Propuesta (implementación parcial)
- **Fecha:** 2026-11-11
- **Drivers y restricciones:** DRV-001, DRV-002; RT-16, RT-17; FS-04, FS-11, FS-19
- **ABB:** Persistencia; Rendimiento de lectura

## Contexto
La carga es predominantemente de lectura y el 90% de los usuarios entra desde el móvil. Hay que responder bien aunque la mayoría de las operaciones sean consultas, y poder crecer en lectura sin crecer en escritura.

## Decisión
- **Paginación por cursor** (posición del último elemento visto) en el feed y en las listas, en lugar de por número de página, para que el costo no crezca con la profundidad.
- **Índices** pensados para las consultas del feed (por fecha y categoría), de los Kudos recibidos y enviados, y del árbol de réplicas (ADR-0012).
- **Respuestas compactas**: solo los campos que la pantalla necesita; compresión HTTP de las respuestas (SUP-05).
- **Caché** para datos de baja volatilidad (categorías, parámetros) y páginas del feed con vida corta, detrás de un puerto de caché.
- **Fase inicial:** caché en memoria del propio servicio. **Evolución** a un almacén de caché compartido cuando haya varias instancias; por eso el diagrama de contenedores muestra la caché como bloque separado.
- Evolución adicional: réplica de lectura de la base de datos y miniaturas de imágenes.
- No hay CDN en la entrega (ADR-0016).

## Alternativas consideradas

| Opción | Costos | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| **Cursor + índices + caché en memoria detrás de un puerto** | Bajo | Media | Media-alta | Bajo | Alta | Alta |
| Paginación por número de página | Bajo | Baja | Media: se degrada con páginas profundas | Medio | Alta | Media |
| Caché compartida desde el inicio | Por verificar | Media-alta | Alta | Medio: otra pieza que operar | Media | Alta |
| Sin caché | Bajo | Baja | Baja | Medio | Alta | Media |

## Consecuencias
- **Positivas:** respuestas rápidas y estables bajo carga; poco costo inicial; el puerto permite cambiar de caché.
- **Negativas:** con la caché en memoria, cada instancia tiene la suya, así que pueden mostrar datos ligeramente distintos hasta que expiren.
- **Riesgos:** datos obsoletos; se acota con vidas cortas y con invalidación al publicar.

## Costos con precio oficial
La estrategia inicial no agrega servicios: índices en PostgreSQL y caché en el propio proceso, **sin costo adicional**. Una caché compartida (por ejemplo, Azure Cache for Redis) y las réplicas de lectura de la base de datos: **No verificado** (no se consultó su precio oficial).

## Registro de cambios
- 2026-11-11: creación como Propuesta.
- 2026-11-16: **implementación parcial.** Construido: índices por autor y fecha, y por destinatario y fecha en la tabla de Kudos, e índice de los adjuntos por Kudo. **No construido:** la paginación por cursor (hoy las listas devuelven una página fija de 50 elementos), la caché y la compresión HTTP de las respuestas (no hay configuración de compresión en la aplicación). No se encontró en el front la reducción de imágenes en el navegador.
