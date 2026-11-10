package com.rica.publicacionesservice.publicaciones;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicacionServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private VerificadorInvestigador verificadorInvestigador;

    @Mock
    private LimitePublicacionesAnualesService limitePublicacionesAnualesService;

    @Mock
    private PublicadorEventosPublicaciones publicadorEventos;

    @InjectMocks
    private PublicacionService publicacionService;

    @Test
    void registrarPublicaElEventoDespuesDeGuardar() {
        Publicacion guardada = new Publicacion("abc123", "Arquitecturas evolutivas", "ana.torres@uptc.edu.co", 2026);
        when(verificadorInvestigador.existe("ana.torres@uptc.edu.co")).thenReturn(true);
        when(limitePublicacionesAnualesService.puedeRegistrar(any(Publicacion.class))).thenReturn(true);
        when(publicacionRepository.save(any(Publicacion.class))).thenReturn(guardada);

        Publicacion resultado = publicacionService.registrar("Arquitecturas evolutivas", "ana.torres@uptc.edu.co", 2026);

        assertThat(resultado.getId()).isEqualTo("abc123");
        verify(publicadorEventos).publicacionRegistrada(guardada);
    }

    @Test
    void registrarNoPublicaEventoSiElInvestigadorNoExiste() {
        when(verificadorInvestigador.existe("nadie@uptc.edu.co")).thenReturn(false);

        // el comando falla: no hay hecho que anunciar
        assertThatThrownBy(() -> publicacionService.registrar("X", "nadie@uptc.edu.co", 2026))
                .isInstanceOf(RuntimeException.class);

        verify(publicadorEventos, never()).publicacionRegistrada(any());
    }
}
