package com.prevengos.plug.android.data.remote.model;

import com.squareup.moshi.Json;

public class AsyncJobResponse {
    @Json(name = "job_id")
    private final String jobId;
    private final String status;
    @Json(name = "trace_id")
    private final String traceId;
    @Json(name = "enqueued_at")
    private final String enqueuedAt;

    public AsyncJobResponse(String jobId, String status, String traceId, String enqueuedAt) {
        this.jobId = jobId;
        this.status = status;
        this.traceId = traceId;
        this.enqueuedAt = enqueuedAt;
    }

    public String getJobId() {
        return jobId;
    }

    public String getStatus() {
        return status;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getEnqueuedAt() {
        return enqueuedAt;
    }
}
