package com.host.SpringBootAutomationProduction.service;

import com.host.SpringBootAutomationProduction.exceptions.NotFoundException;
import com.host.SpringBootAutomationProduction.model.Plan;
import com.host.SpringBootAutomationProduction.repositories.SchedulerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional(readOnly = true)
public class SchedulerService {

    private final SchedulerRepository schedulerRepository;

    @Autowired
    public SchedulerService(SchedulerRepository schedulerRepository) {
        this.schedulerRepository = schedulerRepository;
    }


    @Transactional
    public void savePlan(Plan plan) {

        Optional<Plan> findPlan = schedulerRepository.findByPlanId(plan.getPlanId());

        if (findPlan.isPresent()) {
            Plan updPlan = findPlan.get();
            updPlan.setData(plan.getData());
            schedulerRepository.save(updPlan);
        } else {
            plan.setId(null); //временно, потом решить проблему с OptimisticLocking
            schedulerRepository.save(plan);
        }

    }

    public Plan findPlanByPlanId(String planId) {
        Optional<Plan> findPlan = schedulerRepository.findByPlanId(planId);
        return findPlan.orElseThrow(() -> new NotFoundException("Plan with ID " + planId + " not found"));
    }

    public List<String> findAllPlansId() {
        List<String> plansId = new ArrayList<>();

        try (Stream<Plan> planStream = schedulerRepository.findAll().stream()) {
            planStream.forEach(plan -> plansId.add(plan.getPlanId()));
        }

        return plansId;
    }


}
