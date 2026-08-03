package com.example.ecommerce.admin.dto;

public record AdminDashboardResponse(
    long flaggedListingsCount,
    long openReportsCount,
    long totalUsers,
    long totalListings,
    long salesLast7Days
) {
}
