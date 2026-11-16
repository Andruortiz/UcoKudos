# ADR-0011: Adjuntos con almacenamiento de objetos, análisis de virus y acceso por la API

- **Estado:** Aceptada, con implementación parcial (ver el registro de cambios)
- **Fecha:** 2026-11-11
- **Drivers y restricciones:** DRV-004, DRV-005; RT-15; RN-25; FS-10. El enunciado pide **cuatro estrategias**: almacenamiento, seguridad, recuperación y trazabilidad. No fija límites de tamaño ni de tipo.
- **ABB:** Gestión documental y de archivos; Seguridad

## Contexto
Los Kudos admiten archivos adjuntos. Los archivos son contenido **no confiable** de los usuarios. Los usuarios entran desde el móvil, con datos limitados (DRV-001).

## Decisión

| Estrategia | Decisión |
|---|---|
| **Almacenamiento** | Almacenamiento de objetos, separado de la base de datos. Nombre aleatorio; el nombre original se guarda saneado como dato. Metadatos (autor, Kudo, tipo, tamaño, huella, estado, fecha) en la base de datos |
| **Seguridad** | Validar sesión, tipo, tamaño y cantidad; **análisis de virus una sola vez al subir**; estado `LIMPIO`; no aceptar archivos comprimidos (riesgo de bomba de descompresión); cabeceras de descarga seguras |
| **Recuperación** | La **API** entrega el archivo solo si el estado es `LIMPIO` y el usuario tiene permiso sobre el Kudo. No se reanaliza en cada descarga |
| **Trazabilidad** | Se audita la subida, el rechazo y **cada descarga** |

- **Límites (SUP-04, propuestos por el autor):** 10 MB por archivo, imágenes y PDF, hasta 5 por Kudo. Viven en el catálogo de parámetros (ADR-0017).
- **Compresión (SUP-05):** compresión HTTP de las respuestas de la API; reducción de imágenes en el navegador antes de subir; miniaturas para el feed como evolución.
- **Orden al subir:** validar, analizar virus, reprocesar la imagen, guardar.
- **Si el archivo está infectado:** se descarta solo ese archivo y se informa al usuario. Es una recomendación a confirmar.
- Subida y descarga **por la API**, sin URL firmadas, para mantener el control en un punto.

## Alternativas consideradas

| Opción | Costos | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| **Almacenamiento de objetos, acceso por la API** | Bajo-medio (por verificar) | Media | Alta | Bajo | Alta | Alta |
| Archivos dentro de la base de datos | Bajo | Baja | Baja: engorda la base y las copias de seguridad | Medio | Media | Baja |
| Subida directa con URL firmada y análisis posterior | Bajo | Alta | Alta | Medio: existe un momento con el archivo sin analizar | Media | Alta |
| Disco del servidor | Bajo | Baja | Baja: no sirve con varias instancias ni contenedores | Alto | Baja | Baja |

## Consecuencias
- **Positivas:** control de seguridad en un solo punto; trazabilidad completa; la base de datos no crece con binarios.
- **Negativas:** los archivos pasan por la API y consumen su ancho de banda; el límite de 10 MB lo acota.
- **Pendiente:** elegir la herramienta de análisis de virus (un motor de código abierto en contenedor o un servicio de la nube; **por verificar** costo y límites) y confirmar los valores de SUP-04.

## Registro de cambios
- 2026-11-11: creación como Propuesta.
- 2026-11-16: **implementación parcial** de esta decisión, sin cambiar la opción elegida (almacenamiento de objetos, acceso por la API):
  - **Almacenamiento:** Azure Blob Storage (cuenta `stucokudos1421`, contenedor privado `adjuntos`, sin acceso público). El archivo va a Blob y la base de datos guarda nombre, tipo y tamaño (migración V5). Se accede con una URL firmada (SAS) del contenedor que solo conoce el servidor; el usuario nunca la recibe.
  - **Seguridad:** se valida la cantidad, el tipo y el tamaño, y que los bytes del archivo correspondan al tipo declarado (PDF, PNG, JPEG, GIF y WebP; se rechaza SVG). **Microsoft Defender for Storage** analiza los archivos al subirlos, con tope de 5 GB al mes. **Todavía no se bloquea la descarga según el resultado del análisis.**
  - **Recuperación:** solo el autor y el destinatario descargan, por el endpoint `GET /api/v1/kudos/{id}/attachments/{attachmentId}`; cualquier otro recibe 404.
  - **Trazabilidad:** **pendiente**: la descarga aún no se audita.
  - Se mantiene "subida y descarga por la API, sin URL firmadas para el usuario".
- 2026-11-16: precios oficiales (ver [precios-oficiales.md](precios-oficiales.md)): Blob Storage de acceso frecuente LRS en East US 2, **0,0184 USD por GB al mes** y **0,05 USD por 10 000 operaciones de escritura**; análisis de malware de Defender for Storage, **0,15 USD por GB analizado** (la cuota fija por cuenta: **No verificado**). Reemplazan el "bajo-medio (por verificar)" de la tabla anterior. Almacenar archivos en PostgreSQL costaría **0,13 USD por GB al mes** (almacenamiento del servidor flexible en Central US), unas siete veces más por GB que Blob.
