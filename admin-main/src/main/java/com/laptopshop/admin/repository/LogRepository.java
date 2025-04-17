package com.laptopshop.admin.repository;

import com.laptopshop.admin.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, Integer> {
    // Tìm kiếm log theo action
    List<Log> findByAction(String action);
    
    // Tìm kiếm log theo admin id
    List<Log> findByAdminId(Integer adminId);
    
    // Tìm kiếm log theo khoảng thời gian
    List<Log> findByTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // Tìm kiếm log theo action và admin id
    List<Log> findByActionAndAdminId(String action, Integer adminId);
    
    // Tìm kiếm log theo nội dung chi tiết
    @Query("SELECT l FROM Log l WHERE l.details LIKE %:keyword%")
    List<Log> findByDetailsContaining(String keyword);
    
    // Lấy các log mới nhất
    List<Log> findTop10ByOrderByTimeDesc();
}