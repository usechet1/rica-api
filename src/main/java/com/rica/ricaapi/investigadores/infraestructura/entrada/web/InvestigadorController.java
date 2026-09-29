package com.rica.ricaapi.investigadores.infraestructura.entrada.web;

import com.rica.ricaapi.investigadores.aplicacion.InvestigadorUseCase;
import com.rica.ricaapi.investigadores.dominio.Investigador;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adaptador primario: traduce HTTP/JSON hacia el puerto primario InvestigadorUseCase.
 * Depende de la interfaz, nunca de InvestigadorService directamente.
 */
@RestController
@RequestMapping("/api/investigadores")
public class InvestigadorController {

    private final InvestigadorUseCase investigadorService;
    private final InvestigadorMapper investigadorMapper;

    public InvestigadorController(InvestigadorUseCase investigadorService, InvestigadorMapper investigadorMapper) {
        this.investigadorService = investigadorService;
        this.investigadorMapper = investigadorMapper;
    }

    @PostMapping
    public ResponseEntity<InvestigadorResponse> registrar(@Valid @RequestBody InvestigadorRequest request) {
        Investigador investigador = investigadorService.registrar(
            request.getNombreCompleto(),
            request.getCorreoInstitucional(),
            request.getGrupoInvestigacion()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(investigadorMapper.aResponse(investigador));
    }

    @GetMapping
    public List<InvestigadorResponse> listarTodos() {
        return investigadorService.listarTodos().stream()
            .map(investigadorMapper::aResponse)
            .toList();
    }

    @GetMapping("/{id}")
    public InvestigadorResponse buscarPorId(@PathVariable Long id) {
        return investigadorMapper.aResponse(investigadorService.buscarPorId(id));
    }
}
