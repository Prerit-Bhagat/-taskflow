package com.bhagat.taskflow.repository;

import com.bhagat.taskflow.entity.Job;
import com.bhagat.taskflow.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    @Modifying
    @Query("""
            update Job job
               set job.status = :runningStatus,
                   job.startedAt = :startedAt,
                   job.attemptCount = job.attemptCount + 1
             where job.id = :jobId
               and job.status = :queuedStatus
            """)
    int claimForProcessing(
            @Param("jobId") UUID jobId,
            @Param("queuedStatus") JobStatus queuedStatus,
            @Param("runningStatus") JobStatus runningStatus,
            @Param("startedAt") Instant startedAt
    );
}
