# Evidencias — Taller 8: CQRS con consultas-service

Salidas reales tomadas el 2026-11-10 12:33. Todos los comandos pasan por el Gateway (8000)
salvo cuando se indica. Commit del código: `bc95ec8`.

## 1. Comandos (lado de escritura)

```
$ # registro de Paula Herrera y dos publicaciones, por el gateway
POST /api/investigadores (Paula Herrera) -> 201
POST /api/publicaciones (Modelos de lectura) -> 201
POST /api/publicaciones (Proyecciones con Kafka) -> 201
```

## 2. La ficha: investigador + publicaciones en una sola consulta (paso 7.3)

```
$ curl -s localhost:8000/api/consultas/investigadores/paula.herrera@uptc.edu.co | python3 -m json.tool
{
    "correo": "paula.herrera@uptc.edu.co",
    "nombreCompleto": "Paula Herrera",
    "estado": "ACTIVO",
    "totalPublicaciones": 2,
    "publicaciones": [
        {
            "id": "6af355505611f7f3eb802f61",
            "titulo": "Modelos de lectura",
            "anio": 2026
        },
        {
            "id": "6af355505611f7f3eb802f62",
            "titulo": "Proyecciones con Kafka",
            "anio": 2026
        }
    ]
}
```

## 3. Ranking por publicaciones (paso 7.4)

```
$ curl -s 'localhost:8000/api/consultas/ranking?top=3'   # resumido: nombre y total
1. Valentina Lopez (valentina.lopez@uptc.edu.co): 6
2. Luis Mora (luis.mora@uptc.edu.co): 3
3. Paula Herrera (paula.herrera@uptc.edu.co): 2
```

## 4. Lectura caída, escritura intacta (paso 7.5)

```
$ curl -s -o /dev/null -w '%{http_code}\n' localhost:8083/api/status   # consultas-service detenido
000
```

```
$ # con consultas-service detenido, el comando de escritura responde igual
POST /api/publicaciones (Consistencia eventual) -> 201
```

```
$ curl -s localhost:8083/api/consultas/investigadores/paula.herrera@uptc.edu.co   # al reiniciar consultas-service: totalPublicaciones
totalPublicaciones: 3
```

## 5. Log de consultas-service con las líneas Proyección (pasos 7.2 a 7.5)

```
$ # log de consultas-service (ProyectorFichas), filtrado por paula.herrera
2026-11-10T12:33:02.753 INFO [consultas-service,] Proyección: ficha de paula.herrera@uptc.edu.co actualizada (registro)
2026-11-10T12:33:04.107 INFO [consultas-service,] Proyección: ficha de paula.herrera@uptc.edu.co ahora tiene 1 publicaciones
2026-11-10T12:33:04.124 INFO [consultas-service,] Proyección: ficha de paula.herrera@uptc.edu.co ahora tiene 2 publicaciones
2026-11-10T12:33:16.010 INFO [consultas-service,] Proyección: ficha de paula.herrera@uptc.edu.co ahora tiene 3 publicaciones
```

## 6. La Saga limpia la ficha (paso 7.6)

```
$ curl -s -X DELETE localhost:8000/api/investigadores/14   # -> 202
$ curl -s 'localhost:8000/api/consultas/investigadores?estado=EN_ELIMINACION'   # consultado enseguida
[{"correo":"paula.herrera@uptc.edu.co","nombreCompleto":"Paula Herrera","estado":"EN_ELIMINACION","totalPublicaciones":3,"publicaciones":[{"id":"6af355505611f7f3eb802f61","titulo":"Modelos de lectura","anio":2026},{"id":"6af355505611f7f3eb802f62","titulo":"Proyecciones con Kafka","anio":2026},{"id":"6af355595611f7f3eb802f63","titulo":"Consistencia eventual","anio":2026}]}]

```

```
$ curl -s -w '  -> HTTP %{http_code}' localhost:8000/api/consultas/investigadores/paula.herrera@uptc.edu.co   # cuando termina la Saga
{"mensaje":"No hay ficha para el investigador paula.herrera@uptc.edu.co"}  -> HTTP 404
```

```
$ # log de consultas-service durante la Saga
2026-11-10T12:33:42.943 INFO [consultas-service,] Proyección: paula.herrera@uptc.edu.co marcado EN_ELIMINACION
2026-11-10T12:33:44.972 INFO [consultas-service,] Proyección: ficha de paula.herrera@uptc.edu.co eliminada
```

## 7. Reconstruir el modelo de lectura (paso 8)

Se detuvo consultas-service, se borró la colección de fichas y se regresó el grupo de consumidores al inicio de los tópicos (mismo efecto que cambiar el `group-id`: el grupo no recuerda nada y relee todos los eventos).

```
$ docker exec publicaciones-mongo mongosh rica_consultas --quiet --eval 'db.fichas_investigador.drop(); db.fichas_investigador.countDocuments()'
0
```

```
$ docker exec -i rica-kafka /opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group consultas-service-v2 --all-topics --reset-offsets --to-earliest --execute

GROUP                TOPIC                         PARTITION  NEW-OFFSET
consultas-service-v2 publicaciones.registradas     0          0
consultas-service-v2 investigadores.registrados    0          0
consultas-service-v2 investigadores.en-eliminacion 0          0
consultas-service-v2 investigadores.eliminados     0          0
```

```
$ curl -s localhost:8083/api/consultas/investigadores   # resumido: correo, nombre, estado y total
ana.torres@uptc.edu.co | Ana Torres | ACTIVO | 1
luis.mora@uptc.edu.co | None | ACTIVO | 2
diana.rojas@uptc.edu.co | Diana Rojas | ACTIVO | 0
mateo.diaz@uptc.edu.co | Mateo Diaz | ACTIVO | 2
camila.ortiz@uptc.edu.co | Camila Ortiz | ACTIVO | 1
valentina.lopez@uptc.edu.co | Valentina Lopez | ACTIVO | 6
andres.molina@uptc.edu.co | Andres Molina | ACTIVO | 1
paula.herrera@uptc.edu.co | Paula Herrera | ACTIVO | 3
jorge.paz@uptc.edu.co | Jorge Paz | ACTIVO | 0
elena.cruz@uptc.edu.co | Elena Cruz | ACTIVO | 0
felipe.rios@uptc.edu.co | Felipe Rios | ACTIVO | 0
laura.gil@uptc.edu.co | Laura Gil | ACTIVO | 0
ivan.soto@uptc.edu.co | Ivan Soto | ACTIVO | 0
nora.vega@uptc.edu.co | Nora Vega | ACTIVO | 0
```

Las 14 fichas reaparecieron sin ejecutar ningún comando: el modelo de lectura no es la fuente
de verdad, se reconstruye solo con los eventos de Kafka.

**La trampa de reproducir eventos, vista en mis datos:** Mateo, Camila, Andrés y Paula ya
estaban eliminados (su Saga terminó), pero en la reconstrucción volvieron como `ACTIVO`, y
Luis aparece sin nombre (`None`). No es un error del proyector: `investigadores.registrados`,
`investigadores.en-eliminacion` e `investigadores.eliminados` son tópicos distintos y Kafka
no garantiza el orden entre tópicos. Al releer todo, el evento "eliminado" se procesó antes
que el "registrado", así que la ficha se borró y después se volvió a crear. Se evitaría
publicando todos los eventos de un mismo investigador en un solo tópico, con número de versión.

```
$ # log de consultas-service durante la reconstrucción (orden real de procesamiento)
2026-11-10T12:33:57.198 INFO [consultas-service,] Proyección: mateo.diaz@uptc.edu.co marcado EN_ELIMINACION
2026-11-10T12:33:57.202 INFO [consultas-service,] Proyección: ficha de mateo.diaz@uptc.edu.co eliminada
2026-11-10T12:33:57.213 INFO [consultas-service,] Proyección: ficha de mateo.diaz@uptc.edu.co ahora tiene 1 publicaciones
2026-11-10T12:33:57.215 INFO [consultas-service,] Proyección: ficha de mateo.diaz@uptc.edu.co ahora tiene 2 publicaciones
2026-11-10T12:33:58.749 INFO [consultas-service,] Proyección: ficha de mateo.diaz@uptc.edu.co actualizada (registro)
```
