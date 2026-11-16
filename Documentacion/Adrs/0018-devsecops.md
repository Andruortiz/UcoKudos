# ADR-0018: DevSecOps con GitHub Actions, Sonar y Trivy

- **Estado:** Aceptada, con implementación parcial
- **Fecha:** 2026-11-11
- **Drivers y restricciones:** DRV-007; RT-08, RT-09, RT-10, RT-11, RT-19
- **ABB:** DevSecOps; Seguridad

## Contexto
El enunciado exige integración continua, despliegue automatizado, análisis estático con **Sonar** y escaneo de vulnerabilidades con **Trivy**, además de evidencias de cada uno. Hay dos repositorios (front-end y back-end), previstos en GitHub.

## Decisión
- **Pipeline en GitHub Actions** por repositorio.
- **CI (back-end):** compilar, pruebas unitarias, pruebas de arquitectura (ArchUnit), pruebas de integración con base de datos real, cobertura, análisis con **SonarCloud** y construcción de la imagen.
- **Seguridad:** **Trivy** sobre las dependencias y sobre la imagen; el pipeline falla con vulnerabilidades críticas no aceptadas.
- **CD:** al integrar a la rama principal y pasar los controles, se publica la imagen y se despliega (ADR-0013).
- Secretos del pipeline en los secretos del repositorio; ninguno en el código.
- Pruebas end-to-end del flujo Publicar Kudo en un paso del pipeline, o en una ejecución aparte si el tiempo no alcanza.
- Cada ejecución produce **evidencia**: la captura o el enlace a la ejecución.

## Alternativas consideradas

| Opción | Costos | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| **GitHub Actions** | Bajo (por verificar los minutos gratuitos) | Baja | Alta | Bajo | Alta | Alta |
| Azure DevOps Pipelines | Por verificar | Media | Alta | Medio: otra plataforma además de GitHub | Media | Alta |
| GitLab CI | Por verificar | Media | Alta | Medio: obliga a mover los repositorios | Media | Alta |
| Scripts manuales | Nulo | Baja | Baja | Alto: incumple RT-09 | Baja | Baja |

## Consecuencias
- **Positivas:** todo automatizable y verificable; evidencia generada por cada ejecución; el repositorio y el pipeline están en el mismo lugar.
- **Negativas:** los minutos gratuitos pueden agotarse con ejecuciones frecuentes.
- **Pendiente:** comprobar que Sonar y Trivy aceptan Java 27 (ADR-0002); conectar SonarCloud a ambos repositorios.

## Costos con precio oficial
GitHub Actions, SonarCloud y Trivy: **No verificado.** No se obtuvo el precio oficial ni la cuota de minutos gratuitos de GitHub Actions, ni el plan de SonarCloud; consultar las páginas de precios de GitHub y de SonarSource. Trivy es de código abierto. Azure DevOps Pipelines y GitLab CI: **No verificado.**

## Registro de cambios
- 2026-11-11: creación como Propuesta.
- 2026-11-16: pasa a **Aceptada, con implementación parcial.** Construido: pipeline en GitHub Actions por repositorio, compilación y pruebas, cobertura con JaCoCo, análisis con SonarCloud en el back-end, Trivy sobre el código en ambos repositorios y sobre la imagen en el back-end, y publicación de la imagen en GitHub Container Registry.
- 2026-11-16: **desviaciones declaradas:**
  - **No hay pruebas de arquitectura (ArchUnit)** ni **pruebas de extremo a extremo (Playwright)** en el pipeline.
  - Las pruebas de integración usan **H2**, no una base real; la comprobación de las migraciones sobre PostgreSQL no corre en el CI.
  - El **CD del back-end no despliega**: publica la imagen y el despliegue en Azure es manual (ver [ADR-0013](0013-despliegue.md)). El CD del front-end sí despliega solo.
  - Sonar y Trivy aceptan Java 27 (el CI pasa), con lo que se cierra la verificación pendiente del [ADR-0002](0002-lenguaje-y-framework.md).
- 2026-11-16: **pruebas de arquitectura (ArchUnit) construidas** (ver [ADR-0001](0001-arquitectura-hexagonal.md)). Corren con `mvn test`, así que forman parte del CI sin un paso adicional. La desviación "no hay pruebas de arquitectura" de este ADR queda cerrada; siguen pendientes las pruebas de extremo a extremo.
