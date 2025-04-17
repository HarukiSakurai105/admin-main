package com.laptopshop.admin.repository;

import com.laptopshop.admin.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer> {
    // Tìm kiếm brand theo tên
    Brand findByNameIgnoreCase(String name);
    
    // Kiểm tra xem tên brand đã tồn tại chưa
    boolean existsByNameIgnoreCase(String name);
}