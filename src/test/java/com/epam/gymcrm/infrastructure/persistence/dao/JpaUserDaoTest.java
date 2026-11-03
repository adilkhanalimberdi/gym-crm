package com.epam.gymcrm.infrastructure.persistence.dao;

import com.epam.gymcrm.dao.UserDao;
import com.epam.gymcrm.infrastructure.persistence.entity.UserEntity;
import com.epam.gymcrm.support.PersistenceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JpaUserDaoTest extends PersistenceTest {

    @Autowired
    private UserDao userDao;

    private void persistUser(String username) {
        entityManager.persist(new UserEntity("First", "Last", username, "pw", true));
    }

    @Test
    void findByUsername_returnsMatchingUser() {
        persistUser("John.Smith");

        assertEquals("John.Smith", userDao.findByUsername("John.Smith").orElseThrow().getUsername());
        assertTrue(userDao.findByUsername("John.Smith1").isEmpty());
    }

    @Test
    void findUsernamesStartingWith_returnsBaseAndSerialisedUsernames() {
        persistUser("John.Smith");
        persistUser("John.Smith1");
        persistUser("John.Smythe");
        persistUser("Mary.Smith");

        List<String> usernames = userDao.findUsernamesStartingWith("John.Smith");

        assertEquals(List.of("John.Smith", "John.Smith1"), usernames.stream().sorted().toList());
    }

    @Test
    void findUsernamesStartingWith_treatsLikeWildcardsAsPlainCharacters() {
        persistUser("Jo_n.Smith");
        persistUser("John.Smith");

        assertEquals(List.of("Jo_n.Smith"), userDao.findUsernamesStartingWith("Jo_n"));
    }
}
