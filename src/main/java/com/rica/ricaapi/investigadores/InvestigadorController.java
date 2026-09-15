package com.rica.ricaapi.investigadores;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/investigadores")
public class InvestigadorController {

    private final InvestigadorService investigadorService;
    private final InvestigadorMapper investigadorMapper;

    public InvestigadorController(InvestigadorService investigadorService, InvestigadorMapper investigadorMapper) {
        this.investigadorService = investigadorService;
        this.investigadorMapper = investigadorMapper;
    }

    @PostMapping
    public ResponseEntity<InvestigadorResponse> registrar(@Valid @RequestBody InvestigadorRequest request) {
        // La Factory es el único punto de construcción: el controlador solo
        // pasa los tres campos sueltos, nunca ensambla un Investigador él mismo.
        Investigador investigador = investigadorService.registrar(
            request.getNombreCompleto(),
            request.getCorreoInstitucional(),
            request.getGrupoInvestigacion()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(investigadorMapper.aResponse(investigador));
    }
}
