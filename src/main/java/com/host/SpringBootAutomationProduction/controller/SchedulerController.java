package com.host.SpringBootAutomationProduction.controller;

import com.host.SpringBootAutomationProduction.dto.PlanDTO;
import com.host.SpringBootAutomationProduction.model.postgres.Plan;
import com.host.SpringBootAutomationProduction.service.SchedulerService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {

    private final SchedulerService schedulerService;

    @Autowired
    public SchedulerController(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }


    @GetMapping("/{planId}")
    public ResponseEntity<?> getPlan(@PathVariable("planId") String planId) {
        Plan plan = schedulerService.findPlanByPlanId(planId);

        return ResponseEntity.ok(plan);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPlan(@RequestBody @Valid PlanDTO planDTO, BindingResult bindingResult) {

        Plan plan = convertToPlan(planDTO);

        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMessage.append(error.getField())
                        .append(" - ")
                        .append(error.getDefaultMessage())
                        .append(";");
            }

            return ResponseEntity.badRequest().body(errorMessage.toString());
        }


        schedulerService.savePlan(plan);


        return ResponseEntity.status(HttpStatus.CREATED).body(String.format("Plan %s added successfully", plan.getPlanId()));
    }

    @GetMapping("/plansId")
    public ResponseEntity<?> getAllPlansId() {
        return ResponseEntity.ok(schedulerService.findAllPlansId());
    }

    private Plan convertToPlan(PlanDTO planDTO) {
        return new ModelMapper().map(planDTO, Plan.class);
    }

}
