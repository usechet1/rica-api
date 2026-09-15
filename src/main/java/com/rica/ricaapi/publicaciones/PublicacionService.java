package com.rica.ricaapi.publicaciones;

import com.rica.ricaapi.investigadores.Investigador;
import com.rica.ricaapi.investigadores.InvestigadorRepository;
import org.springframework.stereotype.Service;

@Service
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final InvestigadorRepository investigadorRepository;
    private final LimitePublicacionesAnualesService limitePublicacionesAnualesService;

    public PublicacionService(PublicacionRepository publicacionRepository,
                               InvestigadorRepository investigadorRepository,
                               LimitePublicacionesAnualesService limitePublicacionesAnualesService) {
        this.publicacionRepository = publicacionRepository;
        this.investigadorRepository = investigadorRepository;
        this.limitePublicacionesAnualesService = limitePublicacionesAnualesService;
    }

    public Publicacion registrar(String titulo, String investigadorCorreo, Integer anio) {
        Investigador investigador = investigadorRepository
            .findAll()
            .stream()
            .filter(i -> i.getCorreoInstitucional().valor().equals(investigadorCorreo))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "No existe un investigador registrado con el correo " + investigadorCorreo));

        Publicacion nueva = new Publicacion(null, titulo, investigadorCorreo, anio);

        if (!limitePublicacionesAnualesService.puedeRegistrar(investigador, nueva)) {
            throw new LimiteAnualExcedidoException(
                "El investigador " + investigadorCorreo
                    + " ya alcanzó el máximo de publicaciones para el año " + anio);
        }

        return publicacionRepository.save(nueva);
    }
}
