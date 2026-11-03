package com.epam.gymcrm.infrastructure.persistence.dao;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dao.TrainerDao;
import com.epam.gymcrm.dao.TrainingDao;
import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.dto.TraineeTrainingCriteria;
import com.epam.gymcrm.dto.TrainerTrainingCriteria;
import com.epam.gymcrm.infrastructure.persistence.entity.TraineeEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainerEntity;
import com.epam.gymcrm.infrastructure.persistence.entity.TrainingEntity;
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
    @Autowired
    private TraineeDao traineeDao;
    @Autowired
    private TrainerDao trainerDao;
    @Autowired
    private TrainingTypeDao trainingTypeDao;

    @BeforeEach
    void setUpData() {
        TraineeEntity john = persistTrainee("John", "Smith");
        TraineeEntity emily = persistTrainee("Emily", "Brown");
        TrainerEntity anna = persistTrainer("Anna", "Lee", "Yoga");
        TrainerEntity mark = persistTrainer("Mark", "Taylor", "Resistance");

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
        Trainee john = traineeDao.findByUsername("John.Smith").orElseThrow();
        Trainer anna = trainerDao.findByUsername("Anna.Lee").orElseThrow();

        Training saved = trainingDao.save(new Training(
                john.toParticipant(),
                anna.toParticipant(),
                "New one",
                trainingTypeDao.findByName("Fitness").orElseThrow(),
                MARCH_1,
                45));
        flushAndClear();

        assertEquals(45, entityManager.find(TrainingEntity.class, saved.getId()).getDuration());
    }

    @Test
    void findByTrainee_withoutCriteria_returnsOnlyThatTraineesTrainingsOrderedByDate() {
        List<Training> trainings = trainingDao.findByTrainee("John.Smith", TraineeTrainingCriteria.none());

        assertEquals(List.of("Heavy lifting", "Yoga basics", "Advanced yoga"), names(trainings));
    }

    @Test
    void findByTrainee_buildsParticipantsAndType() {
        Training first = trainingDao.findByTrainee("John.Smith", TraineeTrainingCriteria.none()).get(0);

        assertEquals("John.Smith", first.getTrainee().username());
        assertEquals("Mark", first.getTrainer().firstName());
        assertEquals("Taylor", first.getTrainer().lastName());
        assertEquals("Resistance", first.getTrainingType().getName());
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

    /** With different trainers every training would otherwise load its trainer and user separately. */
    @Test
    void findByTrainee_sendsOneStatement_withDifferentTrainers() {
        TraineeEntity trainee = persistTrainee("Many", "Trainers");
        for (int i = 0; i < 5; i++) {
            persistTraining(trainee, persistTrainer("Trainer", "Number" + i, "Yoga"), "Training " + i, "Yoga", MARCH_1);
        }
        flushAndClear();

        long statements = countStatements(() -> trainingDao.findByTrainee("Many.Trainers", TraineeTrainingCriteria.none()));

        assertEquals(1, statements);
    }

    @Test
    void findByTrainer_sendsOneStatement_withDifferentTrainees() {
        TrainerEntity trainer = persistTrainer("Many", "Trainees", "Yoga");
        for (int i = 0; i < 5; i++) {
            persistTraining(persistTrainee("Trainee", "Number" + i), trainer, "Training " + i, "Yoga", MARCH_1);
        }
        flushAndClear();

        long statements = countStatements(() -> trainingDao.findByTrainer("Many.Trainees", TrainerTrainingCriteria.none()));

        assertEquals(1, statements);
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
