# ADR-0001: Clean Architecture implementada como hexagonal, en un solo servicio

- **Estado:** Aceptada
- **Fecha:** 2026-11-11
- **Drivers y restricciones:** DRV-007; RT-01 (un solo autor), RT-05 (Clean Architecture), RT-22 (un único servicio)
- **ABB:** Arquitectura de la solución (Clean Architecture)

## Contexto
El enunciado exige demostrar Clean Architecture, explicando capas, dependencias, responsabilidades y reglas. El proyecto lo desarrolla una sola persona, que debe poder explicarlo y modificarlo en la sustentación. El flujo Publicar Kudo coordina cinco efectos (guardar, correo, WhatsApp, push, auditoría) y varios proveedores externos que pueden cambiar.

## Decisión
Se construye **un solo servicio desplegable, "UCO Kudos"**, con arquitectura hexagonal (puertos y adaptadores), que es una forma de aplicar Clean Architecture:

| Capa | Contenido | Puede depender de |
|---|---|---|
| `domain` | Entidades y reglas de negocio | Nada |
| `application` | Casos de uso y puertos (interfaces) | `domain` |
| `infrastructure` | Adaptadores de entrada (REST) y de salida (base de datos, correo, WhatsApp, almacenamiento) | `application` y `domain` |

- Regla de dependencia: siempre hacia adentro. El dominio no conoce Spring ni la base de datos.
- Las reglas se verifican con **pruebas de arquitectura (ArchUnit)** en el CI.
- Paquete base: `co.edu.uco.ucokudos`.

## Alternativas consideradas

| Opción | Costos | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| **Hexagonal en un servicio** | Bajo | Media | Media (escala el servicio completo) | Bajo | Alta | Alta: se puede separar después |
| Capas clásicas (controlador, servicio, repositorio) | Bajo | Baja | Media | Medio: el dominio se acopla a la persistencia | Media | Media |
| Microservicios | Alto | Alta | Alta | Alto para un solo autor y cuatro días | Media | Alta |

## Consecuencias
- **Positivas:** el dominio se prueba sin infraestructura; los proveedores externos se cambian sin tocar el negocio; cumple RT-05 con evidencia verificable.
- **Negativas:** más interfaces y mapeos que en capas clásicas.
- **Riesgos:** si se escribe lógica de negocio en los adaptadores, se pierde el beneficio. Lo previenen las pruebas de arquitectura.

## Costos
Esta decisión es de estructura del código y no tiene un precio asociado: Spring y las herramientas de prueba son de código abierto.

## Registro de cambios
- 2026-11-11: creación como Aceptada.
- 2026-11-16: contraste con el código. La decisión se mantiene; estas son las **desviaciones declaradas**:
  - La capa de casos de uso se llama `service` en el código y no `application`.
  - **No existen las pruebas de arquitectura con ArchUnit** que este ADR promete: la dependencia no está en `../../pom.xml`. Es la única verificación automática de la regla de dependencia.
  - Los casos de uso importan `@Transactional` de Spring, así que la capa `service` no depende solo de `domain`.
  - `PushController` y `MessageCatalogRefreshScheduler` dependen directamente de un adaptador de salida.
- 2026-11-16: **pruebas de arquitectura construidas.** Se agregó ArchUnit 1.4.1 y la clase `HexagonalArchitectureTest` con 11 reglas que corren con `mvn test` y, por tanto, en el CI:
  - La dependencia apunta hacia adentro (arquitectura por capas: dominio, servicio, adaptadores de entrada, adaptadores de salida y arranque).
  - El dominio no conoce las demás capas ni usa Spring, Jakarta, Hibernate, Jackson ni MapStruct.
  - El servicio no conoce los adaptadores ni el arranque, y solo usa de los marcos las anotaciones de transacción y de validación.
  - Los adaptadores de entrada no llaman a los de salida, y viceversa.
  - Los controladores hablan con puertos de entrada y no con casos de uso.
  - Los puertos son interfaces, los `*UseCase` viven en el servicio y los adaptadores de los puertos de salida viven en `infrastructure.output`.
- 2026-11-16: **excepciones declaradas en las reglas.** `PushController` depende de `PushNotificationSender` y `MessageCatalogRefreshScheduler` de `RemoteMessageSource`, ambos adaptadores de salida. Se mantienen como excepciones explícitas en la prueba y no se arreglan con un puerto nuevo porque el flujo SSE devuelve un tipo de Spring Web que no debe entrar en el servicio.
- 2026-11-16: **la corrección de la regla de dependencia queda comprobada.** Con el servicio, el dominio y los adaptadores, las 11 reglas pasan. Además, se comprobó que detectan: una clase de prueba temporal en el dominio con una anotación de Spring hizo fallar la regla "dominio sin marcos". Esto cierra la desviación que este ADR tenía declarada sobre ArchUnit; la desviación del nombre de la capa (`service` y no `application`) y la de `@Transactional` siguen vigentes, y la regla del servicio ya las admite.
