package com.epam.gymcrm.web.mapper;

import com.epam.gymcrm.dto.TrainerProfile;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import com.epam.gymcrm.web.dto.RegistrationResponse;
import com.epam.gymcrm.web.dto.TraineeProfileResponse;
import com.epam.gymcrm.web.dto.TraineeSummary;
import com.epam.gymcrm.web.dto.TraineeTrainingResponse;
import com.epam.gymcrm.web.dto.TrainerProfileResponse;
import com.epam.gymcrm.web.dto.TrainerSummary;
import com.epam.gymcrm.web.dto.TrainerTrainingResponse;
import com.epam.gymcrm.web.dto.TrainingTypeResponse;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** Turns entities into the objects the REST API returns; the password is only exposed by {@link #toRegistration}. */
@Component
public class ResponseMapper {

    public RegistrationResponse toRegistration(User user) {
        return new RegistrationResponse(user.getUsername(), user.getPassword());
    }

    public TrainingTypeResponse toTrainingType(TrainingType type) {
        return new TrainingTypeResponse(type.getId(), type.getName());
    }

    public List<TrainingTypeResponse> toTrainingTypes(Collection<TrainingType> types) {
        return types.stream().map(this::toTrainingType).toList();
    }

    public TrainerSummary toTrainerSummary(Trainer trainer) {
        User user = trainer.getUser();
        return new TrainerSummary(user.getUsername(), user.getFirstName(), user.getLastName(),
                toTrainingType(trainer.getSpecialization()));
    }

    public List<TrainerSummary> toTrainerSummaries(Collection<Trainer> trainers) {
        return trainers.stream()
                .sorted(Comparator.comparing(trainer -> trainer.getUser().getUsername()))
                .map(this::toTrainerSummary)
                .toList();
    }

    public TraineeSummary toTraineeSummary(Trainee trainee) {
        User user = trainee.getUser();
        return new TraineeSummary(user.getUsername(), user.getFirstName(), user.getLastName());
    }

    public TraineeProfileResponse toTraineeProfile(Trainee trainee) {
        User user = trainee.getUser();
        return new TraineeProfileResponse(user.getUsername(), user.getFirstName(), user.getLastName(),
                trainee.getDateOfBirth(), trainee.getAddress(), user.isActive(),
                toTrainerSummaries(trainee.getTrainers()));
    }

    public TrainerProfileResponse toTrainerProfile(TrainerProfile profile) {
        User user = profile.trainer().getUser();
        return new TrainerProfileResponse(user.getUsername(), user.getFirstName(), user.getLastName(),
                toTrainingType(profile.trainer().getSpecialization()), user.isActive(),
                profile.trainees().stream().map(this::toTraineeSummary).toList());
    }

    public TraineeTrainingResponse toTraineeTraining(Training training) {
        return new TraineeTrainingResponse(training.getTrainingName(), training.getTrainingDate(),
                toTrainingType(training.getTrainingType()), training.getDuration(),
                fullName(training.getTrainer().getUser()));
    }

    public TrainerTrainingResponse toTrainerTraining(Training training) {
        return new TrainerTrainingResponse(training.getTrainingName(), training.getTrainingDate(),
                toTrainingType(training.getTrainingType()), training.getDuration(),
                fullName(training.getTrainee().getUser()));
    }

    private String fullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
