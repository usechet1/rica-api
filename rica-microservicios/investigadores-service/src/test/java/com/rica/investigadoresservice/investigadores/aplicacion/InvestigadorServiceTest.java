package com.rica.investigadoresservice.investigadores.aplicacion;

import com.rica.investigadoresservice.investigadores.dominio.CorreoDuplicadoException;
import com.rica.investigadoresservice.investigadores.dominio.CorreoInstitucional;
import com.rica.investigadoresservice.investigadores.dominio.Investigador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InvestigadorServiceTest {

    @Mock
    private RepositorioInvestigadores repositorioInvestigadores;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PublicadorEventos publicadorEventos;

    private InvestigadorFactory investigadorFactory;
    private InvestigadorService investigadorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        investigadorFactory = new InvestigadorFactory(repositorioInvestigadores, eventPublisher);
        investigadorService = new InvestigadorService(repositorioInvestigadores, investigadorFactory, publicadorEventos);
    }

    @Test
    void registraUnInvestigadorNuevo() {
        when(repositorioInvestigadores.existeCorreo("ana.torres@uptc.edu.co")).thenReturn(false);
        when(repositorioInvestigadores.guardar(any(Investigador.class))).thenAnswer(inv -> {
            Investigador i = inv.getArgument(0);
            return new Investigador(1L, i.getNombreCompleto(), i.getCorreoInstitucional(), i.getGrupoInvestigacion());
        });

        Investigador resultado = investigadorService.registrar("Ana Torres", "ana.torres@uptc.edu.co", "GIT-UPTC");

        assertEquals("Ana Torres", resultado.getNombreCompleto());
        assertEquals("ana.torres@uptc.edu.co", resultado.getCorreoInstitucional().valor());
    }

    @Test
    void registrarRechazaCorreoInstitucionalDuplicado() {
        when(repositorioInvestigadores.existeCorreo("ana.torres@uptc.edu.co")).thenReturn(true);

        assertThrows(CorreoDuplicadoException.class, () ->
            investigadorService.registrar("Ana Torres", "ana.torres@uptc.edu.co", "GIT-UPTC"));
    }

    @Test
    void correoInstitucionalRechazaDominioNoUptc() {
        assertThrows(IllegalArgumentException.class, () -> new CorreoInstitucional("carlos@gmail.com"));
    }

    @Test
    void confirmarEliminacionBorraAlInvestigadorYPublicaElEventoEliminado() {
        Investigador enEliminacion = new Investigador(1L, "Ana Torres", new CorreoInstitucional("ana.torres@uptc.edu.co"), "GIT-UPTC");
        enEliminacion.marcarEnEliminacion();
        when(repositorioInvestigadores.buscarPorCorreo("ana.torres@uptc.edu.co")).thenReturn(Optional.of(enEliminacion));

        investigadorService.confirmarEliminacion("ana.torres@uptc.edu.co");

        verify(repositorioInvestigadores).eliminar(enEliminacion);
        verify(publicadorEventos).publicar(
                "investigadores.eliminados",
                "ana.torres@uptc.edu.co",
                Map.of("correoInstitucional", "ana.torres@uptc.edu.co"));
    }
}
