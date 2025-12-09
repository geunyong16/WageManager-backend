package com.example.wagemanager.domain.employer.service;

import com.example.wagemanager.common.exception.ErrorCode;
import com.example.wagemanager.common.exception.NotFoundException;
import com.example.wagemanager.domain.employer.entity.Employer;
import com.example.wagemanager.domain.employer.repository.EmployerRepository;
import com.example.wagemanager.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployerService {

    private final EmployerRepository employerRepository;

    @Transactional
    public Employer createEmployer(User user, String phone) {
        Employer employer = Employer.builder()
                .user(user)
                .phone(phone)
                .build();

        return employerRepository.save(employer);
    }

    public Employer getEmployerByUserId(Long userId) {
        return employerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.EMPLOYER_NOT_FOUND, "고용주 정보를 찾을 수 없습니다."));
    }
}
