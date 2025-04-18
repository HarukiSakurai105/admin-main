package com.laptopshop.admin.repository;

import com.laptopshop.admin.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // Tìm kiếm user theo username
    Optional<User> findByUsername(String username);
    
    // Tìm kiếm user theo email
    Optional<User> findByEmail(String email);
    
    // Kiểm tra xem username đã tồn tại chưa
    boolean existsByUsername(String username);
    
    // Kiểm tra xem email đã tồn tại chưa
    boolean existsByEmail(String email);
    
    // Tìm kiếm user theo tên (hỗ trợ phân trang)
    Page<User> findByFullNameContaining(String fullName, Pageable pageable);
    
    // Tìm kiếm user theo vai trò (hỗ trợ phân trang)
    Page<User> findByRole(String role, Pageable pageable);
    
    // Tìm kiếm user theo trạng thái (hỗ trợ phân trang)
    Page<User> findByStatus(String status, Pageable pageable);
}