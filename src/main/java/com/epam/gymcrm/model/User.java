package com.epam.gymcrm.model;

/** Account data shared by trainees and trainers. */
public class User {

    private final Long id;
    private final String username;
    private String firstName;
    private String lastName;
    private String password;
    private boolean active;

    /** A new, active user that has not been stored yet. */
    public User(String firstName, String lastName, String username, String password) {
        this(null, firstName, lastName, username, password, true);
    }

    public User(Long id,
                String firstName,
                String lastName,
                String username,
                String password,
                boolean active) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.active = active;
    }

    /** Activates an inactive user and deactivates an active one. */
    public void toggleActive() {
        active = !active;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return active;
    }

    /** The password is deliberately left out so it can never leak into logs. */
    @Override
    public String toString() {
        return "User[id=" + id + ", username=" + username + ", active=" + active + "]";
    }
}
