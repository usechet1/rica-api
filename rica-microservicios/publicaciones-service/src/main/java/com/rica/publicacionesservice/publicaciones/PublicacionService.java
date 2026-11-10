package com.rica.publicacionesservice.publicaciones;

import com.rica.publicacionesservice.comun.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

/**
 * publicaciones-service no conoce ninguna clase de investigadores. La
 * verificación del investigador pasa por el puerto VerificadorInvestigador,
 * cuyo adaptador (VerificadorInvestigadorGrpc) pregunta a
 * investigadores-service por gRPC. Al cambiar de adaptador en el Taller de la
 * Lección 6, esta clase no cambió.
 */
@Service
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final VerificadorInvestigador verificadorInvestigador;
    private final LimitePublicacionesAnualesService limitePublicacionesAnualesService;
    private final PublicadorEventosPublicaciones publicadorEventos;

    public PublicacionService(PublicacionRepository publicacionRepository,
                               VerificadorInvestigador verificadorInvestigador,
                               LimitePublicacionesAnualesService limitePublicacionesAnualesService,
                               PublicadorEventosPublicaciones publicadorEventos) {
        this.publicacionRepository = publicacionRepository;
        this.verificadorInvestigador = verificadorInvestigador;
        this.limitePublicacionesAnualesService = limitePublicacionesAnualesService;
        this.publicadorEventos = publicadorEventos;
    }

    public Publicacion registrar(String titulo, String investigadorCorreo, Integer anio) {
        if (!verificadorInvestigador.existe(investigadorCorreo)) {
            throw new RecursoNoEncontradoException(
                "No existe un investigador con correo " + investigadorCorreo);
        }

        Publicacion nueva = new Publicacion(null, titulo, investigadorCorreo, anio);

        if (!limitePublicacionesAnualesService.puedeRegistrar(nueva)) {
            throw new LimiteAnualExcedidoException(
                "El investigador " + investigadorCorreo
                    + " ya alcanzó el máximo de publicaciones para el año " + anio);
        }

        // Solo se anuncia el hecho si el comando terminó bien (CQRS, Lección 8).
        Publicacion guardada = publicacionRepository.save(nueva);
        publicadorEventos.publicacionRegistrada(guardada);
        return guardada;
    }
}
