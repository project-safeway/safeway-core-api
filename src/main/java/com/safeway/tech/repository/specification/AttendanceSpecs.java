package com.safeway.tech.repository.specification;

import com.safeway.tech.domain.enums.AttendanceStatusEnum;
import com.safeway.tech.domain.models.Attendance;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

public class AttendanceSpecs {

    public static Specification<Attendance> withRouteId(UUID routeId) {
        return (root, query, cb) ->
                routeId == null ? null : cb.equal(root.get("route").get("id"), routeId);
    }

    public static Specification<Attendance> withStatus(List<AttendanceStatusEnum> statusList) {
        return (root, query, cb) ->
                statusList == null || statusList.isEmpty() ? null : root.get("status").in(statusList);
    }
}
