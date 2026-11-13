package com.quickkart.dto;

public class DeliveryDashboardResponse {

    private Long partnerId;
    private int totalDeliveries;
    private int activeDeliveries;
    private int completedDeliveries;
    private boolean available;

    public DeliveryDashboardResponse(
            Long partnerId,
            int totalDeliveries,
            int activeDeliveries,
            int completedDeliveries,
            boolean available) {

        this.partnerId = partnerId;
        this.totalDeliveries = totalDeliveries;
        this.activeDeliveries = activeDeliveries;
        this.completedDeliveries = completedDeliveries;
        this.available = available;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public int getTotalDeliveries() {
        return totalDeliveries;
    }

    public int getActiveDeliveries() {
        return activeDeliveries;
    }

    public int getCompletedDeliveries() {
        return completedDeliveries;
    }

    public boolean isAvailable() {
        return available;
    }
}