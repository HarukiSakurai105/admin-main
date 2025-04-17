package com.laptopshop.admin.controller;

import com.laptopshop.admin.model.Admin;
import com.laptopshop.admin.model.Brand;
import com.laptopshop.admin.model.Laptop;
import com.laptopshop.admin.model.Log;
import com.laptopshop.admin.repository.AdminRepository;
import com.laptopshop.admin.repository.BrandRepository;
import com.laptopshop.admin.repository.LaptopRepository;
import com.laptopshop.admin.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
public class WebController {

    @Autowired
    private LaptopRepository laptopRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private LogRepository logRepository;

    @ControllerAdvice
    public class GlobalModelAttributeAdvice {

        @ModelAttribute
        public void addGlobalAttributes(Model model, HttpServletRequest request) {
            model.addAttribute("requestURI", request.getRequestURI());
        }
    }

    @GetMapping("/")
    public String home(Model model) {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpServletRequest request) {
        // Check if the user is authenticated
        if (request.getSession().getAttribute("admin") == null) {
            return "redirect:/login"; // Redirect to login if not authenticated
        }
        long laptopCount = laptopRepository.count();
        long brandCount = brandRepository.count();
        List<Log> recentLogs = logRepository.findTop10ByOrderByTimeDesc();

        model.addAttribute("laptopCount", laptopCount);
        model.addAttribute("brandCount", brandCount);
        model.addAttribute("recentLogs", recentLogs);
        return "dashboard";
    }

    @GetMapping("/laptops")
    public String laptopList(Model model) {
        List<Laptop> laptops = laptopRepository.findAll();
        model.addAttribute("laptops", laptops);
        return "laptops/list";
    }

    @GetMapping("/laptops/view/{id}")
    public String viewLaptop(@PathVariable("id") Long id, Model model) {
        logger.info("Attempting to view laptop with ID: {}", id);
        Optional<Laptop> laptopOpt = laptopRepository.findById(id);

        if (laptopOpt.isEmpty()) {
            logger.warn("Laptop with ID {} not found. Redirecting to laptops list.", id);
            return "redirect:/laptops";
        }

        Laptop laptop = laptopOpt.get();
        model.addAttribute("laptop", laptop);
        logger.info("Successfully retrieved laptop: {}", laptop.getName());
        return "laptops/view";
    }

    @GetMapping("/brands")
    public String brandList(Model model) {
        List<Brand> brands = brandRepository.findAll();
        model.addAttribute("brands", brands);
        return "brands/list";
    }

    @GetMapping("/logs")
    public String logList(Model model, @RequestParam(required = false) String action) {
        List<Log> logs;
        if (action != null && !action.isEmpty()) {
            logs = logRepository.findByAction(action);
        } else {
            logs = logRepository.findAll();
        }
        model.addAttribute("logs", logs);
        return "logs/list";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username, @RequestParam String password, Model model,
            HttpServletRequest request) {
        // Tìm admin theo username
        Optional<Admin> adminOpt = adminRepository.findByUsername(username);

        // Kiểm tra mật khẩu (trong thực tế sẽ sử dụng mã hóa)
        if (adminOpt.isPresent() && adminOpt.get().getPasswordHash().equals(password)) {
            // Đăng nhập thành công, lưu thông tin admin vào session
            request.getSession().setAttribute("admin", adminOpt.get());
            // Ghi log đăng nhập thành công
            Log loginLog = new Log();
            loginLog.setAction("LOGIN");
            loginLog.setDetails("Admin " + username + " đã đăng nhập");
            logRepository.save(loginLog);
            // Chuyển hướng đến trang dashboard
            return "redirect:/dashboard";
        } else {
            // Đăng nhập thất bại, quay lại trang login với thông báo lỗi
            return "redirect:/login?error";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        // Lấy thông tin admin từ session để ghi log
        Admin admin = (Admin) request.getSession().getAttribute("admin");
        if (admin != null) {
            // Ghi log đăng xuất
            Log logoutLog = new Log();
            logoutLog.setAction("LOGOUT");
            logoutLog.setDetails("Admin " + admin.getUsername() + " đã đăng xuất");
            logRepository.save(logoutLog);
        }
        // Xóa session
        request.getSession().invalidate();
        return "redirect:/login";
    }

    @GetMapping("/account")
    public String accountInfo(Model model) {
        // Trong thực tế sẽ lấy thông tin từ session hoặc authentication
        Admin admin = adminRepository.findById(1).orElse(new Admin());
        model.addAttribute("admin", admin);
        return "account";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        // Thêm các thuộc tính cài đặt vào model nếu cần
        return "settings";
    }

    @GetMapping("/brands/add")
    public String showAddBrandForm(Model model) {
        model.addAttribute("brand", new Brand());
        return "brands/add";
    }

    @PostMapping("/brands/add")
    public String addBrand(@Valid @ModelAttribute("brand") Brand brand, BindingResult result) {
        if (result.hasErrors()) {
            return "brands/add";
        }
        if (brandRepository.existsByNameIgnoreCase(brand.getName())) {
            result.rejectValue("name", "error.name", "Tên hãng đã tồn tại");
            return "brands/add";
        }
        brandRepository.save(brand);
        return "redirect:/brands";
    }

    @GetMapping("/laptops/add")
    public String showAddLaptopForm(Model model) {
        model.addAttribute("laptop", new Laptop());
        model.addAttribute("brands", brandRepository.findAll());
        return "laptops/add";
    }

    @PostMapping("/laptops/add")
    public String addLaptop(@Valid @ModelAttribute("laptop") Laptop laptop, BindingResult result, Model model,
            HttpServletRequest request) {
        if (result.hasErrors()) {
            model.addAttribute("brands", brandRepository.findAll());
            return "laptops/add";
        }
        // Cập nhật thời gian tạo và cập nhật
        laptop.setCreatedAt(LocalDateTime.now());
        laptop.setUpdatedAt(LocalDateTime.now());
        laptopRepository.save(laptop);

        // Ghi log thêm laptop
        Admin admin = (Admin) request.getSession().getAttribute("admin");
        if (admin != null) {
            Log log = new Log();
            log.setAction("ADD_LAPTOP");
            log.setDetails("Admin " + admin.getUsername() + " đã thêm laptop mới: " + laptop.getName());
            logRepository.save(log);
        }

        return "redirect:/laptops";
    }

    @GetMapping("/laptops/edit/{id}")
    public String showEditLaptopForm(@org.springframework.web.bind.annotation.PathVariable("id") Long id, Model model) {
        Optional<Laptop> laptopOpt = laptopRepository.findById(id.intValue());
        if (laptopOpt.isEmpty()) {
            return "redirect:/laptops";
        }
        model.addAttribute("laptop", laptopOpt.get());
        model.addAttribute("brands", brandRepository.findAll());
        return "laptops/edit";
    }

    @PostMapping("/laptops/edit/{id}")
    public String updateLaptop(@PathVariable("id") Long id, @Valid @ModelAttribute("laptop") Laptop laptop,
            BindingResult result, Model model, HttpServletRequest request) {
        if (result.hasErrors()) {
            model.addAttribute("brands", brandRepository.findAll());
            return "laptops/edit";
        }
        // Cập nhật thời gian cập nhật
        laptop.setUpdatedAt(LocalDateTime.now());
        laptopRepository.save(laptop);

        // Ghi log cập nhật laptop
        Admin admin = (Admin) request.getSession().getAttribute("admin");
        if (admin != null) {
            Log log = new Log();
            log.setAction("UPDATE_LAPTOP");
            log.setDetails("Admin " + admin.getUsername() + " đã cập nhật laptop: " + laptop.getName());
            logRepository.save(log);
        }

        return "redirect:/laptops";
    }
}