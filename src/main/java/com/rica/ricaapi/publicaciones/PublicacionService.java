package com.rica.ricaapi.publicaciones;

import com.rica.ricaapi.investigadores.aplicacion.RepositorioInvestigadores;
import com.rica.ricaapi.investigadores.dominio.Investigador;
import org.springframework.stereotype.Service;

/**
 * NOTA (taller hexagonal): esta clase depende ahora del puerto
 * RepositorioInvestigadores (paquete investigadores.aplicacion) en vez del
 * antiguo InvestigadorRepository, que quedó como detalle técnico dentro de
 * investigadores.infraestructura.salida.persistencia. publicaciones ya no
 * conoce ningún tipo de JPA de investigadores. Darle a publicaciones sus
 * propios puertos (InvestigadorUseCase como puerto secundario, adaptador
 * simulado, etc.) es exactamente el reto opcional 7-B de este taller —
 * no se hizo aquí para mantener el alcance en el paquete investigadores.
 */
@Service
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final RepositorioInvestigadores repositorioInvestigadores;
    private final LimitePublicacionesAnualesService limitePublicacionesAnualesService;

    public PublicacionService(PublicacionRepository publicacionRepository,
                               RepositorioInvestigadores repositorioInvestigadores,
                               LimitePublicacionesAnualesService limitePublicacionesAnualesService) {
        this.publicacionRepository = publicacionRepository;
        this.repositorioInvestigadores = repositorioInvestigadores;
        this.limitePublicacionesAnualesService = limitePublicacionesAnualesService;
    }

    public Publicacion registrar(String titulo, String investigadorCorreo, Integer anio) {
        Investigador investigador = repositorioInvestigadores
            .listarTodos()
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
