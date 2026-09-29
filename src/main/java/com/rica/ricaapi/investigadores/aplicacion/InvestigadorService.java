package com.rica.ricaapi.investigadores.aplicacion;

import com.rica.ricaapi.investigadores.dominio.Investigador;
import com.rica.ricaapi.investigadores.dominio.InvestigadorNoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvestigadorService implements InvestigadorUseCase {

    private final RepositorioInvestigadores repositorioInvestigadores;
    private final InvestigadorFactory investigadorFactory;

    public InvestigadorService(RepositorioInvestigadores repositorioInvestigadores,
                                InvestigadorFactory investigadorFactory) {
        this.repositorioInvestigadores = repositorioInvestigadores;
        this.investigadorFactory = investigadorFactory;
    }

    @Override
    public List<Investigador> listarTodos() {
        return repositorioInvestigadores.listarTodos();
    }

    @Override
    public Investigador buscarPorId(Long id) {
        return repositorioInvestigadores.buscarPorId(id)
            .orElseThrow(() -> new InvestigadorNoEncontradoException(
                "No existe un investigador con id " + id));
    }

    @Override
    public Investigador registrar(String nombreCompleto, String correoInstitucional, String grupoInvestigacion) {
        Investigador investigador = investigadorFactory.crear(nombreCompleto, correoInstitucional, grupoInvestigacion);
        return repositorioInvestigadores.guardar(investigador);
    }
}
