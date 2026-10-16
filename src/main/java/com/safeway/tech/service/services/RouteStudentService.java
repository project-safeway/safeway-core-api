package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.route.RouteStudentRequest;
import com.safeway.tech.api.dto.route.RouteUpdateRequest.RouteStopUpdate;
import com.safeway.tech.api.dto.route.StudentWithAddress;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.domain.models.Guardian;
import com.safeway.tech.domain.models.Route;
import com.safeway.tech.domain.models.RouteStudent;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.infra.exception.CoordinatesNotValidException;
import com.safeway.tech.infra.exception.EntityNotFoundException;
import com.safeway.tech.repository.GuardianRepository;
import com.safeway.tech.repository.RouteRepository;
import com.safeway.tech.repository.RouteStudentRepository;
import com.safeway.tech.service.services.CurrentUserService;
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
    private final GuardianRepository guardianRepository;
    private final CurrentUserService currentUserService;

    public List<RouteStudent> findByRouteId(UUID routeId) {
        return routeStudentRepository.findByRouteId(routeId);
    }

    public void saveAll(List<RouteStudent> students) {
        routeStudentRepository.saveAll(students);
    }

    @Transactional
    public void updateGeneralOrder(UUID routeId, List<RouteStopUpdate> stops) {
        Map<UUID, Integer> orderByStudentId = stops.stream()
                .filter(stop -> "ALUNO".equalsIgnoreCase(stop.type()))
                .collect(Collectors.toMap(RouteStopUpdate::id, RouteStopUpdate::generalOrder));
        List<RouteStudent> students = routeStudentRepository.findByRouteId(routeId);
        students.forEach(student -> student.setGeneralOrder(orderByStudentId.get(student.getStudent().getId())));
        routeStudentRepository.saveAll(students);
    }

    @Transactional
    public void addStudent(UUID routeId, RouteStudentRequest request) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new EntityNotFoundException("Route was not found"));

        Student student = studentService.findByIdForCurrentTransport(request.studentId());
        Address address = resolveStudentAddress(student, request.addressId());

        // Validate address coordinates before proceeding.
        if (address.getLatitude() == null || address.getLongitude() == null) {
            throw new CoordinatesNotValidException("Selected address does not have valid latitude/longitude");
        }

        BigDecimal lat = address.getLatitude();
        BigDecimal lng = address.getLongitude();

        if (lat.compareTo(BigDecimal.valueOf(-90)) < 0 || lat.compareTo(BigDecimal.valueOf(90)) > 0
                || lng.compareTo(BigDecimal.valueOf(-180)) < 0 || lng.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new CoordinatesNotValidException("Invalid address coordinates: " + lat + ", " + lng);
        }

        // Avoid duplicates.
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
                .orElseThrow(() -> new EntityNotFoundException("Student was not found on this route"));

        routeStudentRepository.delete(entity);
    }

    @Transactional
    public void syncStudents(Route route, List<RouteStudentRequest> newStudents) {
        // Remove all previous links.
        routeStudentRepository.deleteAllByRouteId(route.getId());

        // Create new links, assign addresses, and validate guardian ownership.
        List<RouteStudent> entities = newStudents.stream().map(dto -> {
            RouteStudent ia = new RouteStudent();
            ia.setRoute(route);

            Student student = studentService.findByIdForCurrentTransport(dto.studentId());
            Address address = resolveStudentAddress(student, dto.addressId());

            // Validate coordinates.
            if (address.getLatitude() == null || address.getLongitude() == null) {
                throw new CoordinatesNotValidException("Student address (id=" + student.getId()
                        + ") does not have valid latitude/longitude");
            }

            BigDecimal lat = address.getLatitude();
            BigDecimal lng = address.getLongitude();

            if (lat.compareTo(BigDecimal.valueOf(-90)) < 0 || lat.compareTo(BigDecimal.valueOf(90)) > 0
                    || lng.compareTo(BigDecimal.valueOf(-180)) < 0 || lng.compareTo(BigDecimal.valueOf(180)) > 0) {
                throw new CoordinatesNotValidException("Invalid address coordinates for student id="
                        + student.getId() + ": " + lat + ", " + lng);
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
                .orElseThrow(() -> new EntityNotFoundException("Route was not found"));

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

    private Address resolveStudentAddress(Student student, UUID requestedAddressId) {
        List<Guardian> guardians = guardianRepository.findByStudentIdAndTransportId(
                student.getId(),
                currentUserService.getCurrentTransportId());

        if (requestedAddressId != null) {
            return guardians.stream()
                    .map(Guardian::getAddress)
                    .filter(address -> address != null && requestedAddressId.equals(address.getId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Address is not associated with this student's guardian"));
        }

        return guardians.stream()
                .map(Guardian::getAddress)
                .filter(address -> address != null)
                .sorted((left, right) -> Boolean.compare(
                        Boolean.TRUE.equals(right.getPrincipal()),
                        Boolean.TRUE.equals(left.getPrincipal())))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "No guardian address is available for this student"));
    }
}
