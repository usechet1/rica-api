package com.rica.publicacionesservice.publicaciones;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PublicacionRepository extends MongoRepository<Publicacion, String> {

    long countByInvestigadorCorreoAndAnio(String investigadorCorreo, Integer anio);

    List<Publicacion> findByInvestigadorCorreo(String investigadorCorreo);
}
