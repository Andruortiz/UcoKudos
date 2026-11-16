# ADR-0013: Despliegue en contenedores sobre Azure

- **Estado:** Aceptada
- **Fecha:** 2026-11-11
- **Drivers y restricciones:** DRV-007; RT-07, RT-09, RT-12, RT-27
- **ABB:** Despliegue y ejecución; Gestión de configuración

## Contexto
Debe existir una **URL pública** donde el docente use el flujo Publicar Kudo (RT-12). El plazo es de cuatro días y el presupuesto es el de una cuenta de estudiante. El servicio es único y sin estado (RT-07). La aplicación web es estática.

## Decisión
- **API:** imagen de contenedor ejecutada en un servicio de contenedores gestionado (candidato: Azure Container Apps). Durante la demostración se mantiene **al menos 1 réplica activa**, porque escalar a cero provoca arranques lentos en Spring Boot.
- **Aplicación web:** servicio de alojamiento de sitios estáticos (candidato: Azure Static Web Apps, plan gratuito).
- Cada entorno se configura por **variables de entorno** (ADR-0017); la misma imagen sirve para todos.
- **Plan de contingencia:** si Java 27 falla en la imagen base, usar Java 25 (ADR-0002).
- Datos verificados en la documentación de Microsoft: el plan gratuito de Static Web Apps incluye 100 GB de ancho de banda al mes, SSL y dominios personalizados, y **no tiene SLA**; Azure for Students ofrece 100 USD de crédito por 12 meses, con renovación manual.

## Alternativas consideradas

| Opción | Costos | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| **Servicio de contenedores gestionado** | Por verificar (hay una cuota gratuita) | Media | Alta | Medio: arranque en frío si se escala a cero | Alta | Alta |
| Servicio de aplicaciones (App Service) | Por verificar | Baja | Media-alta | Medio | Alta | Media |
| Máquina virtual | Medio | Alta (todo manual) | Media | Alto | Baja | Baja |
| Otra nube o hosting externo | Por verificar | Media | Media | Medio: otro proveedor y credenciales | Media | Media |

## Consecuencias
- **Positivas:** paridad entre entornos (Twelve-Factor); CD simple con una imagen; elasticidad.
- **Negativas:** el plan gratuito del sitio estático no tiene SLA, lo que debe anotarse como riesgo frente a la disponibilidad.
- **Pendiente:** confirmar la suscripción y los costos de la base de datos y de los servicios de soporte; elegir región; decidir si PostgreSQL es gestionado (ADR-0005).

## Costos con precio oficial
Consulta del 16 de noviembre de 2026 en la API de precios de Azure (ver [precios-oficiales.md](precios-oficiales.md)). Los totales son **cálculo del autor** con 730 horas al mes.

| Opción | Precio oficial | Estimado mensual |
|---|---|---|
| **Azure Container Apps (consumo), Central US: API de 1 vCPU y 2 GiB, una réplica** | vCPU en uso **0,000024 USD por segundo**; vCPU en reposo **0,000003 USD por segundo**; memoria **0,000003 USD por GiB y segundo**; solicitudes **0,40 USD por millón** | **Unos 24 USD si casi siempre está en reposo y hasta unos 79 USD si estuviera siempre activa** |
| Gateway y WAF, Container App de 0,25 vCPU y 0,5 GiB | Los mismos medidores | Unos 6 USD en reposo y hasta unos 20 USD activo |
| Static Web Apps, plan **Free** | Sin medidores en la API de precios | **0 USD** |
| Static Web Apps, plan Standard | **9 USD al mes** por aplicación | 9 USD |
| PostgreSQL Flexible Server B1ms | Ver [ADR-0005](0005-base-de-datos.md) | 18,18 USD |
| App Service | **No verificado** (no se consultó) | No verificado |
| Máquina virtual | **No verificado** (no se consultó) | No verificado |
| **Total estimado de la entrega** | | **Entre unos 48 y unos 117 USD al mes**, sin contar Blob ni Defender (centavos) |

**Cuota gratuita de Container Apps:** la documentación de Azure la describe, pero **no figura en la API de precios**: **No verificado**. Con el crédito de 100 USD de Azure for Students por 12 meses, **el gasto mensual consumiría el crédito en pocas semanas** en el escenario alto: hay que apagar o reducir los recursos después de la sustentación.

## Registro de cambios
- 2026-11-11: creación como Propuesta.
- 2026-11-16: pasa a **Aceptada.** Desplegado: la API en `ca-ucokudos-api` (Azure Container Apps, Central US, 1 vCPU, 2 GiB, una réplica fija), el sitio estático en `swa-ucokudos-web` (plan Free) con el dominio `kudos.ucokudos.qd.je`, la base de datos y el Key Vault. La imagen se publica en GitHub Container Registry con el SHA completo del commit como etiqueta.
- 2026-11-16: **diferencia declarada.** El CD del back-end **publica la imagen pero no la despliega**: el paso a Azure se hace a mano con `az containerapp update`. El CD del front-end sí despliega solo.
- 2026-11-16: se agregaron al despliegue un segundo Container App, el gateway con WAF ([ADR-0021](0021-gateway-y-waf-con-modsecurity.md)), y una cuenta de Blob Storage ([ADR-0011](0011-adjuntos.md)). La región de la cuenta de Blob (East US 2) **difiere** de la del Container App (Central US).
- 2026-11-16: no hizo falta el plan de contingencia a Java 25 (ver [ADR-0002](0002-lenguaje-y-framework.md)).
