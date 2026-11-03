package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.dto.TrainerProfile;
import com.epam.gymcrm.dto.request.RegisterTraineeRequest;
import com.epam.gymcrm.dto.request.RegisterTrainerRequest;
import com.epam.gymcrm.dto.response.RegistrationResponse;
import com.epam.gymcrm.dto.response.TraineeProfileResponse;
import com.epam.gymcrm.dto.response.TraineeSummary;
import com.epam.gymcrm.dto.response.TraineeTrainingResponse;
import com.epam.gymcrm.dto.response.TrainerProfileResponse;
import com.epam.gymcrm.dto.response.TrainerSummary;
import com.epam.gymcrm.dto.response.TrainerTrainingResponse;
import com.epam.gymcrm.dto.response.TrainingTypeResponse;
import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.Training;
import com.epam.gymcrm.model.TrainingType;
import com.epam.gymcrm.model.User;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * The only place where requests become entities and entities become responses.
 * The password is only exposed by {@link #toRegistration}.
 */
@Component
public class GymCrmMapper {


    /** Builds a new trainee and its user; the generated username and password come in as credentials. */
    public Trainee toTrainee(RegisterTraineeRequest request, Credentials generated) {
        User user = toUser(
                request.firstName(),
                request.lastName(),
                generated
        );
        return new Trainee(
                user,
                request.dateOfBirth(),
                request.address()
        );
    }

    /**
     * Builds a new trainer and its user. The specialization is looked up by the service
     * (the request only has its id); the generated username and password come in as credentials.
     */
    public Trainer toTrainer(
            RegisterTrainerRequest request,
            TrainingType specialization,
            Credentials generated
    ) {
        User user = toUser(
                request.firstName(),
                request.lastName(),
                generated
        );
        return new Trainer(user, specialization);
    }

    public RegistrationResponse toRegistration(User user) {
        return new RegistrationResponse(
                user.getUsername(),
                user.getPassword()
        );
    }

    public TrainingTypeResponse toTrainingTypeResponse(TrainingType type) {
        return new TrainingTypeResponse(
                type.getId(),
                type.getName()
        );
    }

    public List<TrainingTypeResponse> toTrainingTypeResponses(Collection<TrainingType> types) {
        return types.stream()
                .map(this::toTrainingTypeResponse)
                .toList();
    }

    public TrainerSummary toTrainerSummary(Trainer trainer) {
        User user = trainer.getUser();
        return new TrainerSummary(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                toTrainingTypeResponse(trainer.getSpecialization())
        );
    }

    public List<TrainerSummary> toTrainerSummaries(Collection<Trainer> trainers) {
        return trainers.stream()
                .sorted(Comparator.comparing(trainer -> trainer.getUser().getUsername()))
                .map(this::toTrainerSummary)
                .toList();
    }

    public TraineeSummary toTraineeSummary(Trainee trainee) {
        User user = trainee.getUser();
        return new TraineeSummary(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    public TraineeProfileResponse toTraineeProfileResponse(Trainee trainee) {
        User user = trainee.getUser();
        return new TraineeProfileResponse(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                user.isActive(),
                toTrainerSummaries(trainee.getTrainers())
        );
    }

    public TrainerProfileResponse toTrainerProfileResponse(TrainerProfile profile) {
        User user = profile.trainer().getUser();
        return new TrainerProfileResponse(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                toTrainingTypeResponse(profile.trainer().getSpecialization()),
                user.isActive(),
                profile.trainees().stream()
                        .map(this::toTraineeSummary)
                        .toList()
        );
    }

    public TraineeTrainingResponse toTraineeTrainingResponse(Training training) {
        return new TraineeTrainingResponse(
                training.getTrainingName(),
                training.getTrainingDate(),
                toTrainingTypeResponse(training.getTrainingType()),
                training.getDuration(),
                fullName(training.getTrainer().getUser())
        );
    }

    public TrainerTrainingResponse toTrainerTrainingResponse(Training training) {
        return new TrainerTrainingResponse(
                training.getTrainingName(),
                training.getTrainingDate(),
                toTrainingTypeResponse(training.getTrainingType()),
                training.getDuration(),
                fullName(training.getTrainee().getUser())
        );
    }

    private User toUser(
            String firstName,
            String lastName,
            Credentials generated
    ) {
        return new User(
                firstName.trim(),
                lastName.trim(),
                generated.username(),
                generated.password()
        );
    }

    private String fullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
