package com.safeway.tech.domain.enums;

import lombok.Getter;

@Getter
public enum EducationLevelEnum {

    HIGH_SCHOOL("HIGH_SCHOOL"),
    MIDDLE_SCHOOL("MIDDLE_SCHOOL"),
    DAYCARE_CENTER("DAYCARE_CENTER"),
    KINDERGARTEN("KINDERGARTEN");

    private final String level;

    EducationLevelEnum(String level) {
        this.level = level;
    }

}
