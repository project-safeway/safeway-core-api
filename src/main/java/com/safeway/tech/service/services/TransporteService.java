package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.transporte.TransporteRequest;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.infra.exception.TransporteNotFoundException;
import com.safeway.tech.repository.TransporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransporteService {

    private final TransporteRepository transporteRepository;
    private final UsuarioService usuarioService;
    private final CurrentUserService currentUserService;

    public Transport buscarPorId(UUID idTransporte) {
        UUID userId = currentUserService.getCurrentUserId();

        return transporteRepository.findByIdAndUsuarioId(idTransporte, userId)
                .orElseThrow(() -> new TransporteNotFoundException("Transport não encontrado"));
    }

    public List<Student> listarAlunos(UUID idTransporte) {
        Transport transport = buscarPorId(idTransporte);
        return transport.getAlunosTransportes();
    }

    public List<Transport> listarTransportes() {
        UUID userId = currentUserService.getCurrentUserId();
        return transporteRepository.findAllByIdUsuario(userId);
    }

    public Transport salvarTransporte(TransporteRequest request) {
        Transport transport = new Transport();

        aplicarDados(transport, request);

        UUID userId = currentUserService.getCurrentUserId();
        User user = usuarioService.buscarPorId(userId);

        transport.setUser(user);

        return transporteRepository.save(transport);
    }

    public Transport atualizarTransporte(UUID idTransporte, TransporteRequest request) {
        Transport transport = buscarPorId(idTransporte);

        aplicarDados(transport, request);

        return transporteRepository.save(transport);
    }

    public void excluirTransporte(UUID idTransporte) {
        Transport transport = buscarPorId(idTransporte);
        transporteRepository.delete(transport);
    }

    private void aplicarDados(Transport transport, TransporteRequest request) {
        transport.setPlaca(request.placa());
        transport.setModelo(request.modelo());
        transport.setCapacidade(request.capacidade());
    }
}
