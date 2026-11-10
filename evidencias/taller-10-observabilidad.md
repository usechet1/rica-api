# Evidencias — Taller 10: observabilidad

Salidas reales tomadas el 2026-11-10 12:36. Commit del código: `bc95ec8`.

## 1. Salud de los tres servicios (paso 3)

```
$ curl -s localhost:8080/actuator/health   # resumido: estado general y componentes
status: UP
   db -> UP
   discoveryComposite -> UP
   diskSpace -> UP
   livenessState -> UP
   ping -> UP
   readinessState -> UP
   refreshScope -> UP
   ssl -> UP
```

```
$ curl -s localhost:8081/actuator/health   # resumido: estado general y componentes
status: UP
   discoveryComposite -> UP
   diskSpace -> UP
   livenessState -> UP
   mongo -> UP
   ping -> UP
   readinessState -> UP
   refreshScope -> UP
   ssl -> UP
```

```
$ curl -s localhost:8083/actuator/health   # resumido: estado general y componentes
status: UP
   discoveryComposite -> UP
   diskSpace -> UP
   livenessState -> UP
   mongo -> UP
   ping -> UP
   readinessState -> UP
   refreshScope -> UP
   ssl -> UP
```

## 2. El filtro de correlación respeta o genera el id (paso 4)

```
$ curl -s -i -H 'X-Correlation-Id: evid-demo-001' localhost:8080/api/investigadores | grep -i x-correlation-id
X-Correlation-Id: evid-demo-001
```

```
$ curl -s -i localhost:8080/api/investigadores | grep -i x-correlation-id   # sin cabecera: genera un UUID
X-Correlation-Id: fd97f984-c8d6-4d80-aab3-ec06bb0fe4f9
```

## 3. Una Saga completa siguiendo un solo id (paso 6)

Eliminación de Andrés Molina por el gateway con `X-Correlation-Id: evid-saga-06` (ver también taller-06). Buscando ese id en los logs:

```
$ # logs de investigadores-service y publicaciones-service con evid-saga-06
2026-11-10T12:32:07.747 INFO [investigadores-service,evid-saga-06] Saga: iniciando eliminación de andres.molina@uptc.edu.co (estado EN_ELIMINACION)
2026-11-10T12:32:08.303 INFO [publicaciones-service,evid-saga-06] Saga: 1 publicaciones de andres.molina@uptc.edu.co marcadas como huérfanas
2026-11-10T12:32:08.319 INFO [investigadores-service,evid-saga-06] Saga: publicaciones confirmó para andres.molina@uptc.edu.co. Eliminando investigador.
```

```
$ # log de consultas-service para la misma Saga: sin id (brecha anotada en NOTAS.md)
2026-11-10T12:32:08.289 INFO [consultas-service,] Proyección: andres.molina@uptc.edu.co marcado EN_ELIMINACION
2026-11-10T12:32:10.316 INFO [consultas-service,] Proyección: ficha de andres.molina@uptc.edu.co eliminada
```

## 4. Métricas HTTP (método RED) y del pool de conexiones (paso 7)

```
$ curl -s localhost:8080/actuator/metrics/http.server.requests   # resumido
http.server.requests {'COUNT': 14.0, 'TOTAL_TIME': 0.1136, 'MAX': 0.0128} seconds
```

```
$ curl -s 'localhost:8080/actuator/metrics/http.server.requests?tag=status:404'   # resumido
http.server.requests {'COUNT': 1.0, 'TOTAL_TIME': 0.0034, 'MAX': 0.0034} seconds
```

```
$ curl -s localhost:8080/actuator/metrics/hikaricp.connections.active   # resumido
hikaricp.connections.active {'VALUE': 0.0} 
```

## 5. Lag de los consumidores (paso 7)

```
$ docker exec -i rica-kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group publicaciones-service   # columnas: tópico, offset actual, final, LAG
investigadores.registrados 19 19 LAG=0
investigadores.en-eliminacion 5 5 LAG=0
```

