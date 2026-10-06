package com.bhagat.taskflow.service;

import com.bhagat.taskflow.entity.Job;
import com.bhagat.taskflow.entity.JobStatus;
import com.bhagat.taskflow.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class JobLifecycleService {

    private final JobRepository jobRepository;

    public JobLifecycleService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Job markQueued(UUID jobId) {
        Job job = find(jobId);
        if (job.getStatus() != JobStatus.PENDING) {
            throw new IllegalStateException("Only pending jobs can be queued");
        }
        job.setStatus(JobStatus.QUEUED);
        return jobRepository.save(job);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markQueueFailure(UUID jobId, String errorMessage) {
        Job job = find(jobId);
        if (job.getStatus() == JobStatus.QUEUED) {
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(errorMessage);
            job.setCompletedAt(Instant.now());
            jobRepository.save(job);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Job start(UUID jobId) {
        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null || job.getStatus() != JobStatus.QUEUED) {
            return null;
        }
        job.setStatus(JobStatus.RUNNING);
        job.setStartedAt(Instant.now());
        job.setAttemptCount(job.getAttemptCount() + 1);
        return jobRepository.save(job);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(UUID jobId) {
        Job job = find(jobId);
        if (job.getStatus() == JobStatus.RUNNING) {
            job.setStatus(JobStatus.COMPLETED);
            job.setCompletedAt(Instant.now());
            jobRepository.save(job);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(UUID jobId, String message) {
        Job job = find(jobId);
        if (job.getStatus() == JobStatus.RUNNING) {
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(message == null ? "Job processing failed" : message);
            job.setCompletedAt(Instant.now());
            jobRepository.save(job);
        }
    }

    private Job find(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalStateException("Job not found: " + jobId));
    }
}
