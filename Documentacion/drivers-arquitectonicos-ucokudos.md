# Drivers arquitectónicos de UCO Kudos

> Conversión del archivo `drivers-arquitectonicos-ucokudos.xlsx` a Markdown, como pide la guía del trabajo. Es una conversión **literal** del contenido del 11 de noviembre de 2026: las columnas de estado y de tecnología pendiente de ADR **no se actualizaron** con lo construido después (por ejemplo, el cambio de Twilio a Vonage). El estado vigente de cada decisión está en el [índice de ADR](../04-adr/README.md) y en el [estado del proyecto](../00-estado-del-proyecto.md). El archivo original se conserva en la carpeta anterior.

## Leyenda

**Drivers arquitectónicos: UCO Kudos**

| Elemento | Descripción |
| --- | --- |
| Fuente | Enunciado del Proyecto Integrador Final (TrabajoFinalKudos), sección Drivers Arquitectónicos: DRV-001 a DRV-007. |
| Qué es un driver | Requisito o condición que condiciona decisiones de arquitectura: funcionales significativos, atributos de calidad y restricciones. |
| Columna 'Texto del enunciado' | Descripción tomada del enunciado, sin alterar su sentido. |
| Columnas siguientes | Son interpretación del autor: tipo, atributos de calidad, estrategia de diseño y trazabilidad. La estrategia se describe sin fijar tecnología; se concreta en los ADR. |
| Trazabilidad | FS-xx: funcionalidades (r-tecnicas-negocio-funcionalidades.md). RT-xx: restricciones técnicas del mismo archivo. Códigos de escenario: tradeoff-calidad-ucokudos.md. |
| Símbolos de la matriz | ● = atributo principal del driver; ○ = atributo secundario. |

## Drivers

| ID | Driver | Texto del enunciado | Tipo | Atributos de calidad | Estrategia de diseño (sin tecnología) | Funcionalidades relacionadas | Restricciones relacionadas | Escenarios de calidad | Estado |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| DRV-001 | Movilidad | Se estima que aproximadamente el 90% de los usuarios utilizará la plataforma desde dispositivos móviles. La estrategia de diseño deberá ser Mobile First. | Calidad | Usabilidad móvil; Rendimiento | Diseñar primero para pantallas pequeñas; interfaz ligera; respuestas compactas y paginadas; enlace profundo desde los avisos con retorno tras iniciar sesión; tolerar conexión móvil variable. | FS-01, FS-04, FS-05, FS-19 | RT-16 | EST-USA-01, DOC-USA-01, EST-REN-01 | Estrategia definida; tecnología pendiente de ADR |
| DRV-002 | Alta demanda de lectura | Se espera una carga predominantemente orientada a lectura. La solución deberá justificar cómo responde a escenarios donde la mayoría de las operaciones sean consultas. | Calidad | Rendimiento; Escalabilidad; Disponibilidad | Separar el camino de lectura del de escritura; paginación; índices; caché de datos de baja volatilidad; lectura bajo demanda del árbol de réplicas; poder crecer en lectura sin crecer en escritura. | FS-04, FS-11, FS-19 | RT-17, RT-20 | EST-REN-01, DOC-REN-01, ADM-DIS-01 | Estrategia definida; tecnología pendiente de ADR |
| DRV-003 | Notificaciones multicanal | La plataforma deberá soportar: correo electrónico, WhatsApp y Push Notifications. | Funcional significativo y calidad | Confiabilidad; Interoperabilidad; Disponibilidad | Un contrato propio por canal; envío desacoplado de la publicación; reintentos y estado registrado por canal; el push solo si el destinatario está conectado. | FS-01, FS-02, FS-06 | RT-13, RT-14 | DOC-CON-01, DOC-USA-01, ADM-DIS-01 | Estrategia definida; tecnología pendiente de ADR |
| DRV-004 | Adjuntos | Los Kudos deberán soportar carga de archivos. | Funcional significativo | Seguridad; Rendimiento; Escalabilidad | Estrategias de almacenamiento, seguridad, recuperación y trazabilidad (las cuatro que pide el enunciado): archivos separados de los datos relacionales, validación de tipo y tamaño, acceso controlado a la descarga y registro de cada operación. | FS-01, FS-10 | RT-15 | DOC-USA-01 | Estrategia definida; tecnología pendiente de ADR |
| DRV-005 | Seguridad | La arquitectura debe incorporar mecanismos de seguridad apropiados. | Calidad | Seguridad; Capacidad de ser auditado | Autenticación; autorización por rol validada en el servidor y denegar por defecto; secretos fuera del código; validación de entradas; protección de las APIs y de las integraciones con terceros; auditoría de acciones sensibles. Adjuntos (ver DRV-004): validar tipo y tamaño, tratar el contenido como no confiable, controlar quién puede descargar y registrar cada acceso. | FS-05, FS-06, FS-07, FS-10, FS-13, FS-15 | RT-15 | COO-SEG-01, ADM-SEG-01, COO-CON-01 | Estrategia definida; tecnología pendiente de ADR |
| DRV-006 | Observabilidad | La arquitectura debe incorporar capacidades de monitoreo, métricas, trazas y auditoría. | Calidad | Observabilidad; Capacidad de ser auditado | Registros estructurados; identificador de correlación en todo el flujo; métricas técnicas y de negocio; trazas del flujo Publicar Kudo; auditoría persistente de las acciones sensibles. | FS-03 | — | ADM-OBS-01, COO-AUD-01 | Estrategia definida; tecnología pendiente de ADR |
| DRV-007 | Automatización | La arquitectura debe incorporar capacidades DevSecOps desde la línea base. | Calidad y restricción | Mantenibilidad; Confiabilidad; Seguridad | Construcción, pruebas y despliegue repetibles y sin pasos manuales; análisis estático y escaneo de vulnerabilidades dentro del proceso; configuración por entorno; entornos reproducibles. | — | RT-08, RT-12 | EVA-MAN-01 | Estrategia definida; tecnología pendiente de ADR |

## Driver vs Atributo

| Driver | Rendimiento | Usabilidad móvil | Seguridad | Confiabilidad | Capacidad de ser auditado | Disponibilidad | Interoperabilidad | Mantenibilidad | Escalabilidad | Observabilidad |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| DRV-001 Movilidad | ● | ● |  |  |  | ○ |  |  |  |  |
| DRV-002 Alta demanda de lectura | ● |  |  |  |  | ○ |  |  | ● |  |
| DRV-003 Notificaciones multicanal |  |  |  | ● |  | ○ | ● |  |  |  |
| DRV-004 Adjuntos | ○ |  | ● |  | ● |  |  |  | ○ |  |
| DRV-005 Seguridad |  |  | ● |  | ○ |  |  |  |  |  |
| DRV-006 Observabilidad |  |  |  |  | ● |  |  |  |  | ● |
| DRV-007 Automatización |  |  | ○ | ○ |  |  |  | ● |  |  |
