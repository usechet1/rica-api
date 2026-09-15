package com.rica.ricaapi.publicaciones;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PublicacionRepository extends MongoRepository<Publicacion, String> {

    long countByInvestigadorCorreoAndAnio(String investigadorCorreo, Integer anio);
}
