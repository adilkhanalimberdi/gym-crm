package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDao;
import com.epam.gymcrm.dto.request.CreateTraineeRequest;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.mapper.TraineeMapper;
import com.epam.gymcrm.model.Trainee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private TraineeDao traineeDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    private TraineeMapper mapper;

    public Trainee create(CreateTraineeRequest request) {
        log.info("Creating trainee {} {}", request.firstName(), request.lastName());
        String password = passwordGenerator.generate();
        Trainee created = usernameGenerator.createWithUniqueUsername(
                request.firstName(), request.lastName(),
                username -> traineeDao.create(mapper.toEntity(request, username, password))
        );
        log.info("Created trainee {}", created);
        return created;
    }

    public Trainee update(Trainee trainee) {
        log.info("Updating trainee with id {}", trainee.id());
        if (!traineeDao.update(trainee)) {
            log.warn("Cannot update trainee: id {} not found", trainee.id());
            throw new EntityNotFoundException("Trainee", trainee.id());
        }
        return trainee;
    }

    public void delete(Long id) {
        log.info("Deleting trainee with id {}", id);
        if (!traineeDao.deleteById(id)) {
            log.warn("Cannot delete trainee: id {} not found", id);
            throw new EntityNotFoundException("Trainee", id);
        }
    }

    public Trainee findById(Long id) {
        log.debug("Selecting trainee with id {}", id);
        return traineeDao.findById(id).orElseThrow(() -> new EntityNotFoundException("Trainee", id));
    }

    public List<Trainee> findAll() {
        log.debug("Selecting all trainees");
        return traineeDao.findAll();
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

	@Autowired
	public void setTraineeMapper(TraineeMapper mapper) {
        this.mapper = mapper;
    }
}
