package com.rica.investigadoresservice.investigadores.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Reglas de la entidad Investigador (raíz del Agregado). */
class InvestigadorTest {

    private static Investigador ana(Long id) {
        return new Investigador(id, "Ana Torres", new CorreoInstitucional("ana.torres@uptc.edu.co"), "GIT-UPTC");
    }

    @Test
    void unInvestigadorNuevoEstaActivo() {
        Investigador investigador = ana(1L);

        assertThat(investigador.getEstado()).isEqualTo(EstadoInvestigador.ACTIVO);
        assertThat(investigador.estaEnEliminacion()).isFalse();
    }

    @Test
    void marcarEnEliminacionCambiaElEstadoParaLaSaga() {
        Investigador investigador = ana(1L);

        investigador.marcarEnEliminacion();

        assertThat(investigador.getEstado()).isEqualTo(EstadoInvestigador.EN_ELIMINACION);
        assertThat(investigador.estaEnEliminacion()).isTrue();
    }

    @Test
    void dosInvestigadoresConElMismoIdSonLaMismaEntidad() {
        // Entidad: la identidad es el id, aunque cambien los demás datos
        Investigador original = ana(7L);
        Investigador modificado = ana(7L);
        modificado.setGrupoInvestigacion("OTRO-GRUPO");

        assertThat(original).isEqualTo(modificado);
    }

    @Test
    void unInvestigadorSinIdNoEsIgualAOtro() {
        assertThat(ana(null)).isNotEqualTo(ana(null));
    }
}
