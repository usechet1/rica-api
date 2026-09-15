# NOTAS.md — Taller DDD en rica-api

## 1. Lenguaje Ubicuo: auditoría rápida

**¿Hay algún campo o método con nombre genérico (data, info, value, item) que debería
tener un nombre del dominio?**
No. Revisando `InvestigadorController`, `InvestigadorRequest` e `InvestigadorResponse`,
todos los campos (`nombreCompleto`, `correoInstitucional`, `grupoInvestigacion`) ya usan
vocabulario del dominio académico, no jerga técnica genérica. El único término que
podría verse "técnico" es `id`, pero es un identificador estándar y no representa un
concepto de negocio que necesite nombre propio.

**¿`correoInstitucional` y `grupoInvestigacion` son términos que reconocería alguien de
la Facultad sin que se los tradujeran?**
Sí. Un coordinador de investigación usa exactamente esas palabras en el día a día: el
"correo institucional" es el correo @uptc.edu.co con el que se identifica a cada
investigador, y "grupo de investigación" es la unidad organizativa reconocida por la
Facultad (p. ej. GIT-UPTC). Por eso no se renombran: el Lenguaje Ubicuo no siempre
implica cambiar nombres, a veces implica confirmar que ya están bien y dejar constancia
de por qué.

**¿Por qué `investigadorCorreo` es más preciso que `investigadorId` o `autorId`?**
`investigadorCorreo` deja explícito, en el propio nombre del campo, que la referencia
entre Agregados se hace por el correo institucional (el identificador natural y estable
del investigador en este dominio), no por un id técnico de base de datos ni por el rol
genérico de "autor". Un nombre como `autorId` escondería tanto el tipo de identificador
usado como el hecho de que "autor" y "correo institucional" son, en RICA, el mismo
concepto: cualquiera que lea el código entiende de inmediato qué se está referenciando y
cómo, sin tener que ir a revisar la implementación.

## 4. Límite del Agregado Investigador

| Pregunta | Respuesta |
|---|---|
| ¿Cuál es la raíz del Agregado Investigador? | `Investigador`: es la única clase con `@Entity` en el paquete `investigadores`. |
| ¿Qué vive dentro del límite? | `id`, `nombreCompleto`, `correoInstitucional` (ahora `CorreoInstitucional`, un Value Object), `grupoInvestigacion`. |
| ¿Por qué `Publicacion` NO está dentro de este límite? | Un investigador puede tener muchas publicaciones a lo largo de los años; incluirlas dentro del Agregado violaría la regla de Vernon de "Agregados pequeños" y dispararía cargas y transacciones enormes por cada cambio a un investigador (por ejemplo, actualizar `grupoInvestigacion` no debería traer ni bloquear todas sus publicaciones). |
| ¿Qué pasaría si alguien agrega `List<Publicacion> publicaciones` directo en `Investigador`? | Rompería el límite: dos transacciones concurrentes sobre el mismo `Investigador` podrían pisarse la lista de publicaciones de otra persona; el Agregado dejaría de ser pequeño; y `publicaciones` (que hoy vive en MongoDB, separado de `investigadores` en PostgreSQL) dejaría de poder evolucionar o desplegarse de forma independiente. |

## Resumen de lo aplicado en el código

1. **Lenguaje Ubicuo** — auditado arriba, sin cambios de nombres (ya estaban bien).
2. **Value Object** — `investigadores/CorreoInstitucional.java`: hace imposible construir
   un correo institucional inválido. `Investigador` lo usa vía `@Embedded`.
3. **Servicio de Dominio** — `publicaciones/LimitePublicacionesAnualesService.java`:
   aplica la regla "máximo 5 publicaciones por año" a partir de
   `PublicacionRepository.countByInvestigadorCorreoAndAnio`. Vive en `publicaciones`,
   no en `investigadores`, para no invertir la dirección de dependencia existente.
4. **Límite del Agregado** — documentado arriba, sin cambios de código (ya se respetaba).
5. **Factory** — `investigadores/InvestigadorFactory.java`: único punto de construcción
   de `Investigador`; valida el correo y rechaza duplicados de forma atómica.
   `InvestigadorService.registrar(...)` delega en ella.
6. **Domain Event (reto extra)** — `InvestigadorRegistrado` + `InvestigadorRegistradoListener`:
   se publica al crear un investigador; hoy el "otro lado" es solo un `log.info(...)`.

Comportamiento externo (endpoints, respuestas) sin cambios: `POST /api/investigadores`
y `POST /api/publicaciones` funcionan igual que antes, pero con un dominio mejor
diseñado por dentro.
