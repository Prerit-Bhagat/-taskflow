package com.bhagat.taskflow.worker;

import com.bhagat.taskflow.entity.Job;
import com.bhagat.taskflow.service.JobLifecycleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("worker")
public class JobWorker {

    private static final Logger log = LoggerFactory.getLogger(JobWorker.class);
    private final JobLifecycleService jobLifecycleService;

    public JobWorker(JobLifecycleService jobLifecycleService) {
        this.jobLifecycleService = jobLifecycleService;
    }

    @KafkaListener(topics = "${taskflow.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message) {
        UUID jobId = UUID.fromString(message);
        Job job = jobLifecycleService.start(jobId);
        if (job == null) {
            log.info("Ignoring duplicate or non-queued message jobId={}", jobId);
            return;
        }

        try {
            execute(job);
            jobLifecycleService.complete(jobId);
            log.info("JOB_COMPLETED jobId={} jobType={}", jobId, job.getJobType());
        } catch (RuntimeException exception) {
            jobLifecycleService.fail(jobId, exception.getMessage());
            log.error("JOB_FAILED jobId={} jobType={}", jobId, job.getJobType(), exception);
        }
    }

    private void execute(Job job) {
        // Phase 2 establishes the worker lifecycle; domain-specific executors are added in Phase 3.
        log.info("Executing job jobId={} jobType={}", job.getId(), job.getJobType());
    }
}
