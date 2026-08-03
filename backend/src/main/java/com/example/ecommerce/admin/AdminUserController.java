package com.example.ecommerce.admin;

import com.example.ecommerce.admin.dto.AdminUserDetailResponse;
import com.example.ecommerce.admin.dto.AdminUserSummaryResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public List<AdminUserSummaryResponse> search(@RequestParam(required = false) String q) {
        return adminUserService.search(q);
    }

    @GetMapping("/{id}")
    public AdminUserDetailResponse get(@PathVariable UUID id) {
        return adminUserService.getDetail(id);
    }

    @PostMapping("/{id}/suspend")
    public void suspend(@PathVariable UUID id) {
        adminUserService.suspend(id);
    }

    @PostMapping("/{id}/ban")
    public void ban(@PathVariable UUID id) {
        adminUserService.ban(id);
    }

    @PostMapping("/{id}/reactivate")
    public void reactivate(@PathVariable UUID id) {
        adminUserService.reactivate(id);
    }
}
