# Evidencias — Taller 6: comunicación entre servicios

Salidas reales tomadas el 2026-11-10 12:31 con los 5 servicios corriendo
(eureka-server, investigadores-service, publicaciones-service, consultas-service, gateway)
y Docker con `investigadores-db`, `rica-kafka` y `publicaciones-mongo`.
Commit del código: `bc95ec8`.

## 1. Eureka: servicios registrados (paso 4)

```
$ curl -s -H 'Accept: application/json' localhost:8761/eureka/apps   # resumido: nombre, estado e ip:puerto
GATEWAY ['UP 192.168.84.116:8000']
PUBLICACIONES-SERVICE ['UP 192.168.84.116:8081']
CONSULTAS-SERVICE ['UP 192.168.84.116:8083']
INVESTIGADORES-SERVICE ['UP 192.168.84.116:8080']
```

## 2. gRPC: servidor en investigadores-service, puerto 9090 (paso 2A)

```
$ curl -s -X POST localhost:8080/api/investigadores -H "Content-Type: application/json" -d '{"nombreCompleto":"Valentina Lopez","correoInstitucional":"valentina.lopez@uptc.edu.co","grupoInvestigacion":"GIT-UPTC"}'
{"id":12,"nombreCompleto":"Valentina Lopez","correoInstitucional":"valentina.lopez@uptc.edu.co","grupoInvestigacion":"GIT-UPTC"}
```

```
$ docker run --rm fullstorydev/grpcurl -plaintext host.docker.internal:9090 list
InvestigadorGrpc
grpc.health.v1.Health
grpc.reflection.v1.ServerReflection
```

```
$ echo '{"correoInstitucional":"valentina.lopez@uptc.edu.co"}' | docker run --rm -i fullstorydev/grpcurl -plaintext -d @ host.docker.internal:9090 InvestigadorGrpc/ExisteInvestigador
{
  "existe": true
}
```

```
$ echo '{"correoInstitucional":"no.existe@uptc.edu.co"}' | docker run --rm -i fullstorydev/grpcurl -plaintext -d @ host.docker.internal:9090 InvestigadorGrpc/ExisteInvestigador
{}
```

## 3. gRPC: cliente en publicaciones-service (VerificadorInvestigadorGrpc, paso 2B)

Con un correo que existe responde 201; con un correo inventado, 404 (en el taller 5 respondía 201).

```
$ curl -s -w '  -> HTTP %{http_code}' -X POST localhost:8081/api/publicaciones -H "Content-Type: application/json" -d '{"titulo":"Contratos gRPC","investigadorCorreo":"valentina.lopez@uptc.edu.co","anio":2026}'
{"id":"6af354ef5611f7f3eb802f5a","titulo":"Contratos gRPC","investigadorCorreo":"valentina.lopez@uptc.edu.co","anio":2026}  -> HTTP 201
```

```
$ curl -s -w '  -> HTTP %{http_code}' -X POST localhost:8081/api/publicaciones -H "Content-Type: application/json" -d '{"titulo":"Fantasma","investigadorCorreo":"no.existe@uptc.edu.co","anio":2026}'
{"mensaje":"No existe un investigador con correo no.existe@uptc.edu.co"}  -> HTTP 404
```

## 4. API Gateway: el puerto 8000 responde lo mismo que el 8080 (paso 5)

```
$ curl -s localhost:8000/api/investigadores/12
{"id":12,"nombreCompleto":"Valentina Lopez","correoInstitucional":"valentina.lopez@uptc.edu.co","grupoInvestigacion":"GIT-UPTC"}
```

```
$ diff <(curl -s localhost:8000/api/investigadores) <(curl -s localhost:8080/api/investigadores) && echo 'IGUALES: el gateway enruta /api/investigadores a investigadores-service'
IGUALES: el gateway enruta /api/investigadores a investigadores-service
```

## 5. Transactional Outbox + Kafka (paso 6)

```
$ docker exec investigadores-db psql -U rica -d rica -c "SELECT id, topico, clave, publicado FROM outbox_event ORDER BY id DESC LIMIT 3;"
 id |            topico             |            clave            | publicado 
----+-------------------------------+-----------------------------+-----------
 18 | investigadores.registrados    | valentina.lopez@uptc.edu.co | t
 17 | investigadores.eliminados     | camila.ortiz@uptc.edu.co    | t
 16 | investigadores.en-eliminacion | camila.ortiz@uptc.edu.co    | t
(3 rows)

```

```
$ # log de investigadores-service (OutboxPoller)
Evento 18 publicado en investigadores.registrados
```

```
$ # log de publicaciones-service (InvestigadorRegistradoListener)
Evento recibido [investigadores.registrados]: {"correlationId":"ad4bcd45-6c5a-47db-b585-fcfd82502b2e","id":12,"correoInstitucional":"valentina.lopez@uptc.edu.co","nombreCompleto":"Valentina Lopez"}
```

## 6. Saga coreografiada: eliminar un investigador (paso 7)

```
$ curl -s -w '  -> HTTP %{http_code}' -X POST localhost:8081/api/publicaciones -H "Content-Type: application/json" -d '{"titulo":"Sagas sin transacciones distribuidas","investigadorCorreo":"andres.molina@uptc.edu.co","anio":2026}'
{"id":"6af355175611f7f3eb802f5b","titulo":"Sagas sin transacciones distribuidas","investigadorCorreo":"andres.molina@uptc.edu.co","anio":2026}  -> HTTP 201
```

```
$ curl -s -i -X DELETE -H 'X-Correlation-Id: evid-saga-06' localhost:8000/api/investigadores/13 | head -1
HTTP/1.1 202 Accepted
```

```
$ # logs de investigadores-service y publicaciones-service, en orden
2026-11-10T12:32:07.747 INFO [investigadores-service,evid-saga-06] Saga: iniciando eliminación de andres.molina@uptc.edu.co (estado EN_ELIMINACION)
2026-11-10T12:32:08.303 INFO [publicaciones-service,evid-saga-06] Saga: 1 publicaciones de andres.molina@uptc.edu.co marcadas como huérfanas
2026-11-10T12:32:08.319 INFO [investigadores-service,evid-saga-06] Saga: publicaciones confirmó para andres.molina@uptc.edu.co. Eliminando investigador.
```

```
$ docker exec investigadores-db psql -U rica -d rica -c "SELECT count(*) FROM investigador WHERE correo_institucional='andres.molina@uptc.edu.co';"
 count 
-------
     0
(1 row)
```

```
$ docker exec publicaciones-mongo mongosh rica --quiet --eval 'db.publicaciones.find({investigadorCorreo:"andres.molina@uptc.edu.co"},{_id:0,titulo:1,huerfana:1}).toArray()'
[ { titulo: 'Sagas sin transacciones distribuidas', huerfana: true } ]
```

## 7. Circuit Breaker (paso 3): investigadores-service detenido

```
$ for i in 1 2 3 4 5 6; do curl -s -w '  -> HTTP %{http_code}\n' -X POST localhost:8081/api/publicaciones -H "Content-Type: application/json" -d '{"titulo":"CB","investigadorCorreo":"valentina.lopez@uptc.edu.co","anio":2024}'; done
{"mensaje":"No se pudo contactar a investigadores-service: UNAVAILABLE"}  -> HTTP 503
{"mensaje":"No se pudo contactar a investigadores-service: UNAVAILABLE"}  -> HTTP 503
{"mensaje":"No se pudo contactar a investigadores-service: UNAVAILABLE"}  -> HTTP 503
{"mensaje":"investigadores-service no está disponible (circuito abierto)"}  -> HTTP 503
{"mensaje":"investigadores-service no está disponible (circuito abierto)"}  -> HTTP 503
{"mensaje":"investigadores-service no está disponible (circuito abierto)"}  -> HTTP 503
```

Después se volvió a arrancar investigadores-service, se esperaron 10 s y se enviaron 10 peticiones (cualquier respuesta que no sea 503 cuenta como éxito):

```
$ for i in $(seq 1 10); do curl -s -o /dev/null -w '%{http_code} ' -X POST localhost:8081/api/publicaciones -H "Content-Type: application/json" -d '{"titulo":"Recuperacion","investigadorCorreo":"valentina.lopez@uptc.edu.co","anio":2023}'; done
201 201 201 201 201 409 409 409 409 409 
```

```
$ # log de publicaciones-service (VerificadorInvestigadorGrpc)
2026-11-10T12:32:16.248 WARN [publicaciones-service,] Circuit Breaker investigadores-service: State transition from CLOSED to OPEN
2026-11-10T12:32:32.388 WARN [publicaciones-service,] Circuit Breaker investigadores-service: State transition from OPEN to HALF_OPEN
2026-11-10T12:32:32.493 WARN [publicaciones-service,] Circuit Breaker investigadores-service: State transition from HALF_OPEN to CLOSED
```

