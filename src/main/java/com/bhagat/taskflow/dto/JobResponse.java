package com.bhagat.taskflow.dto;

import com.bhagat.taskflow.entity.JobPriority;
import com.bhagat.taskflow.entity.JobStatus;

import java.time.Instant;
import java.util.UUID;

public record JobResponse(

        UUID id,
        String jobType,
        String payload,
        JobPriority priority,
        JobStatus status,
        Integer attemptCount,
        String errorMessage,
        Instant createdAt,
        Instant startedAt,
        Instant completedAt

) {
}