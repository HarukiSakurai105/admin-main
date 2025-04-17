package com.laptopshop.admin.repository;

import com.laptopshop.admin.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
    // Tìm kiếm admin theo username
    Optional<Admin> findByUsername(String username);
    
    // Kiểm tra xem username đã tồn tại chưa
    boolean existsByUsername(String username);
}