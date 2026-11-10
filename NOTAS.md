# NOTAS.md — Taller DDD en rica-api

## 1. Lenguaje Ubicuo: auditoría rápida

**¿Hay algún campo o método con nombre genérico (data, info, value, item) que debería
tener un nombre del dominio?**
Me puse a revisar `InvestigadorController`, `InvestigadorRequest` e
`InvestigadorResponse` buscando justamente eso, y la verdad no encontré nada. Los
campos (`nombreCompleto`, `correoInstitucional`, `grupoInvestigacion`) ya hablan el
idioma del dominio académico, no suenan a jerga de sistemas. Lo único que a primera
vista parece "técnico" es `id`, pero ni siquiera cuenta: es un identificador estándar,
no representa ningún concepto de negocio que necesite nombre propio.

**¿`correoInstitucional` y `grupoInvestigacion` son términos que reconocería alguien de
la Facultad sin que se los tradujeran?**
Sin duda. Cualquier coordinador de investigación usa esas mismas palabras todos los
días: "correo institucional" es el correo @uptc.edu.co con el que se identifica a cada
investigador, y "grupo de investigación" es la unidad que reconoce oficialmente la
Facultad (por ejemplo, GIT-UPTC). Así que no toqué nada aquí. Y creo que esa es parte de
la lección del Lenguaje Ubicuo: no siempre hay que renombrar, a veces el trabajo es solo
confirmar que ya está bien dicho y dejar la constancia de por qué.

**¿Por qué `investigadorCorreo` es más preciso que `investigadorId` o `autorId`?**
Porque el nombre mismo del campo ya cuenta la historia: la referencia entre Agregados
se hace por el correo institucional, que es el identificador natural y estable del
investigador en este dominio, no por un id técnico de base de datos ni por el rol
genérico de "autor". Si le hubiera puesto `autorId`, estaría escondiendo dos cosas al
mismo tiempo: qué tipo de identificador se usa realmente, y el hecho de que en RICA
"autor" y "correo institucional" son, ni más ni menos, el mismo concepto. Con
`investigadorCorreo` cualquiera que lea el código entiende de una vez qué se está
referenciando y cómo, sin tener que ir a buscar en la implementación.

## 4. Límite del Agregado Investigador

- **¿Cuál es la raíz del Agregado Investigador?** `Investigador`. Es fácil de ver: es la
  única clase con `@Entity` en todo el paquete `investigadores`.
- **¿Qué vive dentro del límite?** `id`, `nombreCompleto`, `correoInstitucional` (que
  ahora es `CorreoInstitucional`, un Value Object) y `grupoInvestigacion`.
- **¿Por qué `Publicacion` NO está dentro de este límite?** Porque un investigador
  puede terminar con decenas de publicaciones acumuladas a lo largo de los años, y
  meterlas dentro del Agregado rompería la regla de Vernon de "Agregados pequeños". En
  la práctica significaría cargar y bloquear transacciones enormes por cada cambio
  chiquito a un investigador; actualizar algo tan simple como `grupoInvestigacion` no
  debería arrastrar ni bloquear todas sus publicaciones.
- **¿Qué pasaría si alguien agrega `List<Publicacion> publicaciones` directo en
  `Investigador`?** Se rompería el límite por varios lados a la vez: dos transacciones
  concurrentes sobre el mismo `Investigador` podrían pisarse la lista de publicaciones
  entre sí, el Agregado dejaría de ser pequeño, y `publicaciones` (que hoy vive en
  MongoDB, separado de `investigadores` en PostgreSQL) perdería la posibilidad de
  evolucionar o desplegarse por su cuenta. Básicamente estaríamos deshaciendo a mano
  una separación que ya tenía sentido.

## Resumen de lo que fui aplicando en el código

1. **Lenguaje Ubicuo** — lo auditado arriba. No cambié nombres porque, revisando con
   calma, ya estaban bien puestos.
2. **Value Object** — `investigadores/CorreoInstitucional.java`. La idea es que sea
   imposible construir un correo institucional inválido; `Investigador` lo usa vía
   `@Embedded`.
3. **Servicio de Dominio** — `publicaciones/LimitePublicacionesAnualesService.java`.
   Aplica la regla de "máximo 5 publicaciones por año" apoyándose en
   `PublicacionRepository.countByInvestigadorCorreoAndAnio`. Lo dejé viviendo en
   `publicaciones` y no en `investigadores`, para no invertir la dirección de
   dependencia que ya existía entre esos dos paquetes.
4. **Límite del Agregado** — ya documentado arriba. Aquí no hubo que tocar código,
   porque el límite ya se respetaba desde antes.
5. **Factory** — `investigadores/InvestigadorFactory.java`. Es el único punto por el
   que se puede construir un `Investigador`; valida el correo y rechaza duplicados de
   forma atómica. `InvestigadorService.registrar(...)` simplemente le delega el trabajo.
6. **Domain Event (el reto extra)** — `InvestigadorRegistrado` junto con
   `InvestigadorRegistradoListener`. Se publica cada vez que se crea un investigador;
   por ahora el "otro lado" del evento es apenas un `log.info(...)`, pero el enganche ya
   queda listo para cuando haya algo más interesante que reaccionar a ese evento.

---

# Taller Lección 4 — Hexagonal en rica-api

## 1. Auditoría: ¿qué era puerto y qué era adaptador antes de tocar nada?

Lo curioso de este paso es que el hexágono ya estaba ahí, a medias, solo que nadie lo
había llamado así. Antes de mover cualquier archivo me senté a ver qué papel cumplía cada
clase.

**`InvestigadorRepository`: ¿puerto primario o secundario?**
Secundario. La pregunta que me ayudó fue "¿quién llama a quién?". Aquí es el propio
sistema el que dice "guárdame esto" o "búscame aquello", y la llamada sale hacia afuera,
hacia la base de datos. Nadie de afuera llama a `InvestigadorRepository`; es el núcleo el
que lo usa. Por eso es secundario.

**`InvestigadorController`: ¿adaptador primario o secundario? ¿Qué tecnología envuelve?**
Primario. Es la puerta de entrada: alguien desde afuera (Postman, un frontend, quien sea)
manda una petición HTTP, y el controller la convierte en algo que el sistema entiende.
Lo que envuelve es HTTP/REST con Spring Web (`@RestController`). La llamada empieza
afuera y entra, justo al revés que el repositorio.

**`InvestigadorService` era una clase sin interfaz: ¿qué faltaba?**
Faltaba el puerto primario: una interfaz que dijera *qué* promete el sistema (registrar,
listar, buscar) sin amarrarse a *cómo* lo hace. El controller dependía directamente de
la clase concreta, así que si algún día quería cambiar la implementación, tenía que tocar
el controller también. Esa interfaz terminó siendo `InvestigadorUseCase`.

**`InvestigadorFactory` (del taller anterior): ¿núcleo o adaptador?**
Núcleo. Me fijé en los imports, como sugiere la guía: no tiene nada de JPA, ni de HTTP,
ni de Mongo. Solo usa clases del propio dominio y el publicador de eventos de Spring, que
es una abstracción, no una tecnología concreta. Si una clase puede vivir sin saber qué
base de datos o qué framework web hay afuera, es núcleo.

## Lo que fui haciendo en el código

1. **Puerto primario**: creé `InvestigadorUseCase` y puse a `InvestigadorService` a
   implementarlo. Ahora el controller depende de la interfaz y no de la clase. Como la
   interfaz promete `listarTodos` y `buscarPorId`, aproveché para agregar los dos GET
   que faltaban; no tenía sentido prometer algo que nadie usa.
2. **Puerto secundario pequeño**: `JpaRepository` trae como treinta métodos y el sistema
   solo necesitaba cuatro. Hice `RepositorioInvestigadores` con esos cuatro (`listarTodos`,
   `buscarPorId`, `existeCorreo`, `guardar`) y un adaptador,
   `InvestigadorRepositoryJpaAdapter`, que por dentro sigue usando Spring Data JPA. No
   borré `InvestigadorRepository`: sigue ahí, pero ya solo lo conoce el adaptador.
3. **Carpetas por rol**: moví todo a `dominio`, `aplicacion` e `infraestructura` (con
   `entrada.web`, `salida.persistencia` y `eventos`). Ahora con solo ver las carpetas se
   entiende qué es el corazón del sistema y qué es "cableado" hacia afuera. Fue el paso
   más tedioso, casi todo por imports que quedaban apuntando al paquete viejo.
4. **Un repositorio falso para probar sin base de datos**: `RepositorioInvestigadoresFalso`
   guarda todo en un `Map`, y `InvestigadorServiceConFalsoTest` usa el servicio real con
   ese falso: sin Spring, sin Mockito, sin Postgres. Corre en milisegundos. Para mí es la
   prueba de que la separación sirve de verdad: al servicio no le importa si detrás hay
   un `Map` o una base de datos.

**Algo que no quedó igual al enunciado:** mi `InvestigadorFactory` también recibe el
publicador de eventos, porque en el taller anterior hice el reto del evento
`InvestigadorRegistrado`. En el test con el falso le paso un publicador que no hace nada
(`evento -> { }`), así sigue sin depender de Spring.

**Publicaciones:** solo le cambié lo necesario para que siguiera compilando.
`PublicacionService` ahora usa el puerto `RepositorioInvestigadores` en lugar del
repositorio de JPA. Darle a publicaciones sus propios puertos era el reto opcional 7-B y
no lo hice.

Al final, por fuera todo responde igual que antes: mismos endpoints, mismas respuestas.
El cambio fue de organización interna, que era justamente la idea del taller.

---

# Taller Lección 5 — Descomposición en dos servicios

## El corte: qué va en cada servicio

| Paquete / clase actual | Destino |
|---|---|
| `investigadores.*` (dominio, aplicacion, infraestructura) | `investigadores-service` |
| `publicaciones.*` | `publicaciones-service` |
| `comun.GlobalExceptionHandler`, `comun.ApiError` | Se duplica en ambos, adaptado: cada servicio maneja solo sus propias excepciones. Ya no hay un único `@RestControllerAdvice` compartido |
| `RecursoNoEncontradoException` | No existía en rica-api. Se crea solo en `publicaciones-service`, para el caso "no existe el investigador" que ahora decide `VerificadorInvestigador`. `investigadores-service` sigue usando su `InvestigadorNoEncontradoException` |
| `plataforma.StatusController` | No existía en rica-api. Cada servicio tiene su propio `/api/status` mínimo |
| `plataforma.CorsConfig`, `ArranqueInformativo`, `SaludoInstitucionalService` | No existen en esta copia de rica-api, así que no hay nada que mover ni descartar |

Diferencias entre mi rica-api y lo que asume la guía:

- **No había migraciones Flyway.** rica-api creaba la tabla con `ddl-auto=update`. En
  `investigadores-service` escribí `V1__crear_tabla_investigador.sql` a partir de la
  entidad `Investigador`, y dejé `ddl-auto: validate` para que Hibernate solo verifique
  que el esquema coincide.
- **La costura no era `existsByCorreoInstitucional_Valor`.** Después del taller
  hexagonal, `PublicacionService` usaba `RepositorioInvestigadores.listarTodos()` y
  filtraba por correo en memoria.
- **`LimitePublicacionesAnualesService` sí cruzaba dominios**: recibía un
  `Investigador`, solo para leer su correo. Ahora recibe solo la `Publicacion` y usa
  `getInvestigadorCorreo()`, así que no conoce ninguna clase de investigadores.

## Duplicar `comun` es una decisión de arquitectura, no un descuido

Es la primera consecuencia real de Database per Service y del despliegue independiente.
Compartir un único `@RestControllerAdvice` exigiría una librería común versionada por
separado (un *shared kernel*, patrón de la guía de Lección 3). Eso acopla el ritmo de
despliegue de los dos servicios. Por ahora, duplicar unas pocas clases de manejo de
errores cuesta menos que ese acoplamiento.

## La dependencia no desapareció: se hizo visible

`PublicacionService` ahora depende del puerto `VerificadorInvestigador`, diseñado desde
publicaciones. Su única implementación, `VerificadorInvestigadorPendiente`, devuelve
siempre `true`. Hoy se puede registrar una publicación con un correo inventado. Esa es
la brecha que cierra el Taller de la Lección 6: hacer que la pregunta pase por la red.

---

# Taller Lección 9 — EDA: consumidores robustos

## Lo que observé

- **Sin defensas:** un mensaje que no era JSON (`esto no es json`) se reintentó 10 veces
  seguidas sin pausa (`currentAttempts=10`, `interval=0`) y después se descartó. El
  consumidor siguió, pero el mensaje se perdió sin dejar rastro.
- **Con la DLT:** el mismo mensaje fue directo a `investigadores.registrados.DLT`, sin
  reintentos (un JSON roto no se arregla esperando), con cabeceras que dicen de qué tópico
  venía y por qué falló (`StreamReadException`).
- **Contador ingenuo:** al regresar el grupo al inicio del tópico, el total pasó de 6 a 12.
  No hubo ni un error en el log; simplemente el número quedó mal.
- **Contador idempotente:** con el mismo escenario, el total se quedó en 6 y el log mostró
  `ya procesado, se ignora` por cada evento repetido.

Un detalle que no estaba en la guía: en Spring Kafka 4 la DLT por defecto se llama
`<tópico>-dlt`, no `<tópico>.DLT`. Tuve que indicarle el nombre a mano en
`ManejoErroresConfig`; si no, el mensaje se iba a otro tópico y la DLT de la guía quedaba vacía.

## Paso 7: ¿qué hacemos con la DLT?

**1. ¿Quién debería enterarse cuando llega un mensaje a la DLT, y cómo?**
El equipo que mantiene `consultas-service`, porque es el que sabe por qué falló su
consumidor. Yo pondría una alerta automática cuando la DLT reciba mensajes (por ejemplo, si
llegan más de cero en cinco minutos) y un tablero con cuántos mensajes hay acumulados. Un
correo por cada mensaje se volvería ruido muy rápido; es mejor una alerta que avise "hay algo
aquí" y un tablero para ver cuánto.

**2. ¿Cómo se reprocesaría un mensaje después de corregir el error?**
Reenviándolo al tópico original, pero con una herramienta aparte y no copiándolo a mano, para
que quede registro de quién lo reenvió y cuándo. Las cabeceras que vi en la DLT
(`original-topic`, `original-partition`, `original-offset`) ya dicen exactamente de dónde
salió. Ojo: solo tiene sentido reenviarlo si el consumidor es idempotente; si no, se podría
procesar dos veces, como pasó con mi contador ingenuo, que saltó de 6 a 12.

**3. ¿Cuándo es válido descartar un mensaje de la DLT, y quién lo decide?**
Cuando se confirma que el mensaje no tiene arreglo y no representa un hecho real del negocio,
como el `esto no es json` que mandé a mano: no es un investigador, es basura. Lo debería
decidir una persona del equipo después de revisarlo, nunca una regla automática, y dejando
anotado por qué se descartó. Si el mensaje sí era un hecho real (un investigador que se
registró de verdad), descartarlo significa perder datos.

**4. ¿Qué pasa si el mensaje de la DLT es válido pero llegó a destiempo?**
Reprocesarlo tarde puede dejar el modelo de lectura en un estado incorrecto. Por ejemplo, si
un `investigadores.registrados` se reenvía después de que ese investigador ya se eliminó, la
ficha resucita, que es lo mismo que me pasó en el paso 8 del taller de CQRS con Luis. Antes de
reenviar habría que revisar si el hecho todavía aplica, o que los eventos lleven una versión
para que el consumidor pueda ignorar los que ya quedaron viejos.

**Conexión con la Lección 10 (observabilidad):** hoy la DLT guarda los mensajes, pero nadie
se entera de que llegaron. Para eso hacen falta tres cosas que se ven en observabilidad:
**métricas** (cuántos mensajes hay en la DLT y cuántos reintentos se hacen), **alertas**
sobre esas métricas y **trazas** que permitan seguir un evento desde que se publicó hasta que
falló. Sin eso, la DLT es solo un cajón donde los problemas se acumulan en silencio.

## Por qué reintentar sin fin es lo peor

Reintentar un mensaje venenoso sin fin es peor que perderlo, porque bloquea la partición y
ningún mensaje bueno que viene detrás se procesa: un solo mensaje roto detiene todo el
consumidor. Y es peor que mandarlo a una DLT, porque la DLT deja el sistema funcionando **y**
conserva el mensaje con el diagnóstico del error, mientras que reintentar sin fin no consigue
ninguna de las dos cosas.

---

# Taller Lección 10 — Observabilidad: seguir una Saga con un id de correlación

## Lo que observé

- **Salud:** los tres servicios responden `UP` en `/actuator/health`. Investigadores muestra
  el componente `db` (PostgreSQL) y publicaciones y consultas muestran `mongo`.
- **Un solo id para toda la Saga:** eliminé a Mateo por el Gateway con
  `X-Correlation-Id: demo-001` y buscando ese id encontré, en orden:
  `[investigadores-service,demo-001]` inicio de la eliminación →
  `[publicaciones-service,demo-001]` 2 publicaciones marcadas como huérfanas →
  `[investigadores-service,demo-001]` confirmación y borrado.
- **Métricas:** `http.server.requests` da el COUNT, TOTAL_TIME y MAX (tasa y duración), y con
  `?tag=status:404` se ven los errores por separado: son las tres señales del método RED.
- **Lag:** con consultas-service detenido registré 3 investigadores y el lag de
  `consultas-service-v2` subió a 3; al arrancarlo de nuevo volvió a 0.
- **Saga atascada:** con publicaciones-service apagado, Camila quedó en `EN_ELIMINACION` y el
  grupo `publicaciones-service` tenía lag 1. Al volver a arrancarlo la Saga terminó sola, y
  el id `demo-002` seguía ahí después de la pausa.

Un detalle que agregué: la guía espera una línea de "inicio de la eliminación" con el id en
investigadores-service, pero `InvestigadorService` no escribía nada en ese momento. Le puse un
`log.info` en `iniciarEliminacion`.

## La brecha en consultas-service

En los logs de consultas-service las líneas de la misma Saga salen como
`[consultas-service,]`, sin id. El evento sí trae el `correlationId` (lo copia el Outbox),
pero `ProyeccionListener` no lo lee. Se cerraría con el mismo patrón del paso 5: en cada
método del listener, leer `correlationId` del JSON y envolver la llamada al proyector en
`try (MDC.MDCCloseable c = MDC.putCloseable("correlationId", id)) { ... }`. Así las líneas
`Proyección: ...` también quedarían dentro de la traza.

## Paso 9: ¿y si nunca vuelve?

**1. Si publicaciones-service no pudiera marcar las publicaciones por un error permanente,
¿en qué estado quedaría el investigador y quién se daría cuenta?**
Quedaría para siempre en `EN_ELIMINACION`: no se borra, pero tampoco vuelve a estar activo.
Hoy nadie se daría cuenta solo, porque no hay ninguna alerta; lo notaría un usuario que vea
al investigador "trabado", o alguien que revise a mano el lag o el log de errores (si el
mensaje terminara en una DLT, quedaría ahí sin que nadie la mire).

**2. Diseña una compensación.**
Un temporizador en investigadores-service que revise cada cierto tiempo los investigadores en
`EN_ELIMINACION` desde hace más de, por ejemplo, 10 minutos. Para esos publicaría un evento
`investigadores.eliminacion-cancelada` (por el mismo Outbox) y devolvería al investigador a
`ACTIVO`. Lo consumirían publicaciones-service (para quitar la marca de huérfanas a sus
publicaciones) y consultas-service (para devolver la ficha a `ACTIVO`). Sería idempotente,
porque las dos acciones son "poner un estado", no "sumar": aplicar dos veces
"huerfana = false" o "estado = ACTIVO" deja el mismo resultado.

**3. ¿Qué métrica y alerta definirías para detectar Sagas atascadas?**
Una métrica con el número de investigadores en `EN_ELIMINACION` desde hace más de 10 minutos
(un gauge que se calcule con una consulta a la base), y una alerta cuando sea mayor que cero.
La complementaría con una alerta sobre el lag del grupo `publicaciones-service` en el tópico
`investigadores.en-eliminacion`: si el lag no baja en unos minutos, el consumidor está caído
o atascado, que es justo lo que vi en el paso 8.

**4. ¿Coreografía u orquestación si se le agregaran cuatro pasos más?**
Orquestación. Con dos servicios la coreografía es sencilla, pero con seis pasos el flujo
quedaría repartido entre muchos listeners y nadie sabría en qué paso va cada Saga; incluso
con el id de correlación tendría que juntar logs de varios servicios para entenderlo. Un
orquestador guarda el estado de cada Saga en un solo lugar, y eso hace mucho más fácil ver
dónde se atascó y lanzar las compensaciones en orden.

---

# Taller Lección 11 — Contenedores y CI

## Paso 2: el Dockerfile multi-etapa

**(a) ¿Por qué se copia primero `pom.xml` y luego `src`?**
Porque Docker guarda cada instrucción como una capa y la reutiliza mientras lo que entra en
ella no cambie. Las dependencias dependen solo del `pom.xml`, que casi nunca cambia; el código
cambia todo el tiempo. Copiando primero el `pom.xml` y descargando las dependencias, esa capa
pesada queda en caché, y cuando cambio una línea de código solo se recompila lo último. Lo
comprobé: la primera construcción tardó más de 5 minutos y, después de cambiar un archivo de
`src/`, tardó menos de 3 segundos.

**(b) ¿Qué contiene la imagen final y qué no?**
Contiene solo el JRE 25 y el `app.jar` ya compilado. No tiene Maven, ni el JDK, ni el código
fuente, ni las dependencias sueltas: todo eso se quedó en la etapa `build`, que se descarta.
Lo verifiqué entrando al contenedor: `java -version` responde, pero `mvn` no existe, y en
`/app` solo está `app.jar`. Así la imagen es más liviana y tiene menos cosas que puedan
tener vulnerabilidades.

**(c) ¿Por qué `USER rica`?**
Para que el proceso no corra como root dentro del contenedor. Si alguien lograra explotar
una falla de la aplicación, tendría solo los permisos de un usuario normal y no los de
administrador. Dentro del contenedor, `whoami` responde `rica`.

## Paso 3: qué capas se reutilizaron

- **Primera construcción:** se ejecutó todo (5 min 26 s): descarga de las imágenes base, de
  las dependencias y la compilación.
- **Sin cambios:** todas las capas salieron `CACHED` (1.8 s).
- **Cambiando un archivo de `src/`:** `COPY pom.xml` y `dependency:go-offline` siguieron en
  `CACHED`; solo se ejecutaron de nuevo `COPY src`, el `package` (1.4 s) y la copia del `.jar`
  a la imagen final (2.8 s en total).

La imagen pesa 607 MB; la capa con el `.jar` ocupa 60.7 MB.

Sobre el problema de replicación de Eureka: en el log del contenedor aparecía
`Adding new peer nodes [http://localhost:8761/eureka/]`, es decir, Eureka se veía a sí mismo
como otro nodo. En mi Mac no llegaron a salir los errores de `Read timed out` (el contenedor
se respondía a sí mismo rápido), pero la causa estaba ahí. Con `eureka.instance.hostname:
localhost` esa línea desapareció.

## Paso 5: una sola imagen, varias configuraciones

Con `SERVER_PORT=9000` la misma imagen arrancó en otro puerto sin reconstruirla. Esto es
mejor que tener una imagen por entorno porque lo que se prueba es exactamente lo mismo que
llega a producción: si construyera una imagen distinta para cada entorno, nunca estaría
seguro de que la que probé es la que se despliega. La configuración (puertos, direcciones de
bases de datos, contraseñas) cambia entre entornos; el código no debería.

## Paso 6: persistencia políglota — tabla de decisión de almacenes

Con Redis vi los cuatro casos de cache-aside: `GET` con el JSON (hit), `TTL` en 30, después de
30 segundos `GET` vacío y `TTL` en `-2` (miss: la clave ya no existe), y `DEL` para invalidar.

Para la tabla elegí el módulo que me toca en el proyecto de grupo (Cadena de Frío): **Alertas e
incidentes**, que hoy es `alertas-service`.

| Tipo de dato | Almacén | Por qué | Consulta principal que lo justifica | Costo de añadirlo |
|---|---|---|---|---|
| Alertas (envío, sensor, severidad, resuelta) | **PostgreSQL** (`alertas-db`) | Necesito transacciones: generar la alerta y marcar el evento como procesado tienen que pasar juntos o ninguno | `existsByEnvioIdAndSeveridadAndResueltaFalse`: "¿este envío tiene alertas críticas sin resolver?" (en mi prueba, `true` con la alerta CRITICA y `false` después del `PATCH /resolver`) | Ninguno extra: ya lo usamos, con Flyway |
| Eventos ya procesados (idempotencia) | **PostgreSQL**, misma base que las alertas | La clave primaria rechaza el duplicado y comparte transacción con la alerta; en Redis o Mongo perdería esa atomicidad | `existsById("sensores.lecturas-fuera-de-rango-0-1")`: al reentregar los eventos, el log dijo "ya procesado" y la base siguió con 2 alertas | Una tabla más (`eventos_procesados`), que crece con cada evento |
| Lecturas fuera de rango en tránsito | **Kafka** (`sensores.lecturas-fuera-de-rango`) | No es una base de consulta sino un log: guarda los eventos aunque Alertas esté caído y permite releerlos | Leer desde el último offset del grupo `alertas-service` | Operar un broker (`cadena-kafka`); hoy uno solo, sin réplicas |
| Mensajes que no se pudieron procesar | **Kafka DLT** (`...lecturas-fuera-de-rango.DLT`) | Conserva el mensaje original con cabeceras de diagnóstico | Leer la DLT con `print.headers=true`: en mi prueba, `StreamReadException` para `esto no es json` | Casi nada, pero nadie la vigila (necesita una alerta) |
| Histórico de lecturas para reportes | **TimescaleDB** (evaluada, no implementada) | Son series de tiempo de alto volumen: consultas por rango de fechas y promedios por hora | "Temperatura promedio por hora del envío X en las últimas 24 horas" | Otra base que aprender y operar; hoy las lecturas son simuladas, así que no lo justifico todavía |
| "¿Hay críticas sin resolver?" muy consultado | **Redis** como caché (descartado por ahora) | Ahorraría consultas a PostgreSQL | La misma consulta del cierre de envíos | Riesgo de leer un `false` viejo justo cuando acaba de llegar una crítica: invalidar mal rompe la integridad del cierre |

**¿Necesito más de una tecnología?** Para Alertas, hoy no: una base relacional bien usada basta.
Las alertas y los eventos procesados necesitan estar en la misma transacción, y eso solo lo da la
misma base; la consulta del cierre es por un índice (`idx_alertas_envio`) y responde rápido sin
caché. Kafka no lo cuento como "otra base", sino como el canal por donde llegan los eventos, y ya
lo necesitábamos para no perder lecturas. Redis lo descarté a propósito: con el cache-aside que
probé, un `GET` puede devolver un dato viejo hasta que pase el TTL o alguien haga `DEL`, y en el
cierre de un envío un `false` viejo significa cerrar un envío con la cadena de frío rota. La
única segunda tecnología que sí agregaría es TimescaleDB, y solo cuando lleguen lecturas reales.

## Lectura opcional: ¿qué haría falta para correr consultas-service como contenedor?

1. Un Dockerfile igual al de Eureka, pero con `EXPOSE 8083`.
2. Una red de Docker compartida con Kafka, MongoDB y Eureka, porque dentro del contenedor
   `localhost` es el propio contenedor y no mi máquina.
3. Cambiar por variables de entorno las direcciones: `SPRING_MONGODB_URI=mongodb://publicaciones-mongo:27017/rica_consultas`,
   `SPRING_KAFKA_BOOTSTRAP_SERVERS=rica-kafka:9092` y la `defaultZone` de Eureka.
4. Configurar en Kafka un segundo listener para los contenedores: hoy anuncia
   `localhost:9092`, que desde otro contenedor no sirve.
5. Ponerlo todo en un mismo `docker-compose.yml` con `depends_on`, para que arranque después
   de Kafka, Mongo y Eureka.
