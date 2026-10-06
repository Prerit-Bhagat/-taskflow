package com.bhagat.taskflow.worker;

import com.bhagat.taskflow.entity.Job;
import com.bhagat.taskflow.service.JobLifecycleService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JobWorkerTest {

    @Test
    void consumesQueuedJobAndMarksItComplete() {
        JobLifecycleService lifecycleService = mock(JobLifecycleService.class);
        JobWorker worker = new JobWorker(lifecycleService);
        UUID id = UUID.randomUUID();
        Job job = new Job();
        job.setId(id);
        job.setJobType("REPORT");

        when(lifecycleService.start(id)).thenReturn(job);

        worker.consume(id.toString());

        verify(lifecycleService).complete(id);
    }

    @Test
    void ignoresDuplicateMessageWhenJobIsNotQueued() {
        JobLifecycleService lifecycleService = mock(JobLifecycleService.class);
        JobWorker worker = new JobWorker(lifecycleService);
        UUID id = UUID.randomUUID();
        when(lifecycleService.start(id)).thenReturn(null);

        worker.consume(id.toString());

        verify(lifecycleService, org.mockito.Mockito.never()).complete(id);
    }
}
