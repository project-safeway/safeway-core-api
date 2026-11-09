package com.safeway.tech.repository;

import com.safeway.tech.domain.enums.UserRole;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.domain.models.UserPhoto;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.sql.DriverManager;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserPhotoPersistenceTest {

    @ParameterizedTest
    @ValueSource(strings = {"org.hibernate.dialect.MySQLDialect", "org.hibernate.dialect.H2Dialect"})
    void storesReplacesAndDeletesPhotoUsingMigrationSchema(String dialect) throws Exception {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        Configuration configuration = new Configuration()
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Transport.class)
                .addAnnotatedClass(UserPhoto.class)
                .setProperty("hibernate.connection.url", url)
                .setProperty("hibernate.connection.username", "sa")
                .setProperty("hibernate.connection.password", "")
                .setProperty("hibernate.physical_naming_strategy",
                        "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy")
                .setProperty("hibernate.dialect", dialect)
                .setProperty("hibernate.hbm2ddl.auto", "create-only");

        try (var factory = configuration.buildSessionFactory()) {
            try (var connection = DriverManager.getConnection(url, "sa", "");
                    var statement = connection.createStatement()) {
                statement.execute("DROP TABLE user_photos");
                ScriptUtils.executeSqlScript(connection,
                        new ClassPathResource("db.migration/V1.0.2__user_profile_photo.sql"));
            }
            UUID photoId;
            UUID userId;
            byte[] content = new byte[100_000];
            content[99_999] = 42;
            try (var session = factory.openSession()) {
                var transaction = session.beginTransaction();
                User user = new User();
                user.setName("Driver");
                user.setEmail("photo@example.com");
                user.setPasswordHash("unused");
                user.setRole(UserRole.DRIVER);
                user.setPrimaryPhoneNumber("11999999999");
                user.setCreatedAt(Instant.now());
                user.setUpdatedAt(Instant.now());
                session.persist(user);
                UserPhoto photo = new UserPhoto();
                photo.setUser(user);
                photo.setContent(content);
                photo.setContentType("image/png");
                photo.setCreatedAt(Instant.now());
                photo.setUpdatedAt(Instant.now());
                session.persist(photo);
                transaction.commit();
                photoId = photo.getId();
                userId = user.getId();
            }
            try (var session = factory.openSession()) {
                var transaction = session.beginTransaction();
                UserPhoto photo = session.createQuery(
                                "from UserPhoto where user.id = :userId and active = true", UserPhoto.class)
                        .setParameter("userId", userId)
                        .getSingleResult();
                assertArrayEquals(content, photo.getContent());
                assertEquals("image/png", photo.getContentType());
                photo.setContent(new byte[]{9, 8, 7});
                transaction.commit();
            }
            try (var session = factory.openSession()) {
                var transaction = session.beginTransaction();
                UserPhoto photo = session.find(UserPhoto.class, photoId);
                assertArrayEquals(new byte[]{9, 8, 7}, photo.getContent());
                session.remove(photo);
                transaction.commit();
            }
            try (var session = factory.openSession()) {
                assertNull(session.find(UserPhoto.class, photoId));
                assertEquals(userId, session.find(User.class, userId).getId());
            }
        }
    }
}
