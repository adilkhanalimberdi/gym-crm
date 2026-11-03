package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.UserDao;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class UsernameGenerator {

    private final UserDao userDao;

    public UsernameGenerator(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Builds "First.Last"; if that is taken, appends the first free serial number
     * ("First.Last1", "First.Last2", ...). One query loads all candidates.
     */
    public String generate(String firstName, String lastName) {
        String base = firstName.trim() + "." + lastName.trim();
        Set<String> taken = new HashSet<>(userDao.findUsernamesStartingWith(base));

        if (!taken.contains(base)) {
            return base;
        }
        int serial = 1;
        while (taken.contains(base + serial)) {
            serial++;
        }
        return base + serial;
    }
}
