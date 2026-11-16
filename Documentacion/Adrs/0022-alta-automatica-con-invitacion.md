# ADR-0022: Alta automática con invitación de Entra, sin aprobación jerárquica

- **Estado:** Aceptada
- **Fecha:** 2026-11-16
- **Modifica parcialmente a:** [ADR-0003](0003-identidad-y-autenticacion.md) y [ADR-0004](0004-autorizacion-y-roles.md) en el paso de **aprobación del superior**. El resto de ambos sigue vigente
- **Drivers y restricciones:** DRV-005; RT-18, RT-25; RN-08, RN-09; FS-05, FS-06
- **ABB:** Gestión de identidad; Autenticación; Autorización

## Contexto
Los ADR-0003 y 0004 describen un alta con **solicitud, aprobación del superior, invitación, aceptación, verificación del celular y activación**. La aprobación jerárquica exige la estructura académica, los cargos únicos y la tabla de aprobaciones (FS-07, FS-08, FS-09, FS-16, FS-17), que **no se construyeron**. Sin ellas, nadie podía aprobar, y el docente evaluador no habría podido probar el flujo Publicar Kudo.

Al probar el alta real aparecieron además dos problemas: el usuario institucional no existía en el directorio propio (error de Entra *user not in tenant*, AADSTS50020), y el directorio exigía asignación previa (AADSTS50105).

## Decisión
- **Pedir acceso:** el usuario escribe su correo institucional en una página pública. La API valida el dominio contra la **lista de dominios permitidos** (ADR-0017), limita la frecuencia de solicitudes y **crea la invitación con Microsoft Graph** (`POST /invitations`), con credenciales de aplicación y el permiso mínimo `User.Invite.All`. **Microsoft envía la invitación** al correo.
- **Primer inicio de sesión:** el usuario acepta la invitación e inicia sesión con Microsoft. Si el token es válido pero no hay usuario propio, el usuario queda con la autoridad `UNREGISTERED` y la aplicación web llama sola al registro. La API crea el usuario **activo**, con el **correo verificado** (lo prueba el token de Entra) y el **celular sin verificar**.
- **Rol inicial:** `TEACHER` si el correo termina en `@uco.edu.co`; `STUDENT` en los demás casos. Es una regla provisional. El resto de roles se asignarían con la gestión de usuarios (FS-15), que no existe.
- **Celular:** el usuario lo registra y lo verifica en su perfil con SMS (ADR-0020). Sin celular verificado **no puede enviar Kudos**.
- **Entra:** la aplicación empresarial tiene "se requiere asignación" en **No**, para que los invitados entren sin asignarlos a mano.
- **El alta y la invitación todavía no se registran en la auditoría.** Es una carencia declarada (ver Consecuencias).

## Alternativas consideradas

| Opción | Costos (consulta 2026-11-16) | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| **Alta automática con invitación por Graph** | Sin costo adicional de infraestructura. Las invitaciones de Graph y los invitados B2B: **No verificado** (consultar la página de precios de Microsoft Entra) | Media | Alta | Medio: cualquiera con un correo de un dominio permitido puede activar su cuenta; se mitiga con la lista de dominios, la limitación de solicitudes y el celular verificado | Alta | Alta: se agrega la aprobación sin cambiar la invitación |
| Aprobación jerárquica antes de invitar (ADR-0003) | Sin costo adicional | Alta: exige las funcionalidades FS-07 a FS-17 | Alta | Bajo en seguridad; **alto en plazo**: no se construyó | Media | Alta |
| Invitación manual en el portal de Entra | Sin costo adicional | Baja | Baja: una persona por cada alta | Medio: depende de que alguien con acceso al portal invite a cada evaluador | Baja | Baja |
| Registro abierto sin invitación | Sin costo adicional | Baja | Alta | Alto: el directorio es de un solo inquilino y exige que el usuario exista o sea invitado | Media | Baja |

## Consecuencias
- **Positivas:** el docente evaluador puede entrar y probar el flujo de principio a fin sin intervención del autor; cumple RN-08 y RN-09 con el correo verificado por el token y el celular por SMS.
- **Negativas:** no se cumple la regla central del enunciado de que cada alta la apruebe el superior; el rol se decide por el dominio del correo; el nombre del usuario puede quedar como el correo si Entra no entrega el nombre.
- **Riesgos:** el permiso `User.Invite.All` de aplicación permite invitar a cualquier correo si se abusa del endpoint público; por eso la API valida el dominio y limita la frecuencia. Un token de invitado puede no traer el correo en la reclamación `email`; la API lo lee también de `preferred_username` sin el sufijo `#EXT#`.
- **Pendiente:** auditar la invitación y el alta; comprobar de extremo a extremo con un correo institucional real que la invitación llegue y que el alta termine, y decidir si se agrega la aprobación jerárquica como evolución.

## Registro de cambios
- 2026-11-16: creación. Registra una decisión que el código ya había tomado sin ADR (el alta automática) y que contradecía el flujo de aprobación de los ADR-0003 y 0004.
