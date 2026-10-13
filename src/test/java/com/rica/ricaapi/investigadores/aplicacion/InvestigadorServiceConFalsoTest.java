package com.rica.ricaapi.investigadores.aplicacion;

import com.rica.ricaapi.investigadores.dominio.Investigador;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sin @SpringBootTest, sin @Mock, sin @DataJpaTest, sin base de datos.
 *
 * NOTA sobre este taller: InvestigadorFactory aquí necesita también un
 * ApplicationEventPublisher (evento de dominio del taller anterior). Se le
 * pasa un publisher "no-op" (evento -> {}) para no reintroducir Spring en
 * este test — sigue sin usarse ningún framework, solo una interfaz funcional
 * de la librería estándar de Spring que no arranca ningún contenedor.
 */
class InvestigadorServiceConFalsoTest {

    @Test
    void registraYRecuperaUnInvestigadorSinNingunaDependenciaDeSpringNiDeBaseDeDatos() {
        RepositorioInvestigadoresFalso repositorio = new RepositorioInvestigadoresFalso();
        InvestigadorFactory factory = new InvestigadorFactory(repositorio, evento -> { });
        InvestigadorService service = new InvestigadorService(repositorio, factory);

        Investigador guardado = service.registrar("Ana Torres", "ana.torres@uptc.edu.co", "GIT-UPTC");

        assertThat(guardado.getId()).isNotNull();
        assertThat(service.buscarPorId(guardado.getId()).getNombreCompleto()).isEqualTo("Ana Torres");
    }
}
