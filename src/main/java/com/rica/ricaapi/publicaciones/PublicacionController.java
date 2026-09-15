package com.rica.ricaapi.publicaciones;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/publicaciones")
public class PublicacionController {

    private final PublicacionService publicacionService;
    private final PublicacionMapper publicacionMapper;

    public PublicacionController(PublicacionService publicacionService, PublicacionMapper publicacionMapper) {
        this.publicacionService = publicacionService;
        this.publicacionMapper = publicacionMapper;
    }

    @PostMapping
    public ResponseEntity<PublicacionResponse> registrar(@Valid @RequestBody PublicacionRequest request) {
        Publicacion publicacion = publicacionService.registrar(
            request.getTitulo(),
            request.getInvestigadorCorreo(),
            request.getAnio()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(publicacionMapper.aResponse(publicacion));
    }
}
