package com.parse;

public class BuddyUploadStatus {
    private long jobsCount;
    private boolean isUploading;

    public long getJobsCount() {
        return jobsCount;
    }

    public void setJobsCount(long jobsCount) {
        this.jobsCount = jobsCount;
    }

    public boolean isUploading() {
        return isUploading;
    }

    public void setUploading(boolean uploading) {
        isUploading = uploading;
    }
}
