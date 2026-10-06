package com.bhagat.taskflow.controller;

import com.bhagat.taskflow.dto.CreateJobRequest;
import com.bhagat.taskflow.dto.JobResponse;
import com.bhagat.taskflow.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody CreateJobRequest request
    ) {

        JobResponse response =
                jobService.createJob(request);
        JobResponse currentState = jobService.getJob(response.id());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(currentState);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                jobService.getJob(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs() {

        return ResponseEntity.ok(
                jobService.getAllJobs()
        );
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<JobResponse> cancelJob(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                jobService.cancelJob(id)
        );
    }
}
