package com.bhagat.taskflow.dto;

import com.bhagat.taskflow.entity.JobPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CreateJobRequest(

        @NotBlank(message = "Job type is required")
        String jobType,

        @NotNull(message = "Payload is required")
        Map<String, Object> payload,

        JobPriority priority

) {
}
