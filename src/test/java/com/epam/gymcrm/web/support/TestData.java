package com.epam.gymcrm.web.support;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import org.springframework.http.HttpHeaders;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;

/** Builders shared by the REST tests. */
public final class TestData {

    public static final String JOHN = "John.Smith";
    public static final String ANNA = "Anna.Lee";

    private TestData() {
    }

    public static String basic(String username, String password) {
        String pair = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(pair.getBytes(StandardCharsets.UTF_8));
    }

    public static HttpHeaders johnAuth() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, basic(JOHN, "secret"));
        return headers;
    }

    /** TrainingType is read only and has no public constructor, so the test data is filled in by reflection. */
    public static TrainingType yoga() {
        try {
            Constructor<TrainingType> constructor = TrainingType.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            TrainingType type = constructor.newInstance();
            setField(type, "id", 2L);
            setField(type, "name", "Yoga");
            return type;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void setField(Object target, String name, Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    public static Trainee trainee() {
        return new Trainee(new User("John", "Smith", JOHN, "secret"), LocalDate.of(1992, 7, 20), "7 River Road");
    }

    public static Trainer trainer() {
        return new Trainer(new User("Anna", "Lee", ANNA, "pw"), yoga());
    }

    public static Training training(Trainee trainee, Trainer trainer) {
        return new Training(trainee, trainer, "Morning Yoga", trainer.getSpecialization(),
                LocalDate.of(2026, 11, 2), 60);
    }
}
