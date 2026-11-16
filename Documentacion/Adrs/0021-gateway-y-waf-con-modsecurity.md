# ADR-0021: Gateway y WAF con nginx, ModSecurity y reglas OWASP CRS

- **Estado:** Aceptada
- **Fecha:** 2026-11-16
- **Reemplaza a:** [ADR-0016](0016-perimetro-waf-y-gateway.md) en la **selección del producto**
- **Drivers y restricciones:** DRV-005; RT-18, RT-25
- **ABB:** Seguridad; Gateway y protección perimetral

## Contexto
El ADR-0016 decidió que el tráfico pasa por un WAF y un gateway, y dejó la **selección del producto pendiente**. Se intentó **Azure Front Door**, la opción mejor encajada, y Azure rechazó crear el recurso: *"Free Trial and Student account is forbidden for Azure Frontdoor resources"*. La suscripción es de estudiante, así que Front Door no está disponible.

Hacían falta un WAF y un gateway que funcionaran en la suscripción, sin un dominio propio ni una cuota fija alta.

## Decisión
- Se despliega **un contenedor `owasp/modsecurity-crs` (nginx con ModSecurity v3 y las reglas OWASP CRS 4.30.0)** como un segundo Container App, `ca-ucokudos-gw`, delante de la API.
- **Como WAF:** las reglas CRS bloquean inyección SQL, XSS y otros ataques comunes. Se comprobó: una inyección SQL y un XSS responden 403 y el tráfico normal pasa.
- **Como gateway:** es la entrada única; el Container App le pone el TLS, nginx reenvía a la API por **HTTPS con verificación del certificado activa** y envía el `Host` del origen.
- La conexión de tiempo real (ADR-0007) lo atraviesa con la cabecera `X-Accel-Buffering: no` que envía la API, para que nginx no retenga los eventos.
- La imagen está fijada a una versión concreta, no a una etiqueta móvil.
- **Falta para cerrar la decisión:** poner la API como **solo interna**, para que el WAF no se pueda saltar llamando directo a la URL pública del Container App. Se hace cuando el front use la URL del gateway.
- **Limitación declarada:** la imagen no trae límite de peticiones. Requiere una imagen propia con una plantilla de nginx, o una regla en la propia API.

## Alternativas consideradas

| Opción | Costos (precio oficial, consulta 2026-11-16) | Complejidad | Escalabilidad | Riesgos | Mantenibilidad | Evolución futura |
|---|---|---|---|---|---|---|
| **nginx con ModSecurity y CRS en Container Apps** | Contenedor de 0,25 vCPU y 0,5 GiB: vCPU 0,000024 USD por segundo en uso y 0,000003 en reposo; memoria 0,000003 USD por GiB y segundo (**unos 6 USD al mes en reposo y hasta unos 20 USD si estuviera siempre activo**, cálculo del autor) | Media | Media: se escala con réplicas | Medio: lo operamos nosotros, y las reglas pueden dar falsos positivos | Media | Media: migrable a un WAF gestionado |
| Azure Front Door Standard con WAF | **35 USD al mes** de tarifa base más solicitudes y transferencia | Media | Alta | **Descartada: Azure no permite Front Door en cuentas de prueba o de estudiante** | Alta | Alta |
| Azure Front Door Premium (reglas administradas) | **330 USD al mes** de tarifa base | Media | Alta | Descartada por la misma razón y por el costo | Alta | Alta |
| WAF en Application Gateway v2 | **0,443 USD por hora fija (323,39 USD al mes)** más 0,0144 USD por hora por unidad de capacidad (10,51 USD al mes) | Alta: exige una red virtual | Alta | Medio: costo muy alto para la entrega | Media | Alta |
| API Management, plan de consumo | Primeras 1 000 000 de llamadas gratis y 4,20 USD por millón después | Media | Alta | Medio: **no es un WAF**, y puede cortar el flujo de notificaciones en tiempo real (no verificado) | Alta | Alta |
| API Management Developer | 0,0658 USD por hora (48,03 USD al mes) | Media | Media | Medio: tampoco es un WAF y no es para producción | Alta | Alta |
| WAF de Cloudflare (plan gratuito) | Sin costo de lista (**No verificado** en esta consulta) | Media | Alta | Alto: exige apuntar un dominio propio, que no tenemos (RT-25) | Alta | Alta |
| Sin WAF ni gateway | Sin costo | Baja | Baja | Alto | Media | Baja |

## Consecuencias
- **Positivas:** hay WAF y gateway funcionando con costo de pocos dólares al mes; el diseño del ADR-0016 se cumple; se puede cambiar por un WAF gestionado más adelante sin tocar el código de la API.
- **Negativas:** el WAF lo operamos nosotros; las reglas pueden bloquear tráfico legítimo (por ejemplo cargas multipart de adjuntos) y requieren ajuste; sin límite de peticiones; mientras la API siga pública, el WAF se puede esquivar.
- **Pendiente:** probar el flujo completo con sesión (publicar con adjunto, notificación en vivo), cerrar el acceso directo a la API y decidir el límite de peticiones.

## Registro de cambios
- 2026-11-16: creación. Reemplaza la selección de producto del ADR-0016 tras el rechazo de Azure Front Door.
