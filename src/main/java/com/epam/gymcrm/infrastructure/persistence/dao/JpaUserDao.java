package com.epam.gymcrm.infrastructure.persistence.dao;

import com.epam.gymcrm.dao.UserDao;
import com.epam.gymcrm.infrastructure.persistence.entity.UserEntity;
import com.epam.gymcrm.infrastructure.persistence.mapper.EntityMapper;
import com.epam.gymcrm.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaUserDao implements UserDao {

    private final EntityMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    public JpaUserDao(EntityMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return entityManager
                .createQuery("select u from UserEntity u where u.username = :username", UserEntity.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .map(mapper::toDomain);
    }

    @Override
    public List<String> findUsernamesStartingWith(String prefix) {
        return entityManager
                .createQuery("select u.username from UserEntity u where u.username like :pattern escape '\\'", String.class)
                .setParameter("pattern", escapeLikeWildcards(prefix) + "%")
                .getResultList();
    }

    private static String escapeLikeWildcards(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
