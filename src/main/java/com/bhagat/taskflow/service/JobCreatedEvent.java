package com.bhagat.taskflow.service;

import java.util.UUID;

public record JobCreatedEvent(UUID jobId) {
}
