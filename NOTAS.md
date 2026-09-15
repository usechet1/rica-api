# NOTAS.md — Taller DDD en rica-api

## 1. Lenguaje Ubicuo: auditoría rápida

**¿Hay algún campo o método con nombre genérico (data, info, value, item) que debería
tener un nombre del dominio?**
No encontré ninguno. Revisando `InvestigadorController`, `InvestigadorRequest` e
`InvestigadorResponse`, todos los campos (`nombreCompleto`, `correoInstitucional`,
`grupoInvestigacion`) ya usan vocabulario del dominio académico y no jerga técnica
genérica. Lo único que podría verse "técnico" es `id`, pero es un identificador
estándar, no un concepto de negocio que necesite nombre propio.

**¿`correoInstitucional` y `grupoInvestigacion` son términos que reconocería alguien de
la Facultad sin que se los tradujeran?**
Sí, sin problema. Un coordinador de investigación usa exactamente esas palabras en el
día a día: el "correo institucional" es el correo @uptc.edu.co con el que se identifica
a cada investigador, y "grupo de investigación" es la unidad organizativa que reconoce
la Facultad (por ejemplo, GIT-UPTC). Por eso no los renombré: el Lenguaje Ubicuo no
siempre implica cambiar nombres, a veces implica confirmar que ya estaban bien y dejar
constancia de por qué.

**¿Por qué `investigadorCorreo` es más preciso que `investigadorId` o `autorId`?**
Porque deja explícito, en el propio nombre del campo, que la referencia entre Agregados
se hace por el correo institucional —el identificador natural y estable del
investigador en este dominio— y no por un id técnico de base de datos ni por el rol
genérico de "autor". `autorId` escondería dos cosas a la vez: qué tipo de identificador
se usa y el hecho de que, en RICA, "autor" y "correo institucional" son el mismo
concepto. Con `investigadorCorreo`, cualquiera que lea el código entiende de inmediato
qué se está referenciando y cómo, sin tener que ir a revisar la implementación.

## 4. Límite del Agregado Investigador

| Pregunta | Respuesta |
|---|---|
| ¿Cuál es la raíz del Agregado Investigador? | `Investigador`: es la única clase con `@Entity` en el paquete `investigadores`. |
| ¿Qué vive dentro del límite? | `id`, `nombreCompleto`, `correoInstitucional` (ahora `CorreoInstitucional`, un Value Object) y `grupoInvestigacion`. |
| ¿Por qué `Publicacion` NO está dentro de este límite? | Porque un investigador puede acumular muchas publicaciones a lo largo de los años, e incluirlas dentro del Agregado violaría la regla de Vernon de "Agregados pequeños": dispararía cargas y transacciones enormes por cada cambio a un investigador. Actualizar `grupoInvestigacion`, por ejemplo, no debería traer ni bloquear todas sus publicaciones. |
| ¿Qué pasaría si alguien agrega `List<Publicacion> publicaciones` directo en `Investigador`? | Se rompería el límite en varios sentidos: dos transacciones concurrentes sobre el mismo `Investigador` podrían pisarse la lista de publicaciones de otra persona, el Agregado dejaría de ser pequeño, y `publicaciones` (que hoy vive en MongoDB, separado de `investigadores` en PostgreSQL) perdería la posibilidad de evolucionar o desplegarse de forma independiente. |

## Resumen de lo que apliqué en el código

1. **Lenguaje Ubicuo** — auditado arriba; no hizo falta cambiar nombres, ya estaban bien.
2. **Value Object** — `investigadores/CorreoInstitucional.java`: hace imposible construir
   un correo institucional inválido. `Investigador` lo usa vía `@Embedded`.
3. **Servicio de Dominio** — `publicaciones/LimitePublicacionesAnualesService.java`:
   aplica la regla "máximo 5 publicaciones por año" apoyándose en
   `PublicacionRepository.countByInvestigadorCorreoAndAnio`. Lo dejé en `publicaciones`,
   no en `investigadores`, para no invertir la dirección de dependencia que ya existía.
4. **Límite del Agregado** — documentado arriba; no hubo cambios de código porque ya se
   respetaba.
5. **Factory** — `investigadores/InvestigadorFactory.java`: es el único punto de
   construcción de `Investigador`; valida el correo y rechaza duplicados de forma
   atómica. `InvestigadorService.registrar(...)` delega en ella.
6. **Domain Event (reto extra)** — `InvestigadorRegistrado` + `InvestigadorRegistradoListener`:
   se publica al crear un investigador. Por ahora el "otro lado" es solo un
   `log.info(...)`, pero ya queda el enganche listo para lo que venga después.

El comportamiento externo no cambió: `POST /api/investigadores` y
`POST /api/publicaciones` responden igual que antes, solo que ahora el dominio por
dentro está mejor diseñado.
