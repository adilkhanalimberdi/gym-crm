package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainingTypeDao;
import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.model.TrainingType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Read-only view of the constant list of training types. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TrainingTypeService {

    private final TrainingTypeDao trainingTypeDao;
    private final AuthenticationService authenticationService;

    public List<TrainingType> findAll(Credentials credentials) {
        authenticationService.authenticate(credentials);
        return trainingTypeDao.findAll();
    }
}
