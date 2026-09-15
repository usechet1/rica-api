package com.rica.ricaapi.investigadores;

import org.springframework.stereotype.Service;

@Service
public class InvestigadorService {

    private final InvestigadorFactory investigadorFactory;
    private final InvestigadorRepository investigadorRepository;

    public InvestigadorService(InvestigadorFactory investigadorFactory,
                                InvestigadorRepository investigadorRepository) {
        this.investigadorFactory = investigadorFactory;
        this.investigadorRepository = investigadorRepository;
    }

    public Investigador registrar(String nombreCompleto, String correoInstitucional, String grupoInvestigacion) {
        Investigador investigador = investigadorFactory.crear(nombreCompleto, correoInstitucional, grupoInvestigacion);
        return investigadorRepository.save(investigador);
    }
}
