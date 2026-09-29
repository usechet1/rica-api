package com.rica.ricaapi.investigadores.aplicacion;

import com.rica.ricaapi.investigadores.dominio.Investigador;

import java.util.List;
import java.util.Optional;

/**
 * Puerto secundario mínimo: solo los cuatro métodos que el núcleo realmente
 * necesita — no los ~30 que JpaRepository ofrece de fábrica.
 */
public interface RepositorioInvestigadores {

    List<Investigador> listarTodos();

    Optional<Investigador> buscarPorId(Long id);

    boolean existeCorreo(String correoInstitucional);

    Investigador guardar(Investigador investigador);
}
