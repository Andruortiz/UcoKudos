# ADR-0002: Java 27, Spring Boot y Spring Web MVC

- **Estado:** Aceptada (con una verificación pendiente)
- **Fecha:** 2026-11-11
- **Drivers y restricciones:** DRV-002, DRV-007; RT-01, RT-21
- **ABB:** Plataforma de ejecución

## Contexto
El equipo (una persona) conoce Java. El proyecto ya está creado con Spring Boot 4.1.2 y Java 27 (`../../pom.xml`). La carga es predominantemente de lectura (DRV-002) y el código debe ser explicable en vivo (RT-01).

## Decisión
- **Java 27** y **Spring Boot 4.1.2**.
- **Spring Web MVC** (modelo bloqueante) con **hilos virtuales** para sostener muchas conexiones simultáneas sin la complejidad de programación reactiva.
- Dependencias base ya presentes: actuator, data-jpa, security, validation, webmvc y el driver de PostgreSQL.

## Alternativas consideradas

| Opción | Costos | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| **Java 27 + MVC con hilos virtuales** | Bajo | Baja | Media-alta | Medio: Java 27 no es versión de soporte prolongado | Alta | Alta |
| Java 25 (versión de soporte prolongado) + MVC | Bajo | Baja | Media-alta | Bajo | Alta | Alta |
| Spring WebFlux (reactivo) | Bajo | Alta | Alta | Alto para una persona y poco tiempo | Media | Alta |

## Consecuencias
- **Positivas:** curva de aprendizaje mínima; código lineal y fácil de explicar.
- **Negativas:** se asume una versión de Java reciente, con menos ejemplos y herramientas validadas.
- **Por verificar:** que **Sonar, Trivy y la imagen base del contenedor** soporten Java 27. Si alguno falla, el plan de contingencia es **bajar a Java 25** sin cambiar el diseño (ADR-0013 y ADR-0018 lo recogen).
- Las pruebas de arquitectura (ArchUnit) mostraron una advertencia con Java 27 en un ensayo previo, pero pasaron.

## Costos
Java, Spring Boot y Spring Web MVC son de código abierto y no tienen costo de licencia. El costo de ejecución es el del contenedor (ADR-0013).

## Registro de cambios
- 2026-11-11: creación como Aceptada, con una verificación pendiente.
- 2026-11-16: **verificación cerrada.** El CI compila y prueba con Java 27 y la imagen base `eclipse-temurin:27` existe, así que no hizo falta el plan de contingencia a Java 25. JaCoCo se subió a la versión 0.8.16 por esa razón.
- 2026-11-16: **diferencia declarada.** Los hilos virtuales que este ADR menciona **no están activados** (`spring.threads.virtual.enabled` no aparece). Se activan con una línea de configuración o se retira la frase en una próxima versión.
- 2026-11-16: **ArchUnit 1.4.1 no puede leer clases compiladas con Java 27.** El importador falla con "Couldn't import class" y las reglas se quedan sin clases. Para no bajar el nivel del proyecto, el build compila además una **copia del código con nivel de bytecode 25** en `../../target/archunit-classes` (segunda ejecución de `maven-compiler-plugin`, sin procesadores de anotaciones), y las pruebas de arquitectura analizan esa copia. El código que se despliega sigue compilado con Java 27. Cuando ArchUnit soporte Java 27 se puede retirar esa ejecución.
