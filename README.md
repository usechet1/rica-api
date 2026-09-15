# rica-api

Registro de Investigadores y publicaciones — UPTC. Proyecto base (fin del
Tutorial 6 del mini-curso Spring Boot) ya refactorizado según el taller de
Lección 3 (DDD: modelo anémico → modelo rico).

## Arrancar

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

## Estructura

```
src/main/java/com/rica/ricaapi/
  investigadores/   Investigador (raíz de Agregado), CorreoInstitucional (VO),
                     InvestigadorFactory, InvestigadorService, repositorio JPA,
                     controlador REST, evento de dominio InvestigadorRegistrado.
  publicaciones/     Publicacion (documento Mongo), LimitePublicacionesAnualesService
                     (Servicio de Dominio), repositorio, controlador REST.
```

Ver `NOTAS.md` para las respuestas escritas del taller (Lenguaje Ubicuo y
límite del Agregado).
