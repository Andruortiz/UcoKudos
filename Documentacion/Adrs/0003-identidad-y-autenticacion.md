# ADR-0003: Directorio propio en Microsoft Entra con usuarios institucionales invitados

- **Estado:** Aceptada, modificada parcialmente por [ADR-0022](0022-alta-automatica-con-invitacion.md) (el paso de aprobación del superior)
- **Fecha:** 2026-11-11
- **Drivers y restricciones:** DRV-005; RT-18, RT-25; RN-08, RN-09; FS-05, FS-06
- **ABB:** Gestión de identidad; Autenticación

## Contexto
Todo usuario se registra con **correo institucional** y celular, y la cuenta solo se activa con solicitud aprobada, correo verificado y celular verificado. El proyecto no tiene acceso de administrador al directorio de la UCO ni dominio propio. La UCO usa Microsoft: las cuentas `@uco.net.co` existen en su directorio. Se comprobó que un usuario de la UCO puede entrar como **invitado** a un directorio propio, creado por el estudiante.

## Decisión
- Se usa un **directorio propio en Microsoft Entra**. Los usuarios institucionales entran como **invitados (B2B)** con su cuenta de la UCO.
- La aplicación web inicia sesión con **OpenID Connect** (código de autorización con PKCE). La API **valida el token** (firma, emisor, destinatario y expiración).
- **Flujo de alta:** solicitud en UCO Kudos → aprobación del superior (ADR-0004) → la API invita al usuario con **Microsoft Graph** y **Microsoft envía la invitación** a su buzón institucional → al aceptarla e iniciar sesión, el correo queda **verificado** → verificación del celular (ADR-0010) → cuenta activa.
- Las cuentas de demostración son **miembros** del directorio, sembrados como activos (SUP-03).
- Del token se lee el **correo** del usuario, no el nombre de usuario (los invitados llevan el formato `#EXT#`).
- **Plan B:** autenticación propia con tokens firmados por la API, detrás de un puerto de identidad.

## Alternativas consideradas

| Opción | Costos | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| **Directorio propio + invitados** | Bajo (por verificar el plan del directorio) | Media | Alta | Medio: depende de Microsoft y de que la UCO permita las invitaciones | Alta | Alta: se migra al directorio real de la UCO |
| Inicio de sesión con el directorio de la UCO | Bajo | Baja | Alta | Alto: requiere permisos que no se tienen | Alta | Alta |
| Autenticación propia (usuario y contraseña) | Bajo | Alta (seguridad a cargo del proyecto) | Media | Alto | Media | Media |
| Keycloak u otro servidor de identidad | Medio (hay que alojarlo) | Alta | Alta | Medio | Media | Alta |

## Consecuencias
- **Positivas:** correo institucional real y verificado sin proveedor de correo propio para ese paso; las contraseñas las gestiona la UCO; evita construir autenticación.
- **Negativas:** cada usuario debe ser invitado y aceptar; no sirve para registro abierto sin aprobación.
- **Seguridad:** permitir solo el dominio institucional (lista configurable, ADR-0017); conceder a la aplicación el **mínimo permiso** de Graph para invitar; guardar su secreto en el gestor de secretos; registrar en la auditoría la invitación y su aceptación.
- **Por verificar:** que la UCO no restrinja las invitaciones salientes de sus usuarios para el docente evaluador; los límites del plan gratuito del directorio.

## Costos
Las invitaciones de Microsoft Graph y los usuarios invitados B2B: **No verificado.** Consultar la página oficial de precios de Microsoft Entra. El directorio propio se usa sin licencias de pago adicionales en este proyecto.

## Registro de cambios
- 2026-11-11: creación como Aceptada.
- 2026-11-16: **el flujo de alta cambió.** La aprobación del superior no se construyó y el alta es automática: la API invita por Graph y la cuenta se activa en el primer inicio de sesión. Las razones, las alternativas y los riesgos están en el [ADR-0022](0022-alta-automatica-con-invitacion.md). El resto de este ADR (directorio propio, invitados, OpenID Connect con PKCE, validación del token, lectura del correo sin `#EXT#`) sigue vigente.
- 2026-11-16: el **plan B** (autenticación propia) no se usó. La aplicación empresarial se configuró con "se requiere asignación" en **No**.
