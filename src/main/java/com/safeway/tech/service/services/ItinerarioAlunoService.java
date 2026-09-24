package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.itinerario.AlunoComLocalizacao;
import com.safeway.tech.api.dto.itinerario.ItinerarioAlunoRequest;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.domain.models.Itinerario;
import com.safeway.tech.domain.models.ItinerarioAluno;
import com.safeway.tech.domain.models.Guardian;
import com.safeway.tech.infra.exception.AlunoNotFoundException;
import com.safeway.tech.infra.exception.CoordinatesNotValidException;
import com.safeway.tech.infra.exception.EnderecoNotFoundException;
import com.safeway.tech.infra.exception.ItinerarioNotFoundException;
import com.safeway.tech.infra.exception.OperationNotAllowedException;
import com.safeway.tech.repository.ItinerarioAlunoRepository;
import com.safeway.tech.repository.ItinerarioRepository;
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
public class ItinerarioAlunoService {

    private final ItinerarioAlunoRepository itinerarioAlunoRepository;
    private final ItinerarioRepository itinerarioRepository;
    private final AlunoService alunoService;
    private final EnderecoService enderecoService;

    public List<ItinerarioAluno> buscarPorItinerarioId(UUID itinerarioId) {
        return itinerarioAlunoRepository.findByItinerarioId(itinerarioId);
    }

    public void salvarTodos(List<ItinerarioAluno> alunos) {
        itinerarioAlunoRepository.saveAll(alunos);
    }

    @Transactional
    public void adicionarAluno(UUID itinerarioId, ItinerarioAlunoRequest request) {
        Itinerario itinerario = itinerarioRepository.findById(itinerarioId)
                .orElseThrow(() -> new ItinerarioNotFoundException("Itinerário não encontrado"));

        Student student = alunoService.buscarPorId(request.alunoId());

        // Determinar endereço: usar request.enderecoId() se presente, caso contrário tentar fallback
        Address address;
        if (request.enderecoId() != null) {
            address = enderecoService.buscarPorId(request.enderecoId());

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
        Optional<ItinerarioAluno> ia = itinerarioAlunoRepository.findByItinerarioIdAndAlunoId(itinerarioId, student.getId());

        if (ia.isPresent()) {
            return;
        }

        ItinerarioAluno entity = new ItinerarioAluno();
        entity.setItinerario(itinerario);
        entity.setStudent(student);
        entity.setAddress(address);
        entity.setOrdemEmbarque(request.ordemEmbarque());

        itinerarioAlunoRepository.save(entity);
    }

    @Transactional
    public void removerAluno(UUID itinerarioId, UUID alunoId) {
        ItinerarioAluno entity = itinerarioAlunoRepository
                .findByItinerarioIdAndAlunoId(itinerarioId, alunoId)
                .orElseThrow(() -> new AlunoNotFoundException("Student não encontrado no itinerário"));

        itinerarioAlunoRepository.delete(entity);
    }

    @Transactional
    public void sincronizarAlunos(Itinerario itinerario, List<ItinerarioAlunoRequest> novos) {
        // Remove todos os vínculos anteriores
        itinerarioAlunoRepository.deleteAllByItinerarioId(itinerario.getId());

        // Cria novos vínculos — atribui endereco e valida se pertence ao responsável
        List<ItinerarioAluno> entidades = novos.stream().map(dto -> {
            ItinerarioAluno ia = new ItinerarioAluno();
            ia.setItinerario(itinerario);

            Student student = alunoService.buscarPorId(dto.alunoId());

            // Determinar endereco: prefer dto.enderecoId(), senão fallback para primeiro endereco de responsavel
            Address address;
            if (dto.enderecoId() != null) {
                address = enderecoService.buscarPorId(dto.enderecoId());
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

        itinerarioAlunoRepository.saveAll(entidades);
    }

    @Transactional
    public void reordenar(UUID itinerarioId, List<UUID> novaOrdemAlunoIds) {
        // Mantido apenas para compatibilidade, mas ordemGlobal sera tratada em endpoint unificado
        List<ItinerarioAluno> atuais = itinerarioAlunoRepository.findByItinerarioId(itinerarioId);
        Map<UUID, ItinerarioAluno> map = atuais.stream()
                .collect(Collectors.toMap(a -> a.getStudent().getId(), a -> a));
        int ordem = 1;
        for (UUID id : novaOrdemAlunoIds) {
            ItinerarioAluno ia = map.get(id);
            if (ia != null) {
                ia.setOrdemEmbarque(ordem++);
            }
        }
        itinerarioAlunoRepository.saveAll(atuais);
    }

    @Transactional
    public List<AlunoComLocalizacao> buscarAlunosComLocalizacao(UUID itinerarioId) {
        Itinerario itinerario = itinerarioRepository.findById(itinerarioId)
                .orElseThrow(() -> new ItinerarioNotFoundException("Itinerário não encontrado"));

        return itinerarioAlunoRepository.findByItinerarioOrderByOrdemEmbarqueAsc(itinerario).stream()
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
