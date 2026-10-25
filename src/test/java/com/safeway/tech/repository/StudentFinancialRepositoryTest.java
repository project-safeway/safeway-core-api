package com.safeway.tech.repository;

import com.safeway.tech.api.dto.student.StudentFeignResponse;
import com.safeway.tech.domain.enums.UserRole;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.domain.models.BaseEntity;
import com.safeway.tech.domain.models.School;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.domain.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class StudentFinancialRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private StudentRepository repository;

    @Test
    void projectsDriverOwnershipWithoutGuardiansAndIsolatesTransports() {
        Student first = createStudent("AAA0001");
        Student second = createStudent("BBB0002");

        List<StudentFeignResponse> ownStudents = repository.findFinancialStudents(first.getTransport().getId());

        assertThat(ownStudents).hasSize(1);
        assertThat(ownStudents.getFirst().id()).isEqualTo(first.getId());
        assertThat(ownStudents.getFirst().transportId()).isEqualTo(first.getTransport().getId());
        User driver = entityManager.getEntityManager()
                .createQuery("SELECT u FROM User u WHERE u.transport = :transport", User.class)
                .setParameter("transport", first.getTransport()).getSingleResult();
        assertThat(ownStudents.getFirst().motoristaId()).isEqualTo(driver.getId());
        assertThat(repository.findFinancialStudentsByIds(
                List.of(second.getId()), first.getTransport().getId())).isEmpty();
        assertThat(repository.findFinancialStudents(null))
                .extracting(StudentFeignResponse::id).containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void excludesInactiveStudentsFromMonthlyGeneration() {
        Student student = createStudent("CCC0003");
        student.setActive(false);
        entityManager.flush();

        assertThat(repository.findFinancialStudents(null)).isEmpty();
    }

    private Student createStudent(String plate) {
        Transport transport = new Transport();
        transport.setLicensePlate(plate);
        persist(transport);
        User driver = new User();
        driver.setName("Driver");
        driver.setEmail(plate + "@example.com");
        driver.setPasswordHash("hash");
        driver.setPrimaryPhoneNumber("11999999999");
        driver.setRole(UserRole.DRIVER);
        driver.setTransport(transport);
        persist(driver);
        Address address = new Address();
        address.setStreet("Street");
        address.setNumber("1");
        address.setNeighborhood("Center");
        address.setCity("City");
        address.setFederalUnit("SP");
        address.setZipCode("00000-000");
        address.setType("SCHOOL");
        persist(address);
        School school = new School();
        school.setName("School");
        school.setAddress(address);
        school.setTransport(transport);
        persist(school);
        Student student = new Student();
        student.setName("Student");
        student.setProfessor("Professor");
        student.setMonthlyFee(500.0);
        student.setDueDate(15);
        student.setSchool(school);
        student.setTransport(transport);
        return persist(student);
    }

    private <T extends BaseEntity> T persist(T entity) {
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entityManager.persistAndFlush(entity);
    }
}
