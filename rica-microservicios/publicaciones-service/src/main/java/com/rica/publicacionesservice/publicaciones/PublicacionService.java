package com.rica.publicacionesservice.publicaciones;

import com.rica.publicacionesservice.comun.RecursoNoEncontradoException;
import org.springframework.stereotype.Service;

/**
 * NOTA (taller descomposición): publicaciones-service ya no conoce ninguna
 * clase de investigadores. La verificación del investigador pasa por el
 * puerto VerificadorInvestigador; hoy su único adaptador es
 * VerificadorInvestigadorPendiente, que acepta cualquier correo hasta que el
 * Taller de la Lección 6 lo reemplace por una llamada real por red.
 */
@Service
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final VerificadorInvestigador verificadorInvestigador;
    private final LimitePublicacionesAnualesService limitePublicacionesAnualesService;

    public PublicacionService(PublicacionRepository publicacionRepository,
                               VerificadorInvestigador verificadorInvestigador,
                               LimitePublicacionesAnualesService limitePublicacionesAnualesService) {
        this.publicacionRepository = publicacionRepository;
        this.verificadorInvestigador = verificadorInvestigador;
        this.limitePublicacionesAnualesService = limitePublicacionesAnualesService;
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

        return publicacionRepository.save(nueva);
    }
}
