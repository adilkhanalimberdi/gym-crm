package com.epam.gymcrm.dao.jpa;

import com.epam.gymcrm.dao.UserDao;
import com.epam.gymcrm.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaUserDao implements UserDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<User> findByUsername(String username) {
        return entityManager
                .createQuery("select u from User u where u.username = :username", User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<String> findUsernamesStartingWith(String prefix) {
        return entityManager
                .createQuery("select u.username from User u where u.username like :pattern escape '\\'", String.class)
                .setParameter("pattern", escapeLikeWildcards(prefix) + "%")
                .getResultList();
    }

    private static String escapeLikeWildcards(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
