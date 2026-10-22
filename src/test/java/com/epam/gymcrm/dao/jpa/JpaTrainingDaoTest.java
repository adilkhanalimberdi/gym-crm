package com.epam.gymcrm.dao.jpa;

import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.dto.TraineeTrainingCriteria;
import com.epam.gymcrm.dto.TrainerTrainingCriteria;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.support.PersistenceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JpaTrainingDaoTest extends PersistenceTest {

    private static final LocalDate MARCH_1 = LocalDate.of(2025, 3, 1);
    private static final LocalDate MARCH_10 = LocalDate.of(2025, 3, 10);
    private static final LocalDate MARCH_20 = LocalDate.of(2025, 3, 20);

    @Autowired
    private TrainingDao trainingDao;

    @BeforeEach
    void setUpData() {
        Trainee john = persistTrainee("John", "Smith");
        Trainee emily = persistTrainee("Emily", "Brown");
        Trainer anna = persistTrainer("Anna", "Lee", "Yoga");
        Trainer mark = persistTrainer("Mark", "Taylor", "Resistance");

        persistTraining(john, anna, "Yoga basics", "Yoga", MARCH_10);
        persistTraining(john, mark, "Heavy lifting", "Resistance", MARCH_1);
        persistTraining(john, anna, "Advanced yoga", "Yoga", MARCH_20);
        persistTraining(emily, anna, "Yoga for Emily", "Yoga", MARCH_10);
        flushAndClear();
    }

    private static List<String> names(List<Training> trainings) {
        return trainings.stream().map(Training::getTrainingName).toList();
    }

    @Test
    void save_persistsTraining() {
        Trainee john = entityManager.createQuery("select t from Trainee t where t.user.username = 'John.Smith'", Trainee.class)
                .getSingleResult();
        Trainer anna = entityManager.createQuery("select t from Trainer t where t.user.username = 'Anna.Lee'", Trainer.class)
                .getSingleResult();

        Training saved = trainingDao.save(new Training(john, anna, "New one", trainingType("Fitness"), MARCH_1, 45));
        flushAndClear();

        assertEquals(45, entityManager.find(Training.class, saved.getId()).getDuration());
    }

    @Test
    void findByTrainee_withoutCriteria_returnsOnlyThatTraineesTrainingsOrderedByDate() {
        List<Training> trainings = trainingDao.findByTrainee("John.Smith", TraineeTrainingCriteria.none());

        assertEquals(List.of("Heavy lifting", "Yoga basics", "Advanced yoga"), names(trainings));
    }

    @Test
    void findByTrainee_filtersByDateRange() {
        var criteria = new TraineeTrainingCriteria(MARCH_10, MARCH_20, null, null);

        assertEquals(List.of("Yoga basics", "Advanced yoga"), names(trainingDao.findByTrainee("John.Smith", criteria)));
    }

    @Test
    void findByTrainee_filtersByOpenEndedDates() {
        assertEquals(List.of("Heavy lifting"),
                names(trainingDao.findByTrainee("John.Smith", new TraineeTrainingCriteria(null, MARCH_1, null, null))));
        assertEquals(List.of("Advanced yoga"),
                names(trainingDao.findByTrainee("John.Smith", new TraineeTrainingCriteria(MARCH_20, null, null, null))));
    }

    @Test
    void findByTrainee_filtersByTrainerNameIgnoringCase() {
        var criteria = new TraineeTrainingCriteria(null, null, "mark", null);

        assertEquals(List.of("Heavy lifting"), names(trainingDao.findByTrainee("John.Smith", criteria)));
    }

    @Test
    void findByTrainee_filtersByTrainerLastName() {
        var criteria = new TraineeTrainingCriteria(null, null, "Lee", null);

        assertEquals(2, trainingDao.findByTrainee("John.Smith", criteria).size());
    }

    @Test
    void findByTrainee_filtersByTrainingType() {
        var criteria = new TraineeTrainingCriteria(null, null, null, "resistance");

        assertEquals(List.of("Heavy lifting"), names(trainingDao.findByTrainee("John.Smith", criteria)));
    }

    @Test
    void findByTrainee_combinesAllFilters() {
        var criteria = new TraineeTrainingCriteria(MARCH_10, MARCH_20, "Anna", "Yoga");

        assertEquals(List.of("Yoga basics", "Advanced yoga"), names(trainingDao.findByTrainee("John.Smith", criteria)));
    }

    @Test
    void findByTrainee_treatsBlankCriteriaAsNoFilter() {
        var criteria = new TraineeTrainingCriteria(null, null, "  ", "");

        assertEquals(3, trainingDao.findByTrainee("John.Smith", criteria).size());
    }

    @Test
    void findByTrainee_returnsEmpty_whenNothingMatches() {
        var criteria = new TraineeTrainingCriteria(null, null, "Nobody", null);

        assertTrue(trainingDao.findByTrainee("John.Smith", criteria).isEmpty());
    }

    @Test
    void findByTrainer_withoutCriteria_returnsOnlyThatTrainersTrainings() {
        List<Training> trainings = trainingDao.findByTrainer("Anna.Lee", TrainerTrainingCriteria.none());

        assertEquals(List.of("Yoga basics", "Yoga for Emily", "Advanced yoga"), names(trainings));
    }

    @Test
    void findByTrainer_filtersByDateRangeAndTraineeName() {
        var criteria = new TrainerTrainingCriteria(MARCH_1, MARCH_10, "emily");

        assertEquals(List.of("Yoga for Emily"), names(trainingDao.findByTrainer("Anna.Lee", criteria)));
    }

    @Test
    void findByTrainer_filtersByOpenEndedDates() {
        assertEquals(List.of("Advanced yoga"),
                names(trainingDao.findByTrainer("Anna.Lee", new TrainerTrainingCriteria(MARCH_20, null, null))));
        assertEquals(List.of("Yoga basics", "Yoga for Emily"),
                names(trainingDao.findByTrainer("Anna.Lee", new TrainerTrainingCriteria(null, MARCH_10, null))));
    }
}
