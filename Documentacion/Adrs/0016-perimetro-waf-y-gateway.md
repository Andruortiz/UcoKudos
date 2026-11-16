# ADR-0016: Perímetro: WAF y API Gateway separados; CDN fuera de la entrega

- **Estado:** Reemplazada por [ADR-0021](0021-gateway-y-waf-con-modsecurity.md) en la selección del producto (la decisión de poner WAF y gateway delante de la API se mantiene; se conserva como historia)
- **Fecha:** 2026-11-11
- **Drivers y restricciones:** DRV-005; RT-18, RT-25
- **ABB:** Seguridad; Gateway y protección perimetral

## Contexto
El autor decidió que el **WAF** y el **API Gateway** van en esta entrega y que se representan como dos bloques. También decidió **no incluir un CDN**. No hay dominio propio (RT-25). Los servicios de perímetro de pago pueden superar el presupuesto.

## Decisión
- El tráfico de la API pasa por **WAF → API Gateway → API**.
- **WAF:** protege contra ataques web comunes y abusos, y termina TLS.
- **API Gateway:** enruta, limita el uso (rate limiting) y aplica políticas comunes.
- La conexión de tiempo real (ADR-0007) debe poder atravesar ambos sin almacenarla en búfer.
- El **CDN no se incluye**. Se deja como evolución para el DRV-001 y el DRV-002.
- **Selección del producto: pendiente.** Se compara abajo. Si el costo o la falta de dominio impiden implementar un componente, se **documenta como diseño** y la aplicación aplica **protección mínima propia** (límite de uso y cabeceras de seguridad), declarándolo como limitación.

## Alternativas consideradas

| Opción | Costos | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| WAF de Cloudflare (plan gratuito) | Bajo | Media | Alta | Alto: exige agregar un dominio propio; el plan gratuito trae un conjunto limitado de reglas | Alta | Alta |
| Azure Front Door con WAF | Medio: la tarifa base de Standard es de unos 35 USD al mes más uso (según la documentación de Microsoft) | Media | Alta | Medio: costo alto para la entrega | Alta | Alta |
| WAF en Application Gateway | Por verificar | Alta | Alta | Medio | Media | Alta |
| API Management (plan de consumo) como gateway | Por verificar (el plan de consumo tiene una cuota mensual gratuita según la documentación del proveedor) | Media | Alta | Medio | Alta | Alta |
| Pasarela propia (Spring Cloud Gateway) | Bajo | Media | Media | Medio: es otro servicio, y el proyecto es de un solo servicio | Media | Media |
| Sin WAF ni gateway, solo protección en la aplicación | Nulo | Baja | Baja | Alto | Media | Baja |

## Consecuencias
- **Positivas:** defensa por capas; límites de uso fuera de la API; el diseño queda listo para producción.
- **Negativas:** más piezas y más costo potencial; cada salto añade latencia.
- **Pendiente:** verificar precios en el portal, decidir si se compra un dominio y confirmar si API Management en plan de consumo está dentro del presupuesto.

## Registro de cambios
- 2026-11-11: creación como Propuesta, con la selección de producto pendiente.
- 2026-11-16: pasa a **Reemplazada por [ADR-0021](0021-gateway-y-waf-con-modsecurity.md)** en la selección del producto. Azure Front Door no está disponible en cuentas de prueba o de estudiante. La decisión de fondo (WAF y gateway delante de la API, sin CDN) se conserva.
- 2026-11-16: precios oficiales de las opciones evaluadas (ver [precios-oficiales.md](precios-oficiales.md)): Front Door Standard **35 USD al mes** y Premium **330 USD al mes** de tarifa base; Application Gateway WAF v2 **0,443 USD por hora fija**; API Management de consumo con **1 000 000 de llamadas gratis**, Developer **0,0658 USD por hora**. Reemplazan los "por verificar" de la tabla anterior.
