package com.host.SpringBootAutomationProduction.repositories;

import com.host.SpringBootAutomationProduction.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchedulerRepository extends JpaRepository<Plan, Integer> {

    Optional<Plan> findByPlanId(String planId);

}
