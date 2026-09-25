package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.route.AlunoComLocalizacao;
import com.safeway.tech.api.dto.route.ItinerarioAlunoRequest;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.domain.models.Route;
import com.safeway.tech.domain.models.RouteStudent;
import com.safeway.tech.domain.models.Guardian;
import com.safeway.tech.infra.exception.AlunoNotFoundException;
import com.safeway.tech.infra.exception.CoordinatesNotValidException;
import com.safeway.tech.infra.exception.EnderecoNotFoundException;
import com.safeway.tech.infra.exception.ItinerarioNotFoundException;
import com.safeway.tech.infra.exception.OperationNotAllowedException;
import com.safeway.tech.repository.RouteStudentRepository;
import com.safeway.tech.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteStudentService {

    private final RouteStudentRepository routeStudentRepository;
    private final RouteRepository routeRepository;
    private final StudentService studentService;
    private final AddressService addressService;

    public List<RouteStudent> buscarPorItinerarioId(UUID itinerarioId) {
        return routeStudentRepository.findByItinerarioId(itinerarioId);
    }

    public void salvarTodos(List<RouteStudent> alunos) {
        routeStudentRepository.saveAll(alunos);
    }

    @Transactional
    public void adicionarAluno(UUID itinerarioId, ItinerarioAlunoRequest request) {
        Route route = routeRepository.findById(itinerarioId)
                .orElseThrow(() -> new ItinerarioNotFoundException("Itinerário não encontrado"));

        Student student = studentService.buscarPorId(request.alunoId());

        // Determinar endereço: usar request.enderecoId() se presente, caso contrário tentar fallback
        Address address;
        if (request.enderecoId() != null) {
            address = addressService.buscarPorId(request.enderecoId());

            boolean enderecoPerenceAoResponsavel = student.getResponsaveis().stream()
                    .anyMatch(r -> r.getEndereco() != null && r.getEndereco().getId().equals(address.getId()));

            if (!enderecoPerenceAoResponsavel) {
                throw new OperationNotAllowedException("Endereço não pertence a nenhum responsável do student");
            }
        } else {
            address = student.getResponsaveis().stream()
                    .map(Guardian::getAddress)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseThrow(() -> new EnderecoNotFoundException("Nenhum endereço disponível para o responsável do student"));
        }

        // Validar que o endereço tem lat/lng válidos antes de prosseguir
        if (address.getLatitude() == null || address.getLongitude() == null) {
            throw new CoordinatesNotValidException("Endereço selecionado não possui latitude/longitude válidas");
        }

        double lat = address.getLatitude();
        double lng = address.getLongitude();

        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new CoordinatesNotValidException("Coordenadas do endereço inválidas: " + lat + ", " + lng);
        }

        // Evita duplicidade
        Optional<RouteStudent> ia = routeStudentRepository.findByItinerarioIdAndAlunoId(itinerarioId, student.getId());

        if (ia.isPresent()) {
            return;
        }

        RouteStudent entity = new RouteStudent();
        entity.setRoute(route);
        entity.setStudent(student);
        entity.setAddress(address);
        entity.setOrdemEmbarque(request.ordemEmbarque());

        routeStudentRepository.save(entity);
    }

    @Transactional
    public void removerAluno(UUID itinerarioId, UUID alunoId) {
        RouteStudent entity = routeStudentRepository
                .findByItinerarioIdAndAlunoId(itinerarioId, alunoId)
                .orElseThrow(() -> new AlunoNotFoundException("Student não encontrado no itinerário"));

        routeStudentRepository.delete(entity);
    }

    @Transactional
    public void sincronizarAlunos(Route route, List<ItinerarioAlunoRequest> novos) {
        // Remove todos os vínculos anteriores
        routeStudentRepository.deleteAllByItinerarioId(route.getId());

        // Cria novos vínculos — atribui endereco e valida se pertence ao responsável
        List<RouteStudent> entidades = novos.stream().map(dto -> {
            RouteStudent ia = new RouteStudent();
            ia.setRoute(route);

            Student student = studentService.buscarPorId(dto.alunoId());

            // Determinar endereco: prefer dto.enderecoId(), senão fallback para primeiro endereco de responsavel
            Address address;
            if (dto.enderecoId() != null) {
                address = addressService.buscarPorId(dto.enderecoId());
            } else {
                address = student.getResponsaveis().stream()
                        .map(Guardian::getAddress)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElseThrow(() -> new EnderecoNotFoundException("Nenhum endereço disponível para o responsável do student"));
            }

            // validar lat/lng
            if (address.getLatitude() == null || address.getLongitude() == null) {
                throw new CoordinatesNotValidException("Endereço do student (id=" + student.getId() + ") não possui latitude/longitude válidas");
            }
            double lat = address.getLatitude();
            double lng = address.getLongitude();
            if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
                throw new CoordinatesNotValidException("Coordenadas do endereço inválidas para student id=" + student.getId() + ": " + lat + ", " + lng);
            }

            boolean enderecoPerenceAoResponsavel = student.getResponsaveis().stream()
                    .anyMatch(r -> r.getEndereco() != null && r.getEndereco().getId().equals(address.getId()));

            if (!enderecoPerenceAoResponsavel) {
                // TODO: Alterar para uma exception que faça mais sentido
                throw new OperationNotAllowedException("Endereço não pertence a nenhum responsável do student");
            }

            ia.setStudent(student);
            ia.setAddress(address);
            ia.setOrdemEmbarque(dto.ordemEmbarque());
            return ia;
        }).toList();

        routeStudentRepository.saveAll(entidades);
    }

    @Transactional
    public void reordenar(UUID itinerarioId, List<UUID> novaOrdemAlunoIds) {
        // Mantido apenas para compatibilidade, mas ordemGlobal sera tratada em endpoint unificado
        List<RouteStudent> atuais = routeStudentRepository.findByItinerarioId(itinerarioId);
        Map<UUID, RouteStudent> map = atuais.stream()
                .collect(Collectors.toMap(a -> a.getStudent().getId(), a -> a));
        int ordem = 1;
        for (UUID id : novaOrdemAlunoIds) {
            RouteStudent ia = map.get(id);
            if (ia != null) {
                ia.setOrdemEmbarque(ordem++);
            }
        }
        routeStudentRepository.saveAll(atuais);
    }

    @Transactional
    public List<AlunoComLocalizacao> buscarAlunosComLocalizacao(UUID itinerarioId) {
        Route route = routeRepository.findById(itinerarioId)
                .orElseThrow(() -> new ItinerarioNotFoundException("Itinerário não encontrado"));

        return routeStudentRepository.findByItinerarioOrderByOrdemEmbarqueAsc(route).stream()
                .filter(ia -> ia.getAddress() != null && ia.getAddress().getLatitude() != null && ia.getAddress().getLongitude() != null)
                .map(ia -> {
                    Address address = ia.getAddress();
                    String enderecoCompleto = String.format("%s, %s - %s",
                            address.getLogradouro(),
                            address.getNumero(),
                            address.getBairro()
                    );

                    return new AlunoComLocalizacao(
                            ia.getStudent().getId(),
                            ia.getStudent().getNome(),
                            address.getId(),
                            enderecoCompleto,
                            address.getLatitude(),
                            address.getLongitude(),
                            ia.getOrdemEmbarque()
                    );
                })
                .toList();
    }
}
