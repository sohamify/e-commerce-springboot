package com.example.ecommerce.admin;

import com.example.ecommerce.admin.dto.AdminUserDetailResponse;
import com.example.ecommerce.admin.dto.AdminUserSummaryResponse;
import java.util.List;
import java.util.UUID;

public interface AdminUserService {

    List<AdminUserSummaryResponse> search(String q);

    AdminUserDetailResponse getDetail(UUID userId);

    void suspend(UUID userId);

    void ban(UUID userId);

    void reactivate(UUID userId);
}
