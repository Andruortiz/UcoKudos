# ADR-0004: Autorización por rol en la base de datos propia

- **Estado:** Aceptada, modificada parcialmente por [ADR-0022](0022-alta-automatica-con-invitacion.md) (el paso de aprobación del superior); implementación parcial
- **Fecha:** 2026-11-11
- **Drivers y restricciones:** DRV-005; RN-01 a RN-07, RN-10 a RN-22, RN-24, RN-31; FS-05, FS-07, FS-08, FS-15, FS-17
- **ABB:** Autorización; Seguridad

## Contexto
Hay 7 roles con jerarquía, un solo rol activo por persona (RN-03) y cargos únicos: un Rector, un Coordinador Académico, un Decano por facultad y un Coordinador por programa (RN-04 a RN-07). Cada solicitud la aprueba el rol superior. Un rol va ligado a una facultad o un programa. Entra autentica (ADR-0003), pero no conoce estas reglas.

## Decisión
- **Autenticación en Entra, autorización en UCO Kudos.** El token prueba quién es el usuario; el rol, el estado y el ámbito (facultad o programa) viven en la **base de datos propia**.
- Los permisos se validan **en el servidor**, en los casos de uso, y **se deniega por defecto**. Un usuario autenticado sin cuenta activa recibe un rechazo.
- Las reglas de unicidad se aplican con **restricciones de base de datos** (un titular activo por cargo) y con reglas de dominio. El **cambio de titular** (FS-17) desactiva al anterior y activa al nuevo en una sola transacción.
- La **tabla de aprobaciones** (quién aprueba a quién) es **dato de configuración**, no código.
- Nadie aprueba su propia solicitud (SUP-06).
- Supuestos: el docente lo aprueba el Coordinador de Programa (SUP-01); facultades y programas los crea el Administrador o el Coordinador Académico (SUP-02); las cuentas de demostración se siembran activas y verificadas (SUP-03).

## Alternativas consideradas

| Opción | Costos | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| **Roles en la base de datos propia** | Bajo | Media | Alta | Bajo | Alta | Alta |
| Roles de aplicación de Entra | Bajo | Media | Alta | Medio: no modela cargos únicos ni ámbito; dos fuentes de verdad | Media | Media |
| Roles dentro del token firmado | Bajo | Baja | Alta | Alto: un rol revocado sigue vigente hasta que el token expire | Baja | Baja |

## Consecuencias
- **Positivas:** reglas de negocio completas y verificables; el cambio de rol es inmediato; las pruebas no dependen de Entra.
- **Negativas:** hay que sincronizar la identidad de Entra con el usuario propio (identificador del token ↔ usuario).
- **Seguridad:** no confiar en el rol que llegue desde el cliente; auditar aprobaciones, rechazos y cambios de titular.

## Costos
La autorización vive en la base de datos propia y no agrega servicios: sin costo adicional.

## Registro de cambios
- 2026-11-11: creación como Aceptada.
- 2026-11-16: **implementación parcial.** Construido: roles, estado y rol en la base de datos propia, validación en el servidor con denegación por defecto, reglas de quién puede enviar a quién. **No construido:** la tabla de aprobaciones, las restricciones de cargos únicos, el cambio de titular y la gestión de usuarios (FS-07, FS-08, FS-15, FS-17). La aprobación del superior fue reemplazada por el alta automática del [ADR-0022](0022-alta-automatica-con-invitacion.md).
- 2026-11-16: **regla nueva.** Un usuario solo puede enviar Kudos con el correo y el celular verificados (código `kudo.sender.not.verified`, respuesta 403).
