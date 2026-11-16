# Restricciones técnicas, reglas de negocio y funcionalidades de UCO Kudos

> Conversión del archivo `r-tecnicas-negocio-funcionalidades.xlsx` a Markdown, como pide la guía del trabajo. Es una conversión **literal** del contenido del 11 de noviembre de 2026: las columnas de estado y de tecnología pendiente de ADR **no se actualizaron** con lo construido después (por ejemplo, el cambio de Twilio a Vonage). El estado vigente de cada decisión está en el [índice de ADR](../04-adr/README.md) y en el [estado del proyecto](../00-estado-del-proyecto.md). El archivo original se conserva en la carpeta anterior.

## R-Tecnicas

| ID | Categoría | Restricción | Origen | Impacto en el diseño |
| --- | --- | --- | --- | --- |
| RT-01 | Plazo y modalidad | Trabajo individual con sustentación obligatoria, que puede incluir cambios de código en vivo. | Enunciado | Todo el código debe ser comprensible y explicable por una sola persona; se evita complejidad innecesaria. |
| RT-02 | Plazo y modalidad | Entrega y sistema desplegado el viernes 13 de noviembre de 2026 a las 12:00 m. | Enunciado | Alcance recortado: flujo Publicar Kudo completo; el resto se diseña y documenta. |
| RT-03 | Documentación | Toda la documentación en Markdown (.md). | Enunciado | Diagramas en texto (Mermaid o PlantUML), versionables en el repositorio. |
| RT-05 | Arquitectura | Clean Architecture con capas, dependencias, responsabilidades y reglas explicadas. | Enunciado | Dominio sin dependencias de frameworks; reglas verificables con pruebas de arquitectura. |
| RT-06 | Arquitectura | Análisis del Reactive Manifesto (responsividad, resiliencia, elasticidad, orientación a mensajes). | Enunciado | Se justifica con mensajería interna (Outbox), reintentos, timeouts y servicio sin estado. |
| RT-07 | Arquitectura | Matriz de cumplimiento de los 12 factores (Twelve-Factor App). | Enunciado | Configuración por entorno, procesos sin estado, logs a stdout, paridad entre entornos. |
| RT-08 | Calidad | Pruebas automatizadas: unitarias, de integración y end-to-end. | Enunciado | Puertos y adaptadores intercambiables; base de datos real para integración. |
| RT-12 | Despliegue | URL pública funcional, con el flujo de publicar Kudos usable por el docente. | Enunciado | Requiere despliegue en la nube y datos de demostración. |
| RT-13 | Funcional | Publicar Kudo debe: guardar, enviar correo, enviar WhatsApp, enviar push (si está conectado) y auditar. | Enunciado | Cinco efectos a coordinar; el guardado debe ser fiable aunque falle un canal externo. |
| RT-14 | Integraciones | Tres canales de notificación: correo, WhatsApp y push. | Enunciado | Un puerto por canal, para cambiar de proveedor sin tocar el dominio. |
| RT-15 | Datos | Los Kudos admiten archivos adjuntos. | Enunciado | Almacenamiento de archivos con seguridad, recuperación y trazabilidad. |
| RT-16 | Experiencia de usuario | Mobile First: se estima que el 90% de los usuarios entra desde móvil. | Enunciado | Front responsive y ligero; respuestas de API compactas y paginadas. |
| RT-17 | Rendimiento | Carga predominantemente de lectura. | Enunciado | Lecturas eficientes: índices, paginación y posible caché. |
| RT-20 | Datos | Contenido de Kudos y réplicas sin límite de longitud; réplicas sin límite de profundidad ni de cantidad. | Enunciado | Campos de texto largo y modelo de árbol sin profundidad máxima. |
| RT-25 | Integraciones | No se dispone de dominio propio para enviar correo ni del tenant institucional de Microsoft. | Entorno | Verificación de correo por enlace con token, sin inicio de sesión con cuenta institucional. |
| RT-26 | Pruebas | Los números de celular para pruebas son limitados (lista de destinatarios del sandbox). | Entorno | Usuarios de demostración sembrados con esos números. |
| RT-27 | Plazo | Cuatro días para diseño, construcción, despliegue y documentación. | Entorno | Prioridad: documentos que guían el diseño y el flujo Publicar Kudo funcionando. |
| Origen: Enunciado = exigido por el documento del proyecto; Entorno = limitación de herramientas, cuentas o plazo. |  |  |  |  |

## R-Negocio

| ID | Tema | Regla de negocio |
| --- | --- | --- |
| RN-01 | Roles | La plataforma soporta 7 roles: Administrador del Sistema, Rector, Coordinador Académico, Coordinador de Facultad (Decano), Coordinador de Programa, Docente y Estudiante. |
| RN-02 | Estructura | Jerarquía: Administrador > Rector > Coordinador Académico > Coordinador de Facultad > Coordinador de Programa > Docentes y Estudiantes. |
| RN-03 | Exclusividad | Cada persona puede tener únicamente un rol activo. |
| RN-04 | Unicidad | La Universidad tiene un solo Rector activo. |
| RN-05 | Unicidad | La Universidad tiene un solo Coordinador Académico activo. |
| RN-06 | Unicidad | Cada Facultad tiene un solo Coordinador de Facultad activo. |
| RN-07 | Unicidad | Cada Programa Académico tiene un solo Coordinador de Programa activo. |
| RN-08 | Registro | Todo usuario se registra con correo institucional y número celular. |
| RN-09 | Activación | La cuenta solo queda activa con solicitud aprobada, correo verificado y celular verificado. |
| RN-10 | Aprobaciones | La solicitud de un Estudiante la aprueba el Coordinador de Programa. |
| RN-11 | Aprobaciones | La solicitud de un Coordinador de Programa la aprueba el Coordinador de Facultad. |
| RN-12 | Aprobaciones | La solicitud de un Coordinador de Facultad la aprueba el Coordinador Académico. |
| RN-13 | Aprobaciones | La solicitud de un Coordinador Académico la aprueba el Rector. |
| RN-14 | Aprobaciones | La solicitud de un Rector la aprueba el Administrador. |
| RN-15 | Aprobaciones | La solicitud de un Administrador la aprueba otro Administrador. |
| RN-16 | Gestión académica | Un estudiante puede estudiar varios programas y debe poder consultarse cuáles. |
| RN-17 | Gestión académica | Un docente puede enseñar en múltiples programas y debe poder consultarse en cuáles. |
| RN-18 | Gestión académica | Un Coordinador de Programa tiene un programa asociado y administra los estudiantes y docentes de sus programas. |
| RN-19 | Gestión académica | Un Coordinador de Facultad tiene una facultad asociada y administra los Coordinadores de Programa de su facultad. |
| RN-20 | Gestión académica | El Coordinador Académico administra los Coordinadores de Facultad. |
| RN-21 | Gestión académica | El Rector administra los Coordinadores Académicos. |
| RN-22 | Gestión académica | Los Administradores administran Administradores y Rectores. |
| RN-23 | Categorías | Existen como mínimo 15 categorías de Kudos: Rendimiento Académico, Mejor Investigador, Empatía, Comunicación Asertiva, Orientación al Logro, Trabajo en Equipo, Trabajo Bajo Presión, Visión Estratégica, Innovación, Liderazgo, Transformación Digital, Mentor Inspirador, Espíritu UCO, Servicio a la Comunidad y Compromiso Institucional. |
| RN-24 | Categorías | Solo el Administrador, el Rector y el Coordinador Académico administran categorías; los demás roles no. |
| RN-25 | Kudos | Todo Kudo tiene autor, destinatario, categoría, fecha, contenido descriptivo y adjuntos. |
| RN-26 | Kudos | El contenido del Kudo no tiene límite de longitud. |
| RN-27 | Réplicas | Se puede replicar sobre Kudos y sobre otras réplicas, sin límite de profundidad, cantidad ni longitud. |
| RN-28 | Calificaciones | Los usuarios califican Kudos y réplicas de 1 a 5 estrellas. |
| RN-29 | Calificaciones | Un usuario tiene una sola calificación activa sobre el mismo contenido. |
| RN-30 | Moderación | Todos los usuarios pueden reportar contenido. |
| RN-31 | Moderación | Solo el Rector y el Coordinador Académico pueden vetar contenido. |
| RN-32 | Moderación | No pueden vetar: Administradores, Coordinadores de Facultad, Coordinadores de Programa, Docentes ni Estudiantes. |
| RN-33 | Publicación | Al publicar un Kudo: se guarda, el destinatario recibe correo y WhatsApp, recibe push si está conectado y todo el proceso queda auditado. |
| Fuente: enunciado del Proyecto Integrador Final (Ingeniería del Software II). |  |  |

## Funcionalidades

| ID | Funcionalidad significativa | Descripción | Reglas de negocio | Drivers | Atributos de calidad | Prioridad | Roles autorizados |
| --- | --- | --- | --- | --- | --- | --- | --- |
| FS-01 | Publicar Kudo | Guardar el Kudo y disparar correo, WhatsApp, push y auditoría. | RN-25, RN-26, RN-33 | DRV-003, DRV-006 | Disponibilidad, consistencia, trazabilidad | Obligatoria | Todos los roles (Sí) |
| FS-02 | Notificaciones multicanal | Entrega por correo, WhatsApp y push con reintentos, sin bloquear la publicación. | RN-33 | DRV-003 | Resiliencia, desacople | Obligatoria | Todos los roles (Recibe) |
| FS-03 | Auditoría | Registro inmutable de las acciones del flujo de publicar y de las acciones sensibles. | RN-33 | DRV-006 | Trazabilidad, seguridad | Obligatoria | Administrador: Consulta |
| FS-04 | Consulta de Kudos (feed y detalle) | Lectura paginada de Kudos con enlace profundo al detalle. | RN-25 | DRV-001, DRV-002 | Rendimiento, escalabilidad de lectura | Alta | Todos los roles (Sí) |
| FS-05 | Autenticación y autorización por rol | Inicio de sesión y permisos según los 7 roles; el enlace de WhatsApp exige sesión. | RN-01, RN-02, RN-03 | DRV-005 | Seguridad | Alta | Todos los roles (Sí) |
| FS-06 | Registro, activación y verificación | Registro con correo y celular; activación con aprobación, correo y celular verificados. | RN-08, RN-09 | DRV-003, DRV-005 | Seguridad, integridad | Alta | Todos los roles (Sí) |
| FS-07 | Aprobaciones jerárquicas | Cada solicitud la aprueba el rol superior según la tabla de aprobaciones. | RN-10 a RN-15 | DRV-005 | Integridad, trazabilidad | Media | Administrador: Sí (Rectores y Administradores); Rector: Sí (Coord. Académico); Coord. Académico: Sí (Decanos); Decano: Sí (Coord. de Programa); Coord. Programa: Sí (Estudiantes; Docentes*) |
| FS-08 | Restricciones de exclusividad y unicidad | Un rol activo por persona; unicidad de Rector, Coordinador Académico, Decano y Coordinador de Programa. | RN-03 a RN-07 | DRV-005 | Integridad de datos | Media | Todos los roles (Automático) |
| FS-09 | Gestión académica | Programas de estudiantes y docentes; quién administra a quién. | RN-16 a RN-22 | DRV-005 | Integridad, mantenibilidad | Media | Administrador: Sí (Administradores y Rectores); Rector: Sí (Coord. Académicos); Coord. Académico: Sí (Decanos); Decano: Sí (Coord. de Programa); Coord. Programa: Sí (Estudiantes y Docentes de sus programas); Docente: Consulta (sus programas); Estudiante: Consulta (sus programas) |
| FS-10 | Adjuntos en Kudos | Subida, descarga segura y trazabilidad de archivos. | RN-25 | DRV-004, DRV-005 | Seguridad, almacenamiento escalable | Media | Todos los roles (Sí) |
| FS-11 | Árbol de réplicas | Réplicas sobre Kudos y réplicas sin límite de profundidad ni cantidad. | RN-27 | DRV-002 | Rendimiento de lectura recursiva | Media | Todos los roles (Sí) |
| FS-12 | Calificaciones | Calificación de 1 a 5, una activa por usuario y contenido. | RN-28, RN-29 | DRV-002 | Integridad, concurrencia | Baja | Todos los roles (Sí) |
| FS-13 | Moderación | Reportes por cualquier usuario y veto por Rector o Coordinador Académico. | RN-30 a RN-32 | DRV-005 | Seguridad, gobernanza | Baja | Administrador: Reportar; Rector: Reportar y vetar; Coord. Académico: Reportar y vetar; Decano: Reportar; Coord. Programa: Reportar; Docente: Reportar; Estudiante: Reportar |
| FS-14 | Administración de categorías | Alta, edición y baja de categorías por Administrador, Rector y Coordinador Académico. | RN-23, RN-24 | DRV-005 | Seguridad, mantenibilidad | Baja | Administrador: Sí; Rector: Sí; Coord. Académico: Sí; Decano: Consulta; Coord. Programa: Consulta; Docente: Consulta; Estudiante: Consulta |
| FS-15 | Administración de usuarios por jerarquía | Activar, desactivar y cambiar de rol a los usuarios que cada rol administra (Programa: estudiantes y docentes; Facultad: coordinadores de programa; y así hacia arriba). | RN-18 a RN-22 | DRV-005 | Seguridad, integridad | Media | Administrador: Sí (Administradores y Rectores); Rector: Sí (Coord. Académicos); Coord. Académico: Sí (Decanos); Decano: Sí (Coord. de Programa); Coord. Programa: Sí (Estudiantes y Docentes de sus programas) |
| FS-16 | Gestión de la estructura académica | Alta y consulta de facultades y programas, y asignación de cada coordinador a su facultad o programa. | RN-06, RN-07, RN-18, RN-19 | DRV-005 | Integridad de datos, mantenibilidad | Media | Administrador: Sí; Coord. Académico: Sí; Decano: Consulta; Coord. Programa: Consulta; Docente: Consulta; Estudiante: Consulta |
| FS-17 | Cambio de titular de un cargo único | Reemplazar al Rector, Coordinador Académico, Decano o Coordinador de Programa, desactivando al anterior antes de activar al nuevo. | RN-03 a RN-07 | DRV-005 | Integridad, consistencia transaccional | Media | Administrador: Sí (Rector); Rector: Sí (Coord. Académico); Coord. Académico: Sí (Decano); Decano: Sí (Coord. de Programa) |
| FS-18 | Datos semilla y usuarios de demostración | Administrador inicial, categorías base y usuarios de demo para validar el flujo de publicar Kudo. | RN-01, RN-09, RN-23 | DRV-003 | Repetibilidad de la demostración | Alta | Administrador: Sí |
| FS-19 | Consultas del usuario | Ver Kudos recibidos y enviados, y filtrar por categoría. | RN-25 | DRV-001, DRV-002 | Rendimiento, usabilidad móvil | Media | Todos los roles (Sí) |
| Alcance: Completa = implementada de punta a punta; Parcial = implementada de forma mínima; Solo diseño = documentada y modelada, sin código. |  |  |  |  |  |  |  |

## Roles-Funcionalidades

| ID | Funcionalidad | Administrador | Rector | Coord. Académico | Decano | Coord. Programa | Docente | Estudiante | Fuente | Observación |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| FS-01 | Publicar Kudo | Sí | Sí | Sí | Sí | Sí | Sí | Sí | Supuesto | El enunciado dice 'una persona'; se asume cualquier usuario con cuenta activa. |
| FS-02 | Notificaciones multicanal | Recibe | Recibe | Recibe | Recibe | Recibe | Recibe | Recibe | Enunciado | El envío es automático (sistema). El destinatario recibe por correo, WhatsApp y push. |
| FS-03 | Auditoría | Consulta | — | — | — | — | — | — | Supuesto | El registro es automático. La consulta de auditoría se asume solo para el Administrador. |
| FS-04 | Consulta de Kudos (feed y detalle) | Sí | Sí | Sí | Sí | Sí | Sí | Sí | Supuesto | Se asume que todo usuario activo consulta Kudos. |
| FS-05 | Autenticación y autorización por rol | Sí | Sí | Sí | Sí | Sí | Sí | Sí | Enunciado | Todos inician sesión; los permisos dependen del rol. |
| FS-06 | Registro, activación y verificación | Sí | Sí | Sí | Sí | Sí | Sí | Sí | Enunciado | Todos se registran con correo y celular; la cuenta se activa tras aprobación y verificaciones. |
| FS-07 | Aprobaciones jerárquicas | Sí (Rectores y Administradores) | Sí (Coord. Académico) | Sí (Decanos) | Sí (Coord. de Programa) | Sí (Estudiantes; Docentes*) | — | — | Enunciado | *La tabla del enunciado no incluye a los Docentes; se asume que los aprueba el Coordinador de Programa. |
| FS-08 | Restricciones de exclusividad y unicidad | Automático | Automático | Automático | Automático | Automático | Automático | Automático | Enunciado | Regla que valida el sistema al activar o cambiar un rol. |
| FS-09 | Gestión académica | Sí (Administradores y Rectores) | Sí (Coord. Académicos) | Sí (Decanos) | Sí (Coord. de Programa) | Sí (Estudiantes y Docentes de sus programas) | Consulta (sus programas) | Consulta (sus programas) | Enunciado | Estudiantes y docentes deben poder saber en qué programas están. |
| FS-10 | Adjuntos en Kudos | Sí | Sí | Sí | Sí | Sí | Sí | Sí | Supuesto | Adjuntan quienes publican o replican; ven y descargan quienes pueden ver el Kudo. |
| FS-11 | Árbol de réplicas | Sí | Sí | Sí | Sí | Sí | Sí | Sí | Enunciado | Todos pueden replicar sobre Kudos y réplicas. |
| FS-12 | Calificaciones | Sí | Sí | Sí | Sí | Sí | Sí | Sí | Enunciado | Todos califican de 1 a 5, una calificación activa por contenido. |
| FS-13 | Moderación | Reportar | Reportar y vetar | Reportar y vetar | Reportar | Reportar | Reportar | Reportar | Enunciado | Todos reportan; solo Rector y Coordinador Académico vetan. |
| FS-14 | Administración de categorías | Sí | Sí | Sí | Consulta | Consulta | Consulta | Consulta | Enunciado | Los demás roles solo usan las categorías al publicar. |
| FS-15 | Administración de usuarios por jerarquía | Sí (Administradores y Rectores) | Sí (Coord. Académicos) | Sí (Decanos) | Sí (Coord. de Programa) | Sí (Estudiantes y Docentes de sus programas) | — | — | Enunciado | Mismo esquema de quién administra a quién. |
| FS-16 | Gestión de la estructura académica | Sí | — | Sí | Consulta | Consulta | Consulta | Consulta | Supuesto | El enunciado no dice quién crea facultades y programas; se asume Administrador y Coordinador Académico. |
| FS-17 | Cambio de titular de un cargo único | Sí (Rector) | Sí (Coord. Académico) | Sí (Decano) | Sí (Coord. de Programa) | — | — | — | Supuesto | Se deriva de la jerarquía: quien administra a un cargo único puede reemplazar a su titular. |
| FS-18 | Datos semilla y usuarios de demostración | Sí | — | — | — | — | — | — | Supuesto | Carga inicial al desplegar; el Administrador inicial es el punto de partida del sistema. |
| FS-19 | Consultas del usuario | Sí | Sí | Sí | Sí | Sí | Sí | Sí | Supuesto | Cada usuario ve sus Kudos recibidos y enviados. |
| Sí = puede ejecutarla; Consulta = solo ve; Reportar / Reportar y vetar = moderación; Recibe = destinatario de notificación; Automático = lo hace el sistema; — = no aplica. Fuente 'Supuesto' = el enunciado no lo define y debe confirmarse. |  |  |  |  |  |  |  |  |  |  |
