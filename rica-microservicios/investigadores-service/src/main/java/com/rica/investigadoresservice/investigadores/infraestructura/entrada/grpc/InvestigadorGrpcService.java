package com.rica.investigadoresservice.investigadores.infraestructura.entrada.grpc;

import com.rica.investigadoresservice.grpc.InvestigadorGrpcGrpc;
import com.rica.investigadoresservice.grpc.InvestigadorRequest;
import com.rica.investigadoresservice.grpc.InvestigadorResponse;
import com.rica.investigadoresservice.investigadores.aplicacion.RepositorioInvestigadores;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

/**
 * Adaptador primario gRPC: igual que InvestigadorController, traduce una
 * tecnología de entrada (aquí gRPC en vez de HTTP/JSON) hacia el núcleo.
 */
@Service
public class InvestigadorGrpcService extends InvestigadorGrpcGrpc.InvestigadorGrpcImplBase {

    private final RepositorioInvestigadores repositorio;

    public InvestigadorGrpcService(RepositorioInvestigadores repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void existeInvestigador(InvestigadorRequest request,
                                   StreamObserver<InvestigadorResponse> responseObserver) {
        boolean existe = repositorio.existeCorreo(request.getCorreoInstitucional());
        responseObserver.onNext(InvestigadorResponse.newBuilder().setExiste(existe).build());
        responseObserver.onCompleted();
    }
}
