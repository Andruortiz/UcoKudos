# ADR-0012: Árbol de réplicas con lista de adyacencia y consulta recursiva

- **Estado:** Propuesta (solo diseño, no construida)
- **Fecha:** 2026-11-11
- **Drivers y restricciones:** DRV-002; RT-20; RN-27; FS-11
- **ABB:** Persistencia; Gestión de contenido

## Contexto
Se puede replicar sobre Kudos y sobre otras réplicas, **sin límite de profundidad, cantidad ni longitud**. La lectura es predominante (DRV-002). El modelo no puede asumir una profundidad máxima.

## Decisión
- **Lista de adyacencia:** cada réplica guarda el identificador de su Kudo y de su réplica padre (nulo si responde al Kudo directamente).
- La lectura **se pide bajo demanda y por niveles**, con paginación: primero las réplicas directas y, si el usuario las despliega, las de ese nivel. No se carga el árbol completo.
- Cuando haga falta recorrerlo, se usa una **consulta recursiva** de la base de datos (PostgreSQL), con un tope de resultados por petición.
- Índices por Kudo y por réplica padre.
- Las calificaciones (RN-28) apuntan al Kudo o a la réplica con una restricción de unicidad por usuario y contenido.

## Alternativas consideradas

| Opción | Costos | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| **Lista de adyacencia + consulta recursiva** | Bajo | Baja | Media-alta con carga por niveles | Medio: árboles muy profundos son lentos si se leen enteros | Alta | Alta |
| Tabla de cierre (ancestros y descendientes) | Medio | Media | Alta en lectura | Medio: escritura más costosa y más almacenamiento | Media | Alta |
| Ruta materializada | Bajo | Media | Alta | Medio: la ruta crece con la profundidad | Media | Media |
| Conjuntos anidados | Bajo | Alta | Media | Alto: insertar obliga a reescribir el árbol | Baja | Baja |

## Consecuencias
- **Positivas:** escritura simple; sin límite de profundidad; fácil de explicar.
- **Negativas:** leer un hilo muy profundo completo requiere varias consultas o una recursiva.
- **Evolución:** si la lectura de hilos profundos se vuelve un problema, pasar a tabla de cierre sin cambiar el contrato de la API.

## Costos con precio oficial
La lista de adyacencia usa la misma base de datos (ADR-0005) y no agrega servicios: **sin costo adicional** sobre los 18,18 USD al mes estimados de PostgreSQL. Las demás opciones (tabla de cierre, ruta materializada, conjuntos anidados) tampoco agregan servicios; su costo es de almacenamiento (**0,13 USD por GB al mes** en el servidor flexible de Central US) y de escritura.

## Registro de cambios
- 2026-11-11: creación como Propuesta.
- 2026-11-16: **no construido.** La funcionalidad de réplicas (FS-11) y las calificaciones (FS-12) no existen: el esquema de la migración V1 **no tiene tabla de réplicas**. El ADR permanece como diseño. Es la mayor brecha funcional del proyecto.
