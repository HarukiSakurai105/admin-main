package com.laptopshop.admin.repository;

import com.laptopshop.admin.model.Laptop;
import com.laptopshop.admin.dto.LaptopDetailDTO;
import com.laptopshop.admin.dto.LaptopStatisticDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LaptopRepository extends JpaRepository<Laptop, Integer> {
    // Tìm kiếm laptop theo tên
    List<Laptop> findByNameContainingIgnoreCase(String name);
    
    // Tìm kiếm laptop theo khoảng giá
    List<Laptop> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    // Tìm kiếm laptop theo hãng
    List<Laptop> findByBrandId(Integer brandId);
    
    // Tìm kiếm laptop theo khoảng giảm giá
    List<Laptop> findByDiscountBetween(BigDecimal minDiscount, BigDecimal maxDiscount);
    
    // Lấy dữ liệu từ view vw_laptop_details
    @Query(value = "SELECT * FROM vw_laptop_details", nativeQuery = true)
    List<Object[]> findAllLaptopDetails();
    
    // Lấy dữ liệu từ view vw_laptop_statistics
    @Query(value = "SELECT * FROM vw_laptop_statistics", nativeQuery = true)
    List<Object[]> findAllLaptopStatistics();
    
    // Gọi stored procedure thêm laptop mới
    @Procedure(name = "sp_add_laptop")
    void addLaptop(
        @Param("name") String name,
        @Param("price") BigDecimal price,
        @Param("discount") BigDecimal discount,
        @Param("brand_id") Integer brandId,
        @Param("specs") String specs,
        @Param("stock_quantity") Integer stockQuantity,
        @Param("admin_id") Integer adminId
    );
    
    // Gọi stored procedure cập nhật laptop
    @Procedure(name = "sp_update_laptop")
    void updateLaptop(
        @Param("id") Integer id,
        @Param("name") String name,
        @Param("price") BigDecimal price,
        @Param("discount") BigDecimal discount,
        @Param("brand_id") Integer brandId,
        @Param("specs") String specs,
        @Param("stock_quantity") Integer stockQuantity,
        @Param("admin_id") Integer adminId
    );
    
    // Gọi stored procedure xóa laptop
    @Procedure(name = "sp_delete_laptop")
    void deleteLaptop(
        @Param("id") Integer id,
        @Param("admin_id") Integer adminId
    );
    
    // Gọi stored procedure cập nhật giảm giá hàng loạt theo hãng
    @Procedure(name = "sp_update_discount_by_brand")
    void updateDiscountByBrand(
        @Param("brand_id") Integer brandId,
        @Param("discount") BigDecimal discount,
        @Param("admin_id") Integer adminId
    );
}