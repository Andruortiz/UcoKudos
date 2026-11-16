# Trade-off de atributos de calidad de UCO Kudos

> Conversión del archivo `tradeoff-calidad-ucokudos.xlsx` a Markdown, como pide la guía del trabajo. Es una conversión **literal** del contenido del 11 de noviembre de 2026: las columnas de estado y de tecnología pendiente de ADR **no se actualizaron** con lo construido después (por ejemplo, el cambio de Twilio a Vonage). El estado vigente de cada decisión está en el [índice de ADR](../04-adr/README.md) y en el [estado del proyecto](../00-estado-del-proyecto.md). El archivo original se conserva en la carpeta anterior.

## Leyenda

**Trade-off de atributos de calidad: UCO Kudos**

| Elemento | Descripción |
| --- | --- |
| Propósito | Priorizar atributos de calidad, convertirlos en escenarios medibles y registrar las tensiones entre ellos. No decide tecnologías: las decisiones se toman después en los ADR. |
| Flujo de hojas | StakeHolders → Priorizacion QA → Lluvia de escenarios → Reglas de seleccion → Escenarios seleccionados → Tacticas por escenario → Tradeoff QA |
| Código de colores | Texto azul = valor ingresado y modificable (por ejemplo, las prioridades). Texto negro = fórmula o texto fijo. |
| Importante | Las prioridades (1 a 10) y las metas de cada escenario son PROPUESTAS del autor y deben validarse. Son supuestos, no datos medidos. |
| Relación con otros archivos | Las funcionalidades (FS-xx) corresponden al Excel r-tecnicas-negocio-funcionalidades.md. Los drivers DRV-001 a DRV-007 vienen del enunciado. |
| Decisión asociada | Queda como 'Pendiente' hasta que se redacten los ADR. |

## StakeHolders

| Stakeholder | ¿Qué sienten? | ¿Qué ven? | ¿Qué oyen? | ¿Qué dicen y qué hacen? |
| --- | --- | --- | --- | --- |
| Estudiante | Quiere sentirse reconocido y enterarse rápido de un Kudo; le frustra esperar o perderse en una pantalla lenta. | Ve la plataforma desde su celular: Kudos recibidos, el feed, réplicas, calificaciones y avisos. | Oye de compañeros y docentes que le enviaron un Kudo; recibe correo, WhatsApp y push. | Abre el enlace del aviso, lee el Kudo, responde, califica y reporta contenido inapropiado. |
| Docente | Quiere reconocer a sus estudiantes sin perder tiempo y confiar en que el mensaje llega. | Ve desde el celular el formulario para publicar, sus Kudos enviados y las réplicas. | Oye que el estudiante recibió o no el aviso; recibe avisos de réplicas y de su propia cuenta. | Publica Kudos con adjuntos, replica, califica y consulta los programas donde enseña. |
| Coordinadores y directivos | Sienten responsabilidad por la convivencia institucional y por las decisiones que toman (aprobar, vetar). | Ven solicitudes por aprobar, usuarios que administran, contenidos reportados y el historial de acciones. | Oyen reportes de la comunidad y reclamos cuando un contenido se veta o una cuenta no se aprueba. | Aprueban solicitudes, administran a quienes les corresponde, reportan y (Rector y Coord. Académico) vetan contenido. |
| Administrador del sistema | Siente la presión de que el sistema esté disponible, seguro y que se pueda reconstruir lo ocurrido. | Ve cuentas, roles, estructura académica, métricas, trazas y logs. | Oye incidentes: accesos indebidos, caídas, mensajes que no llegan. | Configura categorías y estructura, administra administradores y rectores, desactiva cuentas y atiende incidentes. |
| Evaluador (docente del curso) | Quiere ver que el estudiante domina y puede sustentar cada decisión. | Ve el sistema publicado, la documentación, el repositorio, los pipelines y las evidencias. | Oye la sustentación y las respuestas a cambios en vivo. | Usa la URL pública, valida el flujo de publicar Kudo, pide cambios de código y refactorizaciones. |

## Priorizacion QA

| Atributo de calidad | Estudiante | Docente | Coordinadores y directivos | Administrador del sistema | Evaluador (docente del curso) | Total | Ponderador global | Posición |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Rendimiento | 9 | 9 | 7 | 4 | 4 | 33 | 0.12 | 3 |
| Usabilidad móvil | 10 | 10 | 6 | 1 | 5 | 32 | 0.116 | 4 |
| Seguridad | 6 | 6 | 10 | 10 | 9 | 41 | 0.149 | 1 |
| Confiabilidad | 7 | 8 | 8 | 6 | 6 | 35 | 0.127 | 2 |
| Capacidad de ser auditado | 3 | 3 | 9 | 7 | 7 | 29 | 0.105 | 6 |
| Disponibilidad | 8 | 7 | 5 | 9 | 1 | 30 | 0.109 | 5 |
| Interoperabilidad | 4 | 5 | 3 | 2 | 3 | 17 | 0.062 | 10 |
| Mantenibilidad | 1 | 1 | 1 | 5 | 10 | 18 | 0.065 | 8 |
| Escalabilidad | 5 | 4 | 4 | 3 | 2 | 18 | 0.065 | 8 |
| Observabilidad | 2 | 2 | 2 | 8 | 8 | 22 | 0.08 | 7 |
| Total | 55 | 55 | 55 | 55 | 55 | 275 | 1 |  |
| Escala: 10 = más importante para ese stakeholder, 1 = menos importante; cada columna usa los números 1 a 10 una sola vez. Valores propuestos por el autor, por validar. |  |  |  |  |  |  |  |  |

## Lluvia de escenarios

| Stakeholder | Prioridad (1 a 10) | Atributo de calidad | Lluvia en lenguaje de usuario | Pregunta detonante del arquitecto | Ponderador del atributo | Puntaje | Seleccionado |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Estudiante | 10 | Usabilidad móvil | Me llega el aviso al celular, toco el enlace y quiero ver mi Kudo enseguida, sin perderme en menús. | ¿Cuántos toques estás dispuesto a dar desde el aviso hasta leer el Kudo? ¿Qué haces si te pide iniciar sesión? | 0.116 | 1.164 | Sí |
| Estudiante | 9 | Rendimiento | Abro la plataforma con datos del celular y quiero ver los Kudos al instante, no una pantalla en blanco. | ¿Cuánto esperas antes de pensar que no cargó? ¿Qué conexión usas normalmente? | 0.12 | 1.08 | Sí |
| Estudiante | 8 | Disponibilidad | Cuando todos entramos a la vez para ver los reconocimientos, la plataforma tiene que seguir respondiendo. | ¿En qué momentos del semestre entra más gente? ¿Qué tan grave es que no abra un día? | 0.109 | 0.873 | No |
| Docente | 10 | Usabilidad móvil | Quiero publicar un Kudo con una foto o archivo desde mi celular en pocos pasos. | ¿Desde qué dispositivo publicas hoy? ¿Cuántos pasos te parecen aceptables? | 0.116 | 1.164 | Sí |
| Docente | 9 | Rendimiento | Cuando un Kudo tiene muchas réplicas encadenadas, quiero poder leerlas sin que se cuelgue la pantalla. | ¿Qué tan largas son las conversaciones que esperas? ¿Cuánto tarda 'demasiado'? | 0.12 | 1.08 | Sí |
| Docente | 8 | Confiabilidad | Publico el Kudo y necesito estar seguro de que se guardó y de que el estudiante sí recibirá el aviso. | Si un canal falla, ¿prefieres que el Kudo se publique igual y el aviso se reintente? | 0.127 | 1.018 | Sí |
| Coordinadores y directivos | 10 | Seguridad | Solo quien tiene el cargo debe poder vetar o aprobar; no quiero que alguien sin permiso lo haga. | ¿Qué acciones jamás debería poder hacer otro rol? ¿Qué pasa si alguien lo intenta? | 0.149 | 1.491 | Sí |
| Coordinadores y directivos | 9 | Capacidad de ser auditado | Si alguien reclama por un veto o una aprobación, necesito saber quién lo hizo, cuándo y por qué. | ¿Cuánto tiempo hacia atrás debe poder consultarse? ¿Qué datos mínimos necesitas ver? | 0.105 | 0.949 | Sí |
| Coordinadores y directivos | 8 | Confiabilidad | No puede haber dos personas con el mismo cargo único activo, ni aprobaciones que se contradigan. | ¿Qué errores serían inaceptables en las aprobaciones y el cambio de cargos? | 0.127 | 1.018 | Sí |
| Administrador del sistema | 10 | Seguridad | Cuando desactivo a alguien, su acceso debe cortarse de verdad y rápido. | ¿En cuánto tiempo debe hacerse efectiva una desactivación en todos los dispositivos? | 0.149 | 1.491 | Sí |
| Administrador del sistema | 9 | Disponibilidad | Si algo falla, el sistema debe seguir sirviendo lo esencial y avisarme. | ¿Qué funciones deben seguir vivas aunque falle un proveedor externo? | 0.109 | 0.982 | Sí |
| Administrador del sistema | 8 | Observabilidad | Si me dicen que una publicación fue lenta o no llegó, quiero seguir su camino completo y ver dónde se detuvo. | ¿Qué necesitas ver para encontrar la causa en minutos y no en horas? | 0.08 | 0.64 | Sí |
| Evaluador (docente del curso) | 10 | Mantenibilidad | Voy a pedir un cambio en vivo, como cambiar un proveedor o agregar un canal, y debería ser pequeño y seguro. | ¿Qué tipo de cambios pedirías en la sustentación? ¿Cuánto tiempo te parece razonable? | 0.065 | 0.655 | Sí |
| Evaluador (docente del curso) | 9 | Seguridad | Quiero ver que la autenticación, los permisos y los secretos están bien resueltos. | ¿Qué fallas de seguridad buscarías primero en el sistema publicado? | 0.149 | 1.342 | No |
| Evaluador (docente del curso) | 8 | Observabilidad | Quiero ver evidencia de métricas, trazas y auditoría del flujo de publicar Kudo. | ¿Qué evidencia te convence de que el flujo está bien observado? | 0.08 | 0.64 | No |
| Se muestran los 3 atributos más importantes de cada stakeholder. 'Seleccionado' aplica las reglas de la hoja Reglas de seleccion. |  |  |  |  |  |  |  |

## Reglas de seleccion

**Mecánica de selección de escenarios**

| Elemento | Descripción |
| --- | --- |
| Propósito | Escoger de forma reproducible los escenarios más representativos, sin votación de equipo, porque el proyecto es individual. |
| Universo | 15 escenarios: los 3 atributos más importantes de cada uno de los 5 stakeholders. |
| Puntaje | Puntaje = prioridad del atributo para el stakeholder × ponderador global del atributo. Se calcula con fórmula en la hoja Lluvia de escenarios. |
| Criterio 1 | Se seleccionan los 10 escenarios con mayor puntaje. |
| Criterio 2 | Máximo 2 escenarios por atributo, para no concentrar la selección. |
| Criterio 3 | Cobertura de drivers: si Observabilidad (DRV-006) o Mantenibilidad (DRV-007, automatización y sustentación) quedan sin escenario, se agrega el de mayor puntaje de ese atributo. |
| Resultado | 12 escenarios seleccionados. |
| Limitación | Las prioridades las asigna el autor y no se validaron con usuarios reales. Si cambian las prioridades, la selección debe recalcularse con las mismas reglas. |

## Escenarios seleccionados

| # | Código | Stakeholder | Atributo de calidad | Fuente del estímulo | Estímulo | Ambiente | Artefacto | Respuesta | Medida de la respuesta | Business rationale | Supuestos y riesgos |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | EST-USA-01 | Estudiante | Usabilidad móvil | Estudiante desde su celular | Toca el enlace del Kudo recibido por WhatsApp. | Conexión móvil 4G variable; la sesión puede estar activa o vencida. | Aplicación móvil web; acceso al detalle de un Kudo. | Muestra el Kudo; si no hay sesión, pide iniciar sesión y regresa automáticamente a ese Kudo. | Kudo visible en ≤ 3 s (P95) con sesión activa; 100% de los inicios de sesión regresan al Kudo original. | El aviso solo cumple su propósito si el reconocimiento se lee con poca fricción. | Meta propuesta por validar; depende de la red del usuario. |
| 2 | EST-REN-01 | Estudiante | Rendimiento | Estudiante desde su celular | Abre el feed de Kudos. | Operación normal; datos móviles; cientos de Kudos publicados. | Consulta de Kudos del back-end y vista del feed. | Devuelve la primera página paginada y ligera; el resto se carga al desplazarse. | Primera página visible en ≤ 2 s (P95) en 4G; respuesta de API ≤ 100 KB por página. | La carga es predominantemente de lectura (DRV-002) y casi todo el uso es móvil (DRV-001). | Meta propuesta; tamaño de página por definir. |
| 3 | ADM-DIS-01 | Administrador del sistema | Disponibilidad | Proveedor externo de notificaciones | Deja de responder durante un pico de acceso de los usuarios. | Horario académico con pico de usuarios; un canal externo caído. | Componente de notificaciones y consulta de Kudos. | Las lecturas y publicaciones siguen funcionando, se detecta la falla y se alerta al administrador. | Disponibilidad de lectura ≥ 99% mensual en horario académico; 0 errores de lectura causados por un canal externo; alerta en ≤ 5 min. | Un proveedor ajeno no debe impedir ver ni publicar Kudos. | El entorno de pruebas del proveedor tiene límites propios. |
| 4 | DOC-USA-01 | Docente | Usabilidad móvil | Docente desde su celular | Publica un Kudo con un archivo adjunto. | Celular, conexión móvil; operación normal. | Formulario de publicación y flujo Publicar Kudo. | Valida, guarda y confirma al autor sin esperar el envío de las notificaciones. | ≤ 4 pasos desde que abre el formulario; confirmación ≤ 2 s tras enviar (sin contar la subida del archivo). | Reconocer debe ser rápido; esperar a los canales volvería lenta la publicación. | El tiempo de subida depende del tamaño del archivo. |
| 5 | DOC-REN-01 | Docente | Rendimiento | Docente | Abre un Kudo con una conversación larga de réplicas anidadas. | Operación normal; ~500 réplicas y profundidad de 20 niveles. | Consulta del árbol de réplicas. | Entrega el primer nivel y carga las ramas bajo demanda. | Primer nivel visible en ≤ 2 s (P95); cada rama adicional en ≤ 1 s. | El enunciado no limita profundidad ni cantidad de réplicas. | Los números de profundidad y cantidad son hipótesis por validar. |
| 6 | DOC-CON-01 | Docente | Confiabilidad | Docente | Publica un Kudo mientras el proveedor de WhatsApp no responde. | Operación normal; fallo del canal externo. | Flujo Publicar Kudo; gestión de envíos pendientes. | Guarda el Kudo, registra el envío pendiente y lo reintenta; correo y push no se afectan. | 0 Kudos perdidos; 100% de envíos pendientes se reintentan durante 24 h; estado de cada canal registrado. | El paso 1 (guardar) no puede depender de los pasos 2 a 4. | Reintentos limitados por las reglas del proveedor. |
| 7 | COO-SEG-01 | Coordinadores y directivos | Seguridad | Coordinador de Programa | Intenta vetar un Kudo, acción que su rol no tiene. | Operación normal; usuario con sesión válida. | Control de autorización de moderación. | Rechaza la acción y registra el intento. | 100% de los intentos no autorizados rechazados y auditados. | Solo el Rector y el Coordinador Académico pueden vetar. | Las reglas de rol se validan en el servidor, no en la interfaz. |
| 8 | COO-AUD-01 | Coordinadores y directivos | Capacidad de ser auditado | Rector | Consulta quién vetó un Kudo, cuándo y con qué resultado. | Operación normal; consulta de historial. | Registro de auditoría. | Muestra actor, fecha, acción, objeto y resultado. | 100% de las acciones sensibles (publicar, vetar, aprobar, cambiar rol) con actor, fecha y resultado; consulta del historial de un Kudo ≤ 3 s. | Permite responder a reclamos sobre vetos y aprobaciones. | Debe minimizar datos personales. |
| 9 | COO-CON-01 | Coordinadores y directivos | Confiabilidad | Dos Coordinadores Académicos | Aprueban a la vez dos solicitudes para el mismo cargo único. | Operación normal; aprobaciones concurrentes. | Aprobación de solicitudes y activación de cargos únicos. | Activa solo una y rechaza o deja pendiente la otra. | 0 casos con dos titulares activos de un cargo único en 50 aprobaciones concurrentes de prueba. | La unicidad (Rector, Coordinador Académico, Decano, Coordinador de Programa) debe ser inviolable. | La prueba de concurrencia se define con la implementación. |
| 10 | ADM-SEG-01 | Administrador del sistema | Seguridad | Administrador | Desactiva a un usuario que ya no debe acceder. | Operación normal; el usuario puede tener sesión abierta en su celular. | Gestión de cuentas y autenticación. | Revoca el acceso: las siguientes solicitudes del usuario son rechazadas. | 100% de las solicitudes posteriores rechazadas en ≤ 1 minuto. | Una cuenta desactivada no debe conservar acceso. | El tiempo depende del mecanismo de sesión, por decidir. |
| 11 | ADM-OBS-01 | Administrador del sistema | Observabilidad | Administrador | Investiga una publicación lenta o un aviso que no llegó. | Producción; carga normal. | Registros, métricas y trazas del flujo Publicar Kudo. | Permite seguir la publicación de punta a punta con un identificador de correlación. | 100% de las publicaciones con identificador correlacionable; causa localizada en ≤ 10 min en una prueba guiada. | DRV-006 pide métricas, trazas y auditoría desde la línea base. | Meta de tiempo por validar en la práctica. |
| 12 | EVA-MAN-01 | Evaluador (docente del curso) | Mantenibilidad | Evaluador | Pide en la sustentación cambiar el proveedor de un canal de notificación. | Sustentación en vivo; sistema funcionando. | Componentes de notificación y capas del sistema. | El cambio se hace sin tocar las reglas de negocio ni los casos de uso, y las pruebas siguen pasando. | Cambio completado en ≤ 30 min; 0 modificaciones en el dominio y los casos de uso; pruebas de arquitectura en verde. | La sustentación incluye cambios de código en vivo. | Depende de que las pruebas de arquitectura estén implementadas. |
| Las medidas son metas propuestas, por validar con la implementación. El artefacto se describe por función, sin fijar tecnología. |  |  |  |  |  |  |  |  |  |  |  |

## Tacticas por escenario

| # | Código | Atributo de calidad | Funcionalidad relacionada | Táctica(s) a considerar | ADR relacionado |
| --- | --- | --- | --- | --- | --- |
| 1 | EST-USA-01 | Usabilidad móvil | FS-04, FS-05 | Enlace profundo con retorno a la ruta original tras iniciar sesión; interfaz pensada para móvil; reducir pasos. | Pendiente |
| 2 | EST-REN-01 | Rendimiento | FS-04, FS-19 | Paginación; respuestas livianas; lecturas optimizadas y posible caché de datos de baja volatilidad. | Pendiente |
| 3 | ADM-DIS-01 | Disponibilidad | FS-04, FS-02 | Aislar las fallas de canales externos de las lecturas; supervisar el estado de salud. | Pendiente |
| 4 | DOC-USA-01 | Usabilidad móvil | FS-01, FS-10 | Envío de notificaciones desacoplado de la publicación; validación temprana; subida de adjuntos separada. | Pendiente |
| 5 | DOC-REN-01 | Rendimiento | FS-11 | Carga de ramas bajo demanda; paginación por nivel; índices sobre la relación padre-hijo. | Pendiente |
| 6 | DOC-CON-01 | Confiabilidad | FS-01, FS-02 | Persistir los envíos pendientes; reintentos con espera creciente; idempotencia; estado por canal. | Pendiente |
| 7 | COO-SEG-01 | Seguridad | FS-13, FS-05 | Autorización por rol en el servidor; denegar por defecto; auditar los intentos. | Pendiente |
| 8 | COO-AUD-01 | Capacidad de ser auditado | FS-03 | Registro inmutable de acciones sensibles; identificador de correlación; consulta de historial. | Pendiente |
| 9 | COO-CON-01 | Confiabilidad | FS-07, FS-08, FS-17 | La persistencia garantiza la unicidad; transacciones; control de concurrencia. | Pendiente |
| 10 | ADM-SEG-01 | Seguridad | FS-15, FS-05 | Sesiones de corta vida; revocación; verificar el estado de la cuenta en cada solicitud. | Pendiente |
| 11 | ADM-OBS-01 | Observabilidad | FS-03 | Identificador de correlación; registros estructurados; métricas y trazas del flujo. | Pendiente |
| 12 | EVA-MAN-01 | Mantenibilidad | FS-02 | Dependencias hacia el dominio (puertos y adaptadores); pruebas de arquitectura; bajo acoplamiento. | Pendiente |

## Tradeoff QA

| ID | Atributos en tensión | Tensión arquitectónica | Criterio propuesto / resolución | Impacto aceptado | Escenarios relacionados | Decisión asociada |
| --- | --- | --- | --- | --- | --- | --- |
| T-01 | Seguridad vs Usabilidad móvil | Exigir sesión para abrir el enlace del aviso protege el contenido, pero agrega un paso en el celular. | Prima la seguridad; se reduce la fricción con retorno automático al Kudo. | Un paso extra de inicio de sesión cuando la sesión venció. | EST-USA-01, ADM-SEG-01 | Pendiente: se asocia al ADR correspondiente. |
| T-02 | Confiabilidad vs Rendimiento | Esperar a los canales antes de confirmar volvería lenta y frágil la publicación; desacoplarlos exige guardar el estado de cada envío. | Guardar y confirmar primero; los envíos se desacoplan y se reintentan. | El aviso puede llegar con retraso y se necesita almacenar el estado de cada envío. | DOC-USA-01, DOC-CON-01, ADM-DIS-01 | Pendiente: se asocia al ADR correspondiente. |
| T-03 | Rendimiento vs Consistencia | La caché y las lecturas preparadas aceleran, pero pueden mostrar datos desactualizados (calificaciones, vetos, permisos). | Solo se cachean datos de baja volatilidad; vetos y permisos se leen siempre de la fuente de verdad. | Menor aprovechamiento de la caché y más lecturas a la base de datos. | EST-REN-01, COO-SEG-01 | Pendiente: se asocia al ADR correspondiente. |
| T-04 | Capacidad de ser auditado vs Rendimiento y privacidad | Registrar más acciones mejora la trazabilidad, pero cuesta almacenamiento y tiempo, y expone datos personales. | Se auditan completas las acciones sensibles con los datos mínimos necesarios. | Mayor almacenamiento y un pequeño costo por operación sensible. | COO-AUD-01, ADM-OBS-01 | Pendiente: se asocia al ADR correspondiente. |
| T-05 | Mantenibilidad vs Simplicidad | Separar en capas con puertos y adaptadores agrega clases y conversiones, pero permite cambiar proveedores y probar aislado. | Prima la mantenibilidad, por la sustentación con cambios en vivo y las pruebas. | Más código y conversiones entre capas. | EVA-MAN-01 | Pendiente: se asocia al ADR correspondiente. |
| T-06 | Disponibilidad vs Dependencia de proveedores externos | Los canales externos (WhatsApp, correo) no están bajo nuestro control y el entorno de pruebas tiene límites. | Cada canal queda detrás de un contrato propio; su caída no detiene la publicación ni la lectura. | Se acepta que un canal falle y se reintente; dependencia de límites del proveedor. | DOC-CON-01, ADM-DIS-01 | Pendiente: se asocia al ADR correspondiente. |
| T-07 | Rendimiento vs Requisito sin límites | Réplicas sin límite de profundidad y contenido sin límite de longitud presionan la lectura. | No se limita el modelo; la lectura se pagina y carga bajo demanda. | La interfaz muestra ramas colapsadas y las consultas son más complejas. | DOC-REN-01 | Pendiente: se asocia al ADR correspondiente. |
| T-08 | Confiabilidad vs Concurrencia | Garantizar unicidad de cargos exige serializar ciertas operaciones. | Primero la integridad: la persistencia garantiza la unicidad. | Algunas operaciones administrativas pueden ser más lentas o rechazarse por conflicto. | COO-CON-01 | Pendiente: se asocia al ADR correspondiente. |
