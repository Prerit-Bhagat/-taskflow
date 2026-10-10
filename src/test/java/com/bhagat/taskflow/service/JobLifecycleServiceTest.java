package com.bhagat.taskflow.service;

import com.bhagat.taskflow.entity.Job;
import com.bhagat.taskflow.entity.JobStatus;
import com.bhagat.taskflow.repository.JobRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JobLifecycleServiceTest {

    @Test
    void claimsQueuedJobAtomicallyBeforeProcessing() {
        JobRepository repository = mock(JobRepository.class);
        JobLifecycleService lifecycleService = new JobLifecycleService(repository);
        UUID jobId = UUID.randomUUID();
        Job job = new Job();
        job.setId(jobId);
        job.setStatus(JobStatus.RUNNING);

        when(repository.claimForProcessing(
                eq(jobId),
                eq(JobStatus.QUEUED),
                eq(JobStatus.RUNNING),
                any(Instant.class)
        )).thenReturn(1);
        when(repository.findById(jobId)).thenReturn(Optional.of(job));

        Job claimedJob = lifecycleService.start(jobId);

        assertSame(job, claimedJob);
        verify(repository).claimForProcessing(
                eq(jobId),
                eq(JobStatus.QUEUED),
                eq(JobStatus.RUNNING),
                any(Instant.class)
        );
    }

    @Test
    void doesNotProcessJobWhenAnotherWorkerAlreadyClaimedIt() {
        JobRepository repository = mock(JobRepository.class);
        JobLifecycleService lifecycleService = new JobLifecycleService(repository);
        UUID jobId = UUID.randomUUID();

        when(repository.claimForProcessing(
                eq(jobId),
                eq(JobStatus.QUEUED),
                eq(JobStatus.RUNNING),
                any(Instant.class)
        )).thenReturn(0);

        assertNull(lifecycleService.start(jobId));
        verify(repository, never()).findById(jobId);
    }
}
