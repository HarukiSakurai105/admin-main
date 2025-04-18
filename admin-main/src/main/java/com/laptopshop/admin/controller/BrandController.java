package com.laptopshop.admin.controller;

import com.laptopshop.admin.model.Brand;
import com.laptopshop.admin.model.Laptop;
import com.laptopshop.admin.repository.BrandRepository;
import com.laptopshop.admin.repository.LaptopRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/brands")
public class BrandController {

    private static final Logger logger = LoggerFactory.getLogger(BrandController.class);

    @Autowired
    private BrandRepository brandRepository;
    
    @Autowired
    private LaptopRepository laptopRepository;

    // Hiển thị form chỉnh sửa thương hiệu
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Thương hiệu không hợp lệ với ID: " + id));
        model.addAttribute("brand", brand);
        return "brands/edit";
    }

    // Xử lý cập nhật thương hiệu
    @PostMapping("/edit/{id}")
    public String updateBrand(@PathVariable("id") Integer id, @Valid @ModelAttribute("brand") Brand brand,
                             BindingResult result) {
        if (result.hasErrors()) {
            return "brands/edit";
        }
        
        // Kiểm tra xem tên thương hiệu đã tồn tại chưa (ngoại trừ thương hiệu hiện tại)
        Brand existingBrand = brandRepository.findByNameIgnoreCase(brand.getName());
        if (existingBrand != null && !existingBrand.getId().equals(id)) {
            result.rejectValue("name", "error.brand", "Tên thương hiệu này đã tồn tại");
            return "brands/edit";
        }
        
        brand.setId(id); // Đảm bảo ID không bị thay đổi
        brandRepository.save(brand);
        logger.info("Đã cập nhật thương hiệu: {}", brand);
        return "redirect:/brands";
    }

    // Hiển thị trang xác nhận xóa thương hiệu
    @GetMapping("/delete/{id}")
    public String showDeleteForm(@PathVariable("id") Integer id, Model model) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Thương hiệu không hợp lệ với ID: " + id));
        
        // Kiểm tra xem thương hiệu có đang được sử dụng không
        List<Laptop> laptops = laptopRepository.findByBrandId(id);
        
        model.addAttribute("brand", brand);
        model.addAttribute("laptopCount", laptops.size());
        model.addAttribute("canDelete", laptops.isEmpty());
        
        return "brands/delete";
    }

    // Xử lý xóa thương hiệu
    @PostMapping("/delete/{id}")
    public String deleteBrand(@PathVariable("id") Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Thương hiệu không hợp lệ với ID: " + id));
        
        // Kiểm tra xem thương hiệu có đang được sử dụng không
        List<Laptop> laptops = laptopRepository.findByBrandId(id);
        if (!laptops.isEmpty()) {
            return "redirect:/brands";
        }
        
        brandRepository.delete(brand);
        logger.info("Đã xóa thương hiệu: {}", brand);
        return "redirect:/brands";
    }
}