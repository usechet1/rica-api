package com.rica.investigadoresservice.investigadores.dominio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** El Value Object hace imposible construir un correo institucional inválido. */
class CorreoInstitucionalTest {

    @Test
    void aceptaUnCorreoDelDominioUptc() {
        CorreoInstitucional correo = new CorreoInstitucional("ana.torres@uptc.edu.co");

        assertThat(correo.valor()).isEqualTo("ana.torres@uptc.edu.co");
    }

    @Test
    void rechazaUnCorreoDeOtroDominio() {
        assertThatThrownBy(() -> new CorreoInstitucional("carlos@gmail.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("@uptc.edu.co");
    }

    @Test
    void rechazaUnCorreoNulo() {
        assertThatThrownBy(() -> new CorreoInstitucional(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dosCorreosConElMismoValorSonIguales() {
        // Value Object: se compara por su valor, no por identidad
        assertThat(new CorreoInstitucional("ana.torres@uptc.edu.co"))
                .isEqualTo(new CorreoInstitucional("ana.torres@uptc.edu.co"));
    }
}
