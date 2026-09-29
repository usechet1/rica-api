# rica-api (archivado)

Este proyecto fue dividido en `investigadores-service` y `publicaciones-service`
a partir del Taller de la Lección 5. Ver `../rica-microservicios/`.

---

# rica-api

Registro de Investigadores y publicaciones de la UPTC. Nace del Tutorial 6 del
mini-curso de Spring Boot y luego se refactorizó siguiendo el taller de
Lección 3 (DDD: pasar de un modelo anémico a uno rico).

## Cómo levantarlo

```bash
docker-compose up -d          # PostgreSQL (investigadores) + MongoDB (publicaciones)
mvn spring-boot:run
```

## Tests

```bash
mvn test
```

## Endpoints

- `POST /api/investigadores` — { nombreCompleto, correoInstitucional, grupoInvestigacion }
- `POST /api/publicaciones` — { titulo, investigadorCorreo, anio }

## Cómo está organizado

```
src/main/java/com/rica/ricaapi/
  investigadores/   Investigador (raíz de Agregado), CorreoInstitucional (VO),
                     InvestigadorFactory, InvestigadorService, repositorio JPA,
                     controlador REST, evento de dominio InvestigadorRegistrado.
  publicaciones/     Publicacion (documento Mongo), LimitePublicacionesAnualesService
                     (Servicio de Dominio), repositorio, controlador REST.
```

En `NOTAS.md` están las respuestas del taller (lo del Lenguaje Ubicuo y el
límite del Agregado).
