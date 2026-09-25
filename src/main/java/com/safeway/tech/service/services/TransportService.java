package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.transport.TransportRequest;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.infra.exception.TransportNotFoundException;
import com.safeway.tech.repository.TransportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransportService {

    private final TransportRepository transportRepository;
    private final UserService userService;
    private final CurrentUserService currentUserService;

    public Transport buscarPorId(UUID idTransporte) {
        UUID userId = currentUserService.getCurrentUserId();

        return transportRepository.findByIdAndUsuarioId(idTransporte, userId)
                .orElseThrow(() -> new TransportNotFoundException("Transport não encontrado"));
    }

    public List<Student> listarAlunos(UUID idTransporte) {
        Transport transport = buscarPorId(idTransporte);
        return transport.getAlunosTransportes();
    }

    public List<Transport> listarTransportes() {
        UUID userId = currentUserService.getCurrentUserId();
        return transportRepository.findAllByIdUsuario(userId);
    }

    public Transport salvarTransporte(TransportRequest request) {
        Transport transport = new Transport();

        aplicarDados(transport, request);

        UUID userId = currentUserService.getCurrentUserId();
        User user = userService.buscarPorId(userId);

        transport.setUser(user);

        return transportRepository.save(transport);
    }

    public Transport atualizarTransporte(UUID idTransporte, TransportRequest request) {
        Transport transport = buscarPorId(idTransporte);

        aplicarDados(transport, request);

        return transportRepository.save(transport);
    }

    public void excluirTransporte(UUID idTransporte) {
        Transport transport = buscarPorId(idTransporte);
        transportRepository.delete(transport);
    }

    private void aplicarDados(Transport transport, TransportRequest request) {
        transport.setPlaca(request.placa());
        transport.setModelo(request.modelo());
        transport.setCapacidade(request.capacidade());
    }
}
