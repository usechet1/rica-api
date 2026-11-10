# Evidencias — Taller 9: consumidores robustos (EDA)

Salidas reales tomadas el 2026-11-10 12:34 sobre consultas-service.
Commit del código: `bc95ec8`.

## 1. Mensaje venenoso → Dead Letter Topic (paso 3)

```
$ echo 'esto no es json - evidencia 12:34' | docker exec -i rica-kafka /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic investigadores.registrados
(enviado)
```

```
$ docker exec -i rica-kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic investigadores.registrados.DLT --from-beginning --property print.headers=true --timeout-ms 5000   # resumido: cabeceras de diagnóstico del último mensaje
kafka_dlt-exception-fqcn:org.springframework.kafka.listener.ListenerExecutionFailedException
kafka_dlt-exception-cause-fqcn:tools.jackson.core.exc.StreamReadException
kafka_dlt-original-topic:investigadores.registrados
kafka_dlt-original-consumer-group:consultas-service-v2
esto no es json - evidencia 12:34
```

```
$ # ¿siguió funcionando el consumidor? se registra un investigador después del mensaje venenoso
POST /api/investigadores (Ruben Diaz) -> 201
{"correo":"ruben.diaz@uptc.edu.co","nombreCompleto":"Ruben Diaz","estado":"ACTIVO","totalPublicaciones":0,"publicaciones":[]}  -> HTTP 200
```

## 2. Consumidor idempotente: repetir todos los eventos no cambia el contador (paso 6)

```
$ curl -s localhost:8083/api/consultas/estadisticas   # antes de repetir
{"investigadoresRegistrados":15}
```

```
$ docker exec -i rica-kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group consultas-estadisticas --topic investigadores.registrados --reset-offsets --to-earliest --execute

GROUP                  TOPIC                      PARTITION  NEW-OFFSET
consultas-estadisticas investigadores.registrados 0          0
```

```
$ curl -s localhost:8083/api/consultas/estadisticas   # después de repetir TODOS los eventos
{"investigadoresRegistrados":15}
```

```
$ # log de consultas-service (EstadisticasListener) durante la repetición, primeras líneas
2026-11-10T12:35:09.694 INFO [consultas-service,] Estadísticas: evento investigadores.registrados-0-0 ya procesado, se ignora
2026-11-10T12:35:09.696 INFO [consultas-service,] Estadísticas: evento investigadores.registrados-0-1 ya procesado, se ignora
2026-11-10T12:35:09.697 INFO [consultas-service,] Estadísticas: evento investigadores.registrados-0-2 ya procesado, se ignora
2026-11-10T12:35:09.697 WARN [consultas-service,] Estadísticas: mensaje no válido en investigadores.registrados-0@3, no se cuenta
2026-11-10T12:35:09.698 INFO [consultas-service,] Estadísticas: evento investigadores.registrados-0-4 ya procesado, se ignora
2026-11-10T12:35:09.698 WARN [consultas-service,] Estadísticas: mensaje no válido en investigadores.registrados-0@5, no se cuenta
2026-11-10T12:35:09.698 WARN [consultas-service,] Estadísticas: mensaje no válido en investigadores.registrados-0@6, no se cuenta
2026-11-10T12:35:09.699 INFO [consultas-service,] Estadísticas: evento investigadores.registrados-0-7 ya procesado, se ignora
...
total de eventos ignorados por ya procesados: 15
```

