package com.laptopshop.admin.dto;

import java.math.BigDecimal;

/**
 * DTO đại diện cho dữ liệu từ view vw_laptop_statistics
 */
public class LaptopStatisticDTO {
    private String brandName;
    private Long laptopCount;
    private BigDecimal averagePrice;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Long totalStock;

    // Constructors
    public LaptopStatisticDTO() {
    }

    public LaptopStatisticDTO(String brandName, Long laptopCount, BigDecimal averagePrice,
                             BigDecimal minPrice, BigDecimal maxPrice, Long totalStock) {
        this.brandName = brandName;
        this.laptopCount = laptopCount;
        this.averagePrice = averagePrice;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.totalStock = totalStock;
    }

    // Getters and Setters
    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public Long getLaptopCount() {
        return laptopCount;
    }

    public void setLaptopCount(Long laptopCount) {
        this.laptopCount = laptopCount;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Long getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(Long totalStock) {
        this.totalStock = totalStock;
    }

    @Override
    public String toString() {
        return "LaptopStatisticDTO{" +
                "brandName='" + brandName + '\'' +
                ", laptopCount=" + laptopCount +
                ", averagePrice=" + averagePrice +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
                ", totalStock=" + totalStock +
                '}';
    }
}