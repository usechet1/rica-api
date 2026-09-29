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
