package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.route.RouteStudentRequest;
import com.safeway.tech.api.dto.route.StudentWithAddress;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.domain.models.Route;
import com.safeway.tech.domain.models.RouteStudent;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.infra.exception.CoordinatesNotValidException;
import com.safeway.tech.infra.exception.RouteNotFoundException;
import com.safeway.tech.infra.exception.StudentNotFoundException;
import com.safeway.tech.repository.RouteRepository;
import com.safeway.tech.repository.RouteStudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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

    public List<RouteStudent> findByRouteId(UUID routeId) {
        return routeStudentRepository.findByRouteId(routeId);
    }

    public void saveAll(List<RouteStudent> students) {
        routeStudentRepository.saveAll(students);
    }

    @Transactional
    public void addStudent(UUID routeId, RouteStudentRequest request) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException("Itinerário não encontrado"));

        Student student = studentService.findById(request.studentId());

        // Determinar endereço: usar request.addressId() se presente, caso contrário tentar fallback
        // TODO: Ajustar buscar do endereço
        Address address = addressService.findById(student.getId());

        // Validar que o endereço tem lat/lng válidos antes de prosseguir
        if (address.getLatitude() == null || address.getLongitude() == null) {
            throw new CoordinatesNotValidException("Endereço selecionado não possui latitude/longitude válidas");
        }

        BigDecimal lat = address.getLatitude();
        BigDecimal lng = address.getLongitude();

        if (lat.compareTo(BigDecimal.valueOf(-90)) < 0 || lat.compareTo(BigDecimal.valueOf(90)) > 0
                || lng.compareTo(BigDecimal.valueOf(-180)) < 0 || lng.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new CoordinatesNotValidException("Coordenadas do endereço inválidas: " + lat + ", " + lng);
        }

        // Evita duplicidade
        Optional<RouteStudent> routeStudent = routeStudentRepository.findByRouteIdAndStudentId(routeId, student.getId());

        if (routeStudent.isPresent()) {
            return;
        }

        RouteStudent entity = new RouteStudent();
        entity.setRoute(route);
        entity.setStudent(student);
        entity.setAddress(address);
        entity.setBoardingOrder(request.boardingOrder());

        routeStudentRepository.save(entity);
    }

    @Transactional
    public void removeStudent(UUID routeId, UUID studentId) {
        RouteStudent entity = routeStudentRepository
                .findByRouteIdAndStudentId(routeId, studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student não encontrado no itinerário"));

        routeStudentRepository.delete(entity);
    }

    @Transactional
    public void syncStudents(Route route, List<RouteStudentRequest> newStudents) {
        // Remove todos os vínculos anteriores
        routeStudentRepository.deleteAllByRouteId(route.getId());

        // Cria novos vínculos — atribui endereco e valida se pertence ao responsável
        List<RouteStudent> entities = newStudents.stream().map(dto -> {
            RouteStudent ia = new RouteStudent();
            ia.setRoute(route);

            Student student = studentService.findById(dto.studentId());

            // Determinar endereco: prefer dto.addressId(), senão fallback para primeiro endereco de responsavel
            // TODO: Ajustar buscar do endereço
            Address address = addressService.findById(student.getId());

            // validar lat/lng
            if (address.getLatitude() == null || address.getLongitude() == null) {
                throw new CoordinatesNotValidException("Endereço do student (id=" + student.getId() + ") não possui latitude/longitude válidas");
            }

            BigDecimal lat = address.getLatitude();
            BigDecimal lng = address.getLongitude();

            if (lat.compareTo(BigDecimal.valueOf(-90)) < 0 || lat.compareTo(BigDecimal.valueOf(90)) > 0
                    || lng.compareTo(BigDecimal.valueOf(-180)) < 0 || lng.compareTo(BigDecimal.valueOf(180)) > 0) {
                throw new CoordinatesNotValidException("Coordenadas do endereço inválidas para student id=" + student.getId() + ": " + lat + ", " + lng);
            }

            ia.setStudent(student);
            ia.setAddress(address);
            ia.setBoardingOrder(dto.boardingOrder());
            return ia;
        }).toList();

        routeStudentRepository.saveAll(entities);
    }

    @Transactional
    public void reorder(UUID routeId, List<UUID> newStudentIdsOrder) {
        List<RouteStudent> actualOrder = routeStudentRepository.findByRouteId(routeId);

        Map<UUID, RouteStudent> studentMap = actualOrder.stream()
                .collect(Collectors.toMap(a -> a.getStudent().getId(), a -> a));

        int order = 1;

        for (UUID id : newStudentIdsOrder) {
            RouteStudent routeStudent = studentMap.get(id);
            if (routeStudent != null) {
                routeStudent.setBoardingOrder(order++);
            }
        }

        routeStudentRepository.saveAll(actualOrder);
    }

    @Transactional
    public List<StudentWithAddress> findStudentWithAddress(UUID routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException("Itinerário não encontrado"));

        return routeStudentRepository.findByRouteOrderByBoardingOrderAsc(route).stream()
                .filter(routeStudent ->
                        routeStudent.getAddress() != null
                            && routeStudent.getAddress().getLatitude() != null
                            && routeStudent.getAddress().getLongitude() != null)
                .map(routeStudent -> {
                    Address address = routeStudent.getAddress();
                    String fullAddress = String.format("%s, %s - %s",
                            address.getStreet(),
                            address.getNumber(),
                            address.getNeighborhood()
                    );

                    return new StudentWithAddress(
                            routeStudent.getStudent().getId(),
                            routeStudent.getStudent().getName(),
                            address.getId(),
                            fullAddress,
                            address.getLatitude(),
                            address.getLongitude(),
                            routeStudent.getBoardingOrder()
                    );
                })
                .toList();
    }
}
