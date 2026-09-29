package com.rica.ricaapi.investigadores.aplicacion;

import com.rica.ricaapi.investigadores.dominio.Investigador;

import java.util.List;

/** Puerto primario: lo que el mundo exterior puede pedirle al núcleo de investigadores. */
public interface InvestigadorUseCase {

    List<Investigador> listarTodos();

    Investigador buscarPorId(Long id);

    Investigador registrar(String nombreCompleto, String correoInstitucional, String grupoInvestigacion);
}
