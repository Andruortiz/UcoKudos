# ADR-0019: Front-end con React, TypeScript y Vite, mobile first

- **Estado:** Aceptada
- **Fecha:** 2026-11-11
- **Drivers y restricciones:** DRV-001, DRV-005, DRV-007; RT-04 (repositorio de front-end separado), RT-16 (mobile first), RT-12 (URL pública); FS-04, FS-05, FS-19
- **ABB:** Experiencia de usuario; Autenticación (cliente)

## Contexto
El 90% de los usuarios entra desde el móvil, así que la interfaz debe ser ligera y pensada primero para pantallas pequeñas. El front-end vive en su propio repositorio y solo habla con la API. Debe iniciar sesión con Microsoft Entra (ADR-0003), recibir avisos en tiempo real por SSE (ADR-0007) y soportar **enlaces profundos**: el aviso de WhatsApp lleva al detalle de un Kudo, exige sesión y devuelve al usuario a ese Kudo después de iniciar sesión. Lo desarrolla una sola persona, con poco tiempo, y el conocimiento previo del equipo está en Java; **no se sabe qué framework de front-end domina el autor**.

## Decisión
- **React con TypeScript**, empaquetado con **Vite**, como aplicación de una sola página (SPA).
- **Mobile first:** estilos pensados primero para pantallas pequeñas, interfaz ligera y carga por rutas.
- **Inicio de sesión:** biblioteca oficial de Microsoft para el navegador (MSAL), con OpenID Connect y PKCE.
- **Rutas protegidas** con retorno: `/kudos/:id` redirige al inicio de sesión y vuelve al mismo Kudo.
- **Tiempo real:** `EventSource` nativo del navegador para SSE.
- **Pruebas:** unitarias y de componentes con Vitest y Testing Library; extremo a extremo con Playwright.
- **Alojamiento:** sitio estático (ADR-0013); la compresión y las cabeceras se configuran en el alojamiento.
- **Criterio que puede cambiar esta decisión:** si el autor ya domina Angular o Vue, se usa ese. Cualquiera de los tres cumple los requisitos; lo que pesa es la velocidad con la que el autor lo explique y modifique en la sustentación (RT-01).

## Alternativas consideradas

| Opción | Costos | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| **React + TypeScript + Vite** | Bajo | Media | Alta | Bajo: gran cantidad de ejemplos y bibliotecas; hay que elegir cada pieza | Alta | Alta |
| Angular | Bajo | Alta: más estructura y conceptos | Alta | Medio: curva de aprendizaje mayor para el plazo | Alta | Alta |
| Vue | Bajo | Baja-media | Alta | Medio: menos ejemplos de integración con MSAL | Alta | Alta |
| HTML y JavaScript sin framework | Bajo | Baja al inicio, alta al crecer | Baja | Alto: el enrutado, el estado y las pruebas quedan a mano | Baja | Baja |
| Aplicación nativa móvil | Alto | Alta | Media | Alto: fuera del alcance y del plazo | Media | Media |

## Consecuencias
- **Positivas:** ecosistema amplio; biblioteca de Microsoft disponible para el inicio de sesión; SPA estática barata de alojar; pruebas y análisis de Sonar y Trivy estándar sobre el repositorio de front-end.
- **Negativas:** al ser una SPA, la primera carga descarga el paquete completo; hay que cuidar su tamaño por los datos móviles.
- **Riesgos:** la decisión se basa en criterios generales, no en el conocimiento del autor. Debe confirmarse antes de crear el repositorio.
- **Pendiente:** confirmar el framework; elegir el sistema de estilos (CSS propio o una biblioteca de utilidades); conectar el repositorio de front-end a Sonar, Trivy y al pipeline (ADR-0018).

## Costos con precio oficial
Consulta del 16 de noviembre de 2026 (ver [precios-oficiales.md](precios-oficiales.md)). React, TypeScript, Vite, MSAL, Vitest y Testing Library son de código abierto, sin licencia. El alojamiento es **Azure Static Web Apps, plan Free: 0 USD** (el plan Standard cuesta **9 USD al mes**). Angular, Vue y la aplicación nativa: sin cifra obtenida; **No verificado** el costo de desarrollo de la aplicación nativa.

## Registro de cambios
- 2026-11-11: creación como Propuesta, con el framework sin confirmar.
- 2026-11-16: pasa a **Aceptada.** El front se construyó con **React, TypeScript y Vite** como SPA, con inicio de sesión con MSAL, rutas protegidas con retorno y pruebas con Vitest y Testing Library (43 pruebas). Está publicado en `https://kudos.ucokudos.qd.je`.
- 2026-11-16: **desviaciones y decisiones tomadas al construir:**
  - **Tiempo real:** se usa `fetch` con lectura del flujo y no `EventSource`, para poder enviar la cabecera `Authorization` (ver [ADR-0007](0007-push-tiempo-real.md)).
  - **Estilos:** CSS propio en un solo archivo con variables, sin biblioteca de utilidades. El diseño se rehízo en una dirección limpia con barra de navegación superior, listas con líneas finas y un solo azul de acento.
  - **Alta de usuario:** la aplicación web registra sola al usuario en su primer inicio de sesión (ver [ADR-0022](0022-alta-automatica-con-invitacion.md)).
  - **Adjuntos:** descarga de cada archivo desde el detalle del Kudo y botón para quitar un archivo antes de enviar.
  - La API se llamará por el **gateway con WAF** ([ADR-0021](0021-gateway-y-waf-con-modsecurity.md)): el cambio de URL está hecho en el pipeline del front y **falta desplegarlo**.
- 2026-11-16: **no construido:** las pruebas de extremo a extremo con Playwright.
