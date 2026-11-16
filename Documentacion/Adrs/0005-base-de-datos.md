# ADR-0005: Base de datos relacional PostgreSQL con migraciones Flyway

- **Estado:** Aceptada
- **Fecha:** 2026-11-11
- **Drivers y restricciones:** DRV-002; RT-08, RT-17, RT-20
- **ABB:** Persistencia

## Contexto
Los datos son relacionales y con restricciones fuertes (unicidad de cargos, una calificación activa por usuario y contenido, árbol de réplicas). La carga es de lectura (DRV-002). El contenido de Kudos y réplicas no tiene límite de longitud (RT-20). El `../../pom.xml` ya incluye el driver de PostgreSQL.

## Decisión
- **PostgreSQL** como base de datos relacional.
- **Flyway** para migraciones versionadas del esquema, ejecutadas en el despliegue.
- Texto largo en columnas `text`; índices pensados para las consultas del feed y del árbol (ADR-0012, ADR-0015).
- Las pruebas de integración usan una base real con **Testcontainers**.
- Si se necesita más lectura, evolución: réplica de lectura.

## Alternativas consideradas

| Opción | Costos | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| **PostgreSQL** | Por verificar (hay servicio gestionado y contenedor) | Baja | Alta | Bajo | Alta | Alta |
| Azure SQL | Por verificar. La documentación de Microsoft indica que su oferta gratuita es incompatible con la suscripción Azure for Students | Media: otro driver y dialecto | Alta | Medio | Media | Media |
| MySQL | Por verificar | Baja | Media | Medio | Media | Media |
| Base documental (NoSQL) | Por verificar | Media | Alta | Alto: pierde restricciones relacionales | Media | Media |

## Consecuencias
- **Positivas:** no cambia el driver ya presente; restricciones y transacciones nativas; consulta recursiva para el árbol de réplicas.
- **Negativas:** la base gestionada puede costar; hay que verificarlo en el portal.
- **Pendiente:** decidir dónde se aloja (servicio gestionado o contenedor) en el ADR-0013, y confirmar los límites de coste.

## Costos con precio oficial
Consulta del 16 de noviembre de 2026 en la API de precios de Azure (ver [precios-oficiales.md](precios-oficiales.md)).

| Opción | Precio oficial | Estimado mensual |
|---|---|---|
| **PostgreSQL Flexible Server, B1ms, Central US** | Cómputo **0,01921 USD por hora**; almacenamiento **0,13 USD por GB al mes** | 14,02 USD de cómputo y 4,16 USD por 32 GB: **unos 18,18 USD al mes** (cálculo del autor, 730 horas) |
| Azure SQL | **No verificado** (no se consultó); la documentación de Microsoft indica que su oferta gratuita no es compatible con Azure for Students | No verificado |
| MySQL | **No verificado** (no se consultó) | No verificado |
| Base documental | **No verificado** (no se consultó) | No verificado |

## Registro de cambios
- 2026-11-11: creación como Propuesta.
- 2026-11-16: pasa a **Aceptada.** Se usa PostgreSQL Flexible Server B1ms de 32 GB en Central US, sin alta disponibilidad, con migraciones Flyway V1 a V5 (esquema inicial, categorías, categorías de la guía, verificación del celular y adjuntos en Blob).
- 2026-11-16: **diferencia declarada.** Las pruebas de integración usan H2 y no Testcontainers como promete este ADR. Una prueba opcional (`FlywayMigrationsTest`) aplica las migraciones a un PostgreSQL real, pero solo corre si se define una variable de entorno y **no corre en el CI**.
