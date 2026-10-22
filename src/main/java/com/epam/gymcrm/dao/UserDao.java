package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    Optional<User> findByUsername(String username);

    /** Returns every username that starts with the given prefix (used to find a free serial number). */
    List<String> findUsernamesStartingWith(String prefix);
}
