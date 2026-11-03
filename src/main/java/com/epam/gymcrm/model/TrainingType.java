package com.epam.gymcrm.model;

/** One value of the constant list of training types; it has no setters because it cannot be changed. */
public class TrainingType {

    private final Long id;
    private final String name;

    public TrainingType(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "TrainingType[id=" + id + ", name=" + name + "]";
    }
}
