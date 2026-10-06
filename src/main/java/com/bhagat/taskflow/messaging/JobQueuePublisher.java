package com.bhagat.taskflow.messaging;

import com.bhagat.taskflow.service.JobCreatedEvent;
import com.bhagat.taskflow.service.JobLifecycleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.concurrent.TimeUnit;

@Component
public class JobQueuePublisher {

    private static final Logger log = LoggerFactory.getLogger(JobQueuePublisher.class);

    private final JobLifecycleService jobLifecycleService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public JobQueuePublisher(
            JobLifecycleService jobLifecycleService,
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${taskflow.kafka.topic}") String topic
    ) {
        this.jobLifecycleService = jobLifecycleService;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(JobCreatedEvent event) {
        var job = jobLifecycleService.markQueued(event.jobId());
        try {
            kafkaTemplate.send(topic, job.getId().toString(), job.getId().toString())
                    .get(10, TimeUnit.SECONDS);
            log.info("JOB_QUEUED jobId={} jobType={}", job.getId(), job.getJobType());
        } catch (Exception exception) {
            jobLifecycleService.markQueueFailure(event.jobId(),
                    "Failed to publish job to Kafka: " + exception.getMessage());
            throw new IllegalStateException("Could not queue job " + event.jobId(), exception);
        }
    }

}
