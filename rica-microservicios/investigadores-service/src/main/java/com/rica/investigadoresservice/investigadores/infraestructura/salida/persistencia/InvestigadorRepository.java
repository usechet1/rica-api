package com.rica.investigadoresservice.investigadores.infraestructura.salida.persistencia;

import com.rica.investigadoresservice.investigadores.dominio.Investigador;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Detalle técnico: interfaz que Spring Data JPA implementa por reflexión.
 * Nadie fuera de InvestigadorRepositoryJpaAdapter conoce esta interfaz —
 * el núcleo solo conoce RepositorioInvestigadores.
 */
public interface InvestigadorRepository extends JpaRepository<Investigador, Long> {

    boolean existsByCorreoInstitucional_Valor(String valor);
}
