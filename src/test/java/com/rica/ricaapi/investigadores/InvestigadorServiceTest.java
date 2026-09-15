package com.rica.ricaapi.investigadores;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class InvestigadorServiceTest {

    @Mock
    private InvestigadorRepository investigadorRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private InvestigadorFactory investigadorFactory;
    private InvestigadorService investigadorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        investigadorFactory = new InvestigadorFactory(investigadorRepository, eventPublisher);
        investigadorService = new InvestigadorService(investigadorFactory, investigadorRepository);
    }

    @Test
    void registraUnInvestigadorNuevo() {
        when(investigadorRepository.existsByCorreoInstitucional_Valor("ana.torres@uptc.edu.co")).thenReturn(false);
        when(investigadorRepository.save(any(Investigador.class))).thenAnswer(inv -> {
            Investigador i = inv.getArgument(0);
            return new Investigador(1L, i.getNombreCompleto(), i.getCorreoInstitucional(), i.getGrupoInvestigacion());
        });

        Investigador resultado = investigadorService.registrar("Ana Torres", "ana.torres@uptc.edu.co", "GIT-UPTC");

        assertEquals("Ana Torres", resultado.getNombreCompleto());
        assertEquals("ana.torres@uptc.edu.co", resultado.getCorreoInstitucional().valor());
    }

    @Test
    void registrarRechazaCorreoInstitucionalDuplicado() {
        when(investigadorRepository.existsByCorreoInstitucional_Valor("ana.torres@uptc.edu.co")).thenReturn(true);

        assertThrows(CorreoDuplicadoException.class, () ->
            investigadorService.registrar("Ana Torres", "ana.torres@uptc.edu.co", "GIT-UPTC"));
    }

    @Test
    void correoInstitucionalRechazaDominioNoUptc() {
        assertThrows(IllegalArgumentException.class, () -> new CorreoInstitucional("carlos@gmail.com"));
    }
}
