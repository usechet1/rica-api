# Evidencias de ejecución de los talleres

Salidas reales de comandos (curl, docker, grpcurl, logs) tomadas con el sistema corriendo.
Cada archivo indica la fecha y el commit del código con el que se tomaron.

| Taller | Archivo | Qué muestra |
|---|---|---|
| 6 · Comunicación | [taller-06-comunicacion.md](taller-06-comunicacion.md) | Eureka con los servicios en UP, gRPC (grpcurl y 404 con correo inventado), Gateway, Outbox + Kafka, Saga con su id y Circuit Breaker CLOSED → OPEN → HALF_OPEN → CLOSED |
| 8 · CQRS | [taller-08-cqrs.md](taller-08-cqrs.md) | Ficha JSON con sus publicaciones, ranking, log `Proyección:`, lectura caída, 404 después de la Saga y reconstrucción del modelo de lectura |
| 9 · EDA | [taller-09-eda.md](taller-09-eda.md) | Mensaje venenoso en `investigadores.registrados.DLT` con sus cabeceras y contador idempotente que no cambia al repetir los eventos |
| 10 · Observabilidad | [taller-10-observabilidad.md](taller-10-observabilidad.md) | `/actuator/health` en UP de los 3 servicios, `X-Correlation-Id`, Saga seguida por un solo id, métricas y lag |
| 11 · Contenedores y CI | [taller-11-contenedores.md](taller-11-contenedores.md), `taller-11-actions-rojo.png` y `taller-11-actions-lista.png` | Imagen Docker multi-etapa y ejecuciones de GitHub Actions en verde, rojo y verde |

Las respuestas escritas de cada taller están en [`../NOTAS.md`](../NOTAS.md).
