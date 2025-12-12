package com.sportsgear.rentalplatform.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_ShouldReturnUser() {
        // GIVEN
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("password")
                .role(Role.USER)
                .build();
        
        entityManager.persist(user);
        entityManager.flush();

        // WHEN
        User found = userRepository.findByEmail("test@example.com");

        // THEN
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("test@example.com");
        assertThat(found.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void findByEmail_WhenEmailNotExists_ShouldReturnNull() {
        // WHEN
        User found = userRepository.findByEmail("nonexistent@example.com");

        // THEN
        assertThat(found).isNull();
    }
}