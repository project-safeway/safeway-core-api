package com.safeway.tech.repository.specification;

import com.safeway.tech.domain.enums.AttendanceStatusEnum;
import com.safeway.tech.domain.models.Attendance;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

public class ChamadaSpecs {

    public static Specification<Attendance> comItinerarioId(UUID itinerarioId) {
        return (root, query, cb) ->
                itinerarioId == null ? null : cb.equal(root.get("itinerario").get("id"), itinerarioId);
    }

    public static Specification<Attendance> comStatus(List<AttendanceStatusEnum> statusList) {
        return (root, query, cb) ->
                statusList == null || statusList.isEmpty() ? null : root.get("status").in(statusList);
    }

    public static Specification<Attendance> comTransporte(UUID transporteId) {
        return (root, query, cb) ->
                transporteId == null ? null : cb.equal(root.get("itinerario").get("transporte").get("id"), transporteId);
    }

    public static Specification<Attendance> comUsuario(UUID usuarioId) {
        return (root, query, cb) ->
                usuarioId == null ? null : cb.equal(root.get("itinerario").get("transporte").get("usuario").get("id"), usuarioId);
    }
}
