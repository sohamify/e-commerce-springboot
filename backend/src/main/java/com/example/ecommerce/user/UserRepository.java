package com.example.ecommerce.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
        SELECT u FROM User u
        WHERE :q IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
                         OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :q, '%'))
        ORDER BY u.createdAt DESC
        """)
    List<User> search(@Param("q") String q, Pageable pageable);
}
