package org.synyx.urlaubsverwaltung.overtime.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OvertimeDetailPersonDtoTest {

    @Test
    void equals() {
        final OvertimeDetailPersonDto personDto = new OvertimeDetailPersonDto(1, "mail@example.org", "niceName", "gravatarUrl", false);
        final OvertimeDetailPersonDto personDtoOne = new OvertimeDetailPersonDto(1, "mail@example.org", "niceName", "gravatarUrl", false);

        final OvertimeDetailPersonDto personDtoTwo = new OvertimeDetailPersonDto(1, "differentMail@example.org", "niceName", "gravatarUrl", false);

        assertThat(personDto)
            .isEqualTo(personDto)
            .isEqualTo(personDtoOne)
            .isNotEqualTo(personDtoTwo)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void hashCodeTest() {
        final OvertimeDetailPersonDto personDtoOne = new OvertimeDetailPersonDto(1, "mail@example.org", "niceName", "gravatarUrl", false);
        assertThat(personDtoOne.hashCode()).isEqualTo(-754489482);
    }
}
