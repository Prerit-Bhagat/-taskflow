package com.bhagat.taskflow.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.bhagat.taskflow.dto.CreateJobRequest;
import com.bhagat.taskflow.dto.JobResponse;
import com.bhagat.taskflow.entity.Job;
import com.bhagat.taskflow.entity.JobPriority;
import com.bhagat.taskflow.entity.JobStatus;
import com.bhagat.taskflow.exception.JobNotFoundException;
import com.bhagat.taskflow.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final ObjectMapper objectMapper;

    public JobService(
            JobRepository jobRepository,
            ObjectMapper objectMapper
    ) {
        this.jobRepository = jobRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public JobResponse createJob(CreateJobRequest request) {

        String payload;

        try {
            payload = objectMapper.writeValueAsString(request.payload());
        } catch (JacksonException e) {
            throw new IllegalArgumentException(
                    "Invalid job payload"
            );
        }

        Job job = new Job();

        job.setJobType(request.jobType());
        job.setPayload(payload);

        job.setPriority(
                request.priority() != null
                        ? request.priority()
                        : JobPriority.MEDIUM
        );

        job.setStatus(JobStatus.PENDING);
        job.setAttemptCount(0);
        job.setCreatedAt(Instant.now());

        Job savedJob = jobRepository.save(job);

        return toResponse(savedJob);
    }

    @Transactional(readOnly = true)
    public JobResponse getJob(UUID id) {

        Job job = jobRepository.findById(id)
                .orElseThrow(
                        () -> new JobNotFoundException(id)
                );

        return toResponse(job);
    }

    @Transactional(readOnly = true)
    public List<JobResponse> getAllJobs() {

        return jobRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public JobResponse cancelJob(UUID id) {

        Job job = jobRepository.findById(id)
                .orElseThrow(
                        () -> new JobNotFoundException(id)
                );

        if (job.getStatus() != JobStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending jobs can be cancelled"
            );
        }

        job.setStatus(JobStatus.CANCELLED);

        return toResponse(jobRepository.save(job));
    }

    private JobResponse toResponse(Job job) {

        return new JobResponse(
                job.getId(),
                job.getJobType(),
                job.getPayload(),
                job.getPriority(),
                job.getStatus(),
                job.getAttemptCount(),
                job.getErrorMessage(),
                job.getCreatedAt(),
                job.getStartedAt(),
                job.getCompletedAt()
        );
    }
}