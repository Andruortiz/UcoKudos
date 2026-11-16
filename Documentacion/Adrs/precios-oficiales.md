# Precios oficiales usados en los ADR

Fecha de consulta: **16 de noviembre de 2026**. Moneda: dólares estadounidenses (USD), sin impuestos. Los precios cambian: cada ADR remite a esta tabla y debe volver a consultarse antes de la sustentación.

## Cómo se obtuvieron

| Origen | Método | Qué cubre |
|---|---|---|
| **API de precios de Azure** (`https://prices.azure.com/api/retail/prices`) | Consulta por servicio, región y SKU, con la moneda USD. Es la fuente oficial de Microsoft para el precio de lista | Todos los servicios de Azure |
| **Página oficial de Twilio** (`https://www.twilio.com/en-us/sms/pricing/co`) | Lectura de la página, que declara "Pricing current as of November 2026" | SMS a Colombia |
| **Documentación oficial de Meta** (`https://developers.facebook.com/docs/whatsapp/pricing`) | Lectura de la página | Modelo de cobro de WhatsApp |

Los meses se calculan con 730 horas (2 628 000 segundos). Los totales mensuales son **cálculo del autor** a partir del precio unitario oficial, no una cifra publicada por el proveedor.

## Azure

| Servicio | Región | Medidor | Precio oficial | Estimado mensual para este proyecto |
|---|---|---|---|---|
| Container Apps (consumo) | Central US | vCPU en uso | 0,000024 USD por segundo | API de 1 vCPU y 2 GiB con una réplica: unos 24 USD si pasa casi todo el tiempo en reposo y hasta unos 79 USD si estuviera siempre activa |
| | | vCPU en reposo | 0,000003 USD por segundo | |
| | | Memoria (en uso o en reposo) | 0,000003 USD por GiB y segundo | |
| | | Solicitudes | 0,40 USD por millón | |
| PostgreSQL Flexible Server, B1ms | Central US | Cómputo | 0,01921 USD por hora | 14,02 USD |
| | | Almacenamiento | 0,13 USD por GB al mes | 32 GB: 4,16 USD |
| Static Web Apps | Central US | Plan Standard, aplicación | 9 USD al mes | El plan **Free** no tiene medidores; se usa el Free |
| Blob Storage, acceso frecuente, LRS | East US 2 | Datos almacenados | 0,0184 USD por GB al mes | Unos centavos para los adjuntos de la demostración |
| | | Operaciones de escritura | 0,05 USD por 10 000 | |
| Key Vault, Standard | East US 2 | Operaciones | 0,03 USD por 10 000 | Menos de 1 USD |
| Log Analytics | Central US | Ingesta | Primeros 5 GB gratis; después 2,76 USD por GB | 0 USD con el volumen actual |
| Service Bus, Standard | Central US | Unidad base | 0,013441 USD por hora (10 USD al mes), más operaciones y conexiones | 10 USD (solo si se usara un bus; el proyecto usa una bandeja de salida) |
| Azure Front Door, Standard | Global | Tarifa base | 35 USD al mes (más solicitudes y transferencia) | 35 USD |
| Azure Front Door, Premium | Global | Tarifa base | 330 USD al mes (más solicitudes y transferencia) | 330 USD |
| Application Gateway WAF v2 | Central US | Costo fijo | 0,443 USD por hora | 323,39 USD |
| | | Unidad de capacidad | 0,0144 USD por hora | 10,51 USD por unidad |
| API Management, Consumo | Central US | Llamadas | Primeras 1 000 000 gratis; después 0,042 USD por 10 000 (4,20 USD por millón) | 0 USD con poco tráfico |
| API Management, Developer | Central US | Unidad | 0,0658 USD por hora | 48,03 USD |
| API Management, Standard | Central US | Unidad | 0,9407 USD por hora | 686,71 USD |
| Defender for Storage, análisis de malware | Varias regiones | Datos analizados | 0,15 USD por GB | Centavos con los adjuntos de la demostración; **la cuota fija por cuenta no se obtuvo** de la API y debe leerse en el portal |

**Pendiente de verificar en el portal:** la cuota gratuita mensual de Container Apps (la documentación de Azure la describe en vCPU-segundos, GiB-segundos y solicitudes, pero no figura en la API de precios) y la cuota fija por cuenta de Defender for Storage.

## Terceros

| Servicio | Precio oficial | Nota |
|---|---|---|
| Twilio, SMS saliente a Colombia | **0,0592 USD por segmento**. Número internacional desde 1,15 USD al mes | La página no desglosa por operador y advierte que pueden aplicar cargos adicionales de operador |
| Vonage, SMS saliente a Colombia | **No verificado.** La página de precios de Vonage exige elegir el país de forma interactiva y la API de precios de la cuenta de prueba responde "Forbidden" | Debe consultarse en el panel de Vonage con una cuenta de pago |
| WhatsApp Business Platform (Meta) | Cobro por mensaje entregado, según categoría (marketing, utilidad, autenticación) y país. Un nivel gratuito de 1 000 mensajes de servicio por número y mes. Las tarifas de Colombia se publican en archivos CSV y PDF en COP y USD | No se obtuvo la tarifa de Colombia: la página no la lista |
| Microsoft Entra ID, invitados B2B | Sin cifra obtenida en esta consulta | Verificar en la página de precios de Microsoft Entra |
| Microsoft Graph, invitaciones | Sin cifra obtenida en esta consulta | Verificar en la documentación de Microsoft |
| GitHub Actions y SonarCloud | Sin cifra obtenida en esta consulta | Verificar en las páginas de GitHub y de SonarSource |
| Correo SMTP de Gmail | Sin costo para el proyecto; límites de envío no verificados | Verificar la documentación de Google |

## Reglas para usar estos precios

- Un ADR cita el precio con su **medidor, unidad y fecha**; no escribe "bajo" o "por verificar" si la cifra está aquí.
- Si la cifra no se pudo obtener, el ADR lo dice con las palabras **"No verificado"** y nombra dónde consultarla.
- Si se vuelve a consultar y el precio cambió, se agrega una fila nueva con la fecha. No se borran las cifras anteriores, porque el ADR que las usó las citó.
