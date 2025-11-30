package com.demo.loadgen.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadStats {
    private long totalRequests;
    private long successfulRequests;
    private long failedRequests;
    private long averageResponseTime;
    private double successRate;
    private Map<String, Integer> actionBreakdown;

    public LoadStats(long totalRequests, long successfulRequests, long failedRequests, 
                     long averageResponseTime, Map<String, AtomicInteger> actionCounts) {
        this.totalRequests = totalRequests;
        this.successfulRequests = successfulRequests;
        this.failedRequests = failedRequests;
        this.averageResponseTime = averageResponseTime;
        this.successRate = totalRequests > 0 ? (successfulRequests * 100.0 / totalRequests) : 0;
        
        this.actionBreakdown = new HashMap<>();
        actionCounts.forEach((key, value) -> this.actionBreakdown.put(key, value.get()));
    }

    // Getters
    public long getTotalRequests() {
        return totalRequests;
    }

    public long getSuccessfulRequests() {
        return successfulRequests;
    }

    public long getFailedRequests() {
        return failedRequests;
    }

    public long getAverageResponseTime() {
        return averageResponseTime;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public Map<String, Integer> getActionBreakdown() {
        return actionBreakdown;
    }
}


