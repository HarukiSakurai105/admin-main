package com.laptopshop.admin.controller;

import com.laptopshop.admin.model.Admin;
import com.laptopshop.admin.model.Brand;
import com.laptopshop.admin.model.Laptop;
import com.laptopshop.admin.model.Log;
import com.laptopshop.admin.repository.AdminRepository;
import com.laptopshop.admin.repository.BrandRepository;
import com.laptopshop.admin.repository.LaptopRepository;
import com.laptopshop.admin.repository.LogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

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
        Optional<Laptop> laptopOpt = laptopRepository.findById(id.intValue());

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
    public String accountInfo(Model model, HttpServletRequest request) {
        // Lấy thông tin admin từ session
        Admin admin = (Admin) request.getSession().getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }
        model.addAttribute("admin", admin);
        return "account";
    }
    
    @GetMapping("/account/edit")
    public String showEditAccountForm(Model model, HttpServletRequest request) {
        // Lấy thông tin admin từ session
        Admin admin = (Admin) request.getSession().getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }
        model.addAttribute("admin", admin);
        return "account-edit";
    }
    
    @PostMapping("/account/edit")
    public String updateAccount(@Valid @ModelAttribute("admin") Admin updatedAdmin, 
                              BindingResult result, 
                              HttpServletRequest request) {
        if (result.hasErrors()) {
            return "account-edit";
        }
        
        // Lấy thông tin admin hiện tại từ session
        Admin currentAdmin = (Admin) request.getSession().getAttribute("admin");
        if (currentAdmin == null) {
            return "redirect:/login";
        }
        
        // Cập nhật thông tin admin
        currentAdmin.setEmail(updatedAdmin.getEmail());
        // Không cập nhật mật khẩu ở đây, sẽ có form riêng để đổi mật khẩu
        
        // Lưu thông tin đã cập nhật
        adminRepository.save(currentAdmin);
        
        // Cập nhật lại thông tin trong session
        request.getSession().setAttribute("admin", currentAdmin);
        
        // Ghi log
        Log log = new Log();
        log.setAction("UPDATE_ACCOUNT");
        log.setAdmin(currentAdmin);
        log.setDetails("Admin " + currentAdmin.getUsername() + " đã cập nhật thông tin tài khoản");
        logRepository.save(log);
        
        return "redirect:/account?success";
    }

    @GetMapping("/settings")
    public String settings(Model model, HttpServletRequest request) {
        // Kiểm tra quyền truy cập
        Admin admin = (Admin) request.getSession().getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }
        
        // Chỉ admin mới có quyền truy cập trang cài đặt
        if (!"ADMIN".equals(admin.getRole())) {
            return "redirect:/dashboard?error=access_denied";
        }
        
        // Thêm các thuộc tính cài đặt vào model nếu cần
        return "settings";
    }
    
    @GetMapping("/admins")
    public String adminList(Model model, HttpServletRequest request) {
        // Kiểm tra quyền truy cập
        Admin admin = (Admin) request.getSession().getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }
        
        // Chỉ admin mới có quyền truy cập trang quản lý tài khoản
        if (!"ADMIN".equals(admin.getRole())) {
            return "redirect:/dashboard?error=access_denied";
        }
        
        List<Admin> admins = adminRepository.findAll();
        model.addAttribute("admins", admins);
        return "admins/list";
    }
    
    @GetMapping("/admins/add")
    public String showAddAdminForm(Model model, HttpServletRequest request) {
        // Kiểm tra quyền truy cập
        Admin admin = (Admin) request.getSession().getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }
        
        // Chỉ admin mới có quyền thêm tài khoản mới
        if (!"ADMIN".equals(admin.getRole())) {
            return "redirect:/dashboard?error=access_denied";
        }
        
        model.addAttribute("admin", new Admin());
        return "admins/add";
    }
    
    @PostMapping("/admins/add")
    public String addAdmin(@Valid @ModelAttribute("admin") Admin newAdmin, 
                          BindingResult result, 
                          HttpServletRequest request) {
        // Kiểm tra quyền truy cập
        Admin currentAdmin = (Admin) request.getSession().getAttribute("admin");
        if (currentAdmin == null) {
            return "redirect:/login";
        }
        
        // Chỉ admin mới có quyền thêm tài khoản mới
        if (!"ADMIN".equals(currentAdmin.getRole())) {
            return "redirect:/dashboard?error=access_denied";
        }
        
        if (result.hasErrors()) {
            return "admins/add";
        }
        
        // Kiểm tra username đã tồn tại chưa
        if (adminRepository.existsByUsername(newAdmin.getUsername())) {
            result.rejectValue("username", "error.username", "Tên đăng nhập đã tồn tại");
            return "admins/add";
        }
        
        // Lưu tài khoản mới
        adminRepository.save(newAdmin);
        
        // Ghi log
        Log log = new Log();
        log.setAction("ADD_ADMIN");
        log.setAdmin(currentAdmin);
        log.setDetails("Admin " + currentAdmin.getUsername() + " đã thêm tài khoản mới: " + newAdmin.getUsername());
        logRepository.save(log);
        
        return "redirect:/admins";
    }
    
    @GetMapping("/admins/edit/{id}")
    public String showEditAdminForm(@PathVariable("id") Integer id, 
                                  Model model, 
                                  HttpServletRequest request) {
        // Kiểm tra quyền truy cập
        Admin currentAdmin = (Admin) request.getSession().getAttribute("admin");
        if (currentAdmin == null) {
            return "redirect:/login";
        }
        
        // Chỉ admin mới có quyền chỉnh sửa tài khoản
        if (!"ADMIN".equals(currentAdmin.getRole())) {
            return "redirect:/dashboard?error=access_denied";
        }
        
        Optional<Admin> adminOpt = adminRepository.findById(id);
        if (adminOpt.isEmpty()) {
            return "redirect:/admins";
        }
        
        model.addAttribute("admin", adminOpt.get());
        return "admins/edit";
    }
    
    @PostMapping("/admins/edit/{id}")
    public String updateAdmin(@PathVariable("id") Integer id, 
                            @Valid @ModelAttribute("admin") Admin updatedAdmin, 
                            BindingResult result, 
                            HttpServletRequest request) {
        // Kiểm tra quyền truy cập
        Admin currentAdmin = (Admin) request.getSession().getAttribute("admin");
        if (currentAdmin == null) {
            return "redirect:/login";
        }
        
        // Chỉ admin mới có quyền chỉnh sửa tài khoản
        if (!"ADMIN".equals(currentAdmin.getRole())) {
            return "redirect:/dashboard?error=access_denied";
        }
        
        if (result.hasErrors()) {
            return "admins/edit";
        }
        
        Optional<Admin> adminOpt = adminRepository.findById(id);
        if (adminOpt.isEmpty()) {
            return "redirect:/admins";
        }
        
        Admin existingAdmin = adminOpt.get();
        
        // Cập nhật thông tin
        existingAdmin.setEmail(updatedAdmin.getEmail());
        existingAdmin.setRole(updatedAdmin.getRole());
        
        // Nếu mật khẩu được cung cấp, cập nhật mật khẩu
        if (updatedAdmin.getPasswordHash() != null && !updatedAdmin.getPasswordHash().isEmpty()) {
            existingAdmin.setPasswordHash(updatedAdmin.getPasswordHash());
        }
        
        // Lưu thông tin đã cập nhật
        adminRepository.save(existingAdmin);
        
        // Ghi log
        Log log = new Log();
        log.setAction("UPDATE_ADMIN");
        log.setAdmin(currentAdmin);
        log.setDetails("Admin " + currentAdmin.getUsername() + " đã cập nhật tài khoản: " + existingAdmin.getUsername());
        logRepository.save(log);
        
        return "redirect:/admins";
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
    public String showEditLaptopForm(@PathVariable("id") Long id, Model model) {
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
    
    @GetMapping("/laptops/delete/{id}")
    public String showDeleteLaptopConfirmation(@PathVariable("id") Long id, Model model) {
        Optional<Laptop> laptopOpt = laptopRepository.findById(id.intValue());
        if (laptopOpt.isEmpty()) {
            return "redirect:/laptops";
        }
        model.addAttribute("laptop", laptopOpt.get());
        return "laptops/delete";
    }
    
    @PostMapping("/laptops/delete/{id}")
    public String deleteLaptop(@PathVariable("id") Long id, HttpServletRequest request) {
        Optional<Laptop> laptopOpt = laptopRepository.findById(id.intValue());
        if (laptopOpt.isPresent()) {
            Laptop laptop = laptopOpt.get();
            String laptopName = laptop.getName();
            
            // Xóa laptop
            laptopRepository.delete(laptop);
            
            // Ghi log xóa laptop
            Admin admin = (Admin) request.getSession().getAttribute("admin");
            if (admin != null) {
                Log log = new Log();
                log.setAction("DELETE_LAPTOP");
                log.setDetails("Admin " + admin.getUsername() + " đã xóa laptop: " + laptopName);
                logRepository.save(log);
            }
        }
        return "redirect:/laptops";
    }
}