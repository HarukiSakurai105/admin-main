package com.laptopshop.admin.controller;

import com.laptopshop.admin.dto.UserDTO;
import com.laptopshop.admin.model.Log;
import com.laptopshop.admin.model.User;
import com.laptopshop.admin.repository.LogRepository;
import com.laptopshop.admin.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LogRepository logRepository;

    // Hiển thị danh sách người dùng với phân trang
    @GetMapping
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model,
            HttpServletRequest request) {
        
        // Kiểm tra đăng nhập
        if (request.getSession().getAttribute("admin") == null) {
            return "redirect:/login";
        }

        // Tạo đối tượng Pageable cho phân trang và sắp xếp
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        // Tìm kiếm người dùng theo tên
        Page<User> userPage;
        if (search.isEmpty()) {
            userPage = userRepository.findAll(pageable);
        } else {
            userPage = userRepository.findByFullNameContaining(search, pageable);
        }

        model.addAttribute("userPage", userPage);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        return "users/list";
    }

    // Hiển thị form thêm người dùng mới
    @GetMapping("/add")
    public String showAddUserForm(Model model, HttpServletRequest request) {
        // Kiểm tra đăng nhập
        if (request.getSession().getAttribute("admin") == null) {
            return "redirect:/login";
        }

        model.addAttribute("userDTO", new UserDTO());
        return "users/add";
    }

    // Xử lý thêm người dùng mới
    @PostMapping("/add")
    public String addUser(@Valid @ModelAttribute("userDTO") UserDTO userDTO,
                          BindingResult result,
                          Model model,
                          HttpServletRequest request) {
        // Kiểm tra đăng nhập
        if (request.getSession().getAttribute("admin") == null) {
            return "redirect:/login";
        }

        // Kiểm tra lỗi validation
        if (result.hasErrors()) {
            return "users/add";
        }

        // Kiểm tra username và email đã tồn tại chưa
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            result.rejectValue("username", "error.user", "Tên đăng nhập đã tồn tại");
            return "users/add";
        }

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            result.rejectValue("email", "error.user", "Email đã tồn tại");
            return "users/add";
        }

        // Lưu người dùng mới
        User user = userDTO.toEntity();
        userRepository.save(user);

        // Ghi log
        Log log = new Log();
        log.setAction("ADD_USER");
        log.setDetails("Thêm người dùng mới: " + user.getUsername());
        log.setTime(LocalDateTime.now());
        logRepository.save(log);

        return "redirect:/users";
    }

    // Hiển thị thông tin chi tiết người dùng
    @GetMapping("/view/{id}")
    public String viewUser(@PathVariable("id") Integer id, Model model, HttpServletRequest request) {
        // Kiểm tra đăng nhập
        if (request.getSession().getAttribute("admin") == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return "redirect:/users";
        }

        model.addAttribute("user", userOpt.get());
        return "users/view";
    }

    // Hiển thị form chỉnh sửa thông tin người dùng
    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Integer id, Model model, HttpServletRequest request) {
        // Kiểm tra đăng nhập
        if (request.getSession().getAttribute("admin") == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return "redirect:/users";
        }

        UserDTO userDTO = new UserDTO(userOpt.get());
        model.addAttribute("userDTO", userDTO);
        return "users/edit";
    }

    // Xử lý cập nhật thông tin người dùng
    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable("id") Integer id,
                            @Valid @ModelAttribute("userDTO") UserDTO userDTO,
                            BindingResult result,
                            Model model,
                            HttpServletRequest request) {
        // Kiểm tra đăng nhập
        if (request.getSession().getAttribute("admin") == null) {
            return "redirect:/login";
        }

        // Kiểm tra lỗi validation
        if (result.hasErrors()) {
            return "users/edit";
        }

        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return "redirect:/users";
        }

        User existingUser = userOpt.get();

        // Kiểm tra username và email đã tồn tại chưa (nếu thay đổi)
        if (!existingUser.getUsername().equals(userDTO.getUsername()) && 
            userRepository.existsByUsername(userDTO.getUsername())) {
            result.rejectValue("username", "error.user", "Tên đăng nhập đã tồn tại");
            return "users/edit";
        }

        if (!existingUser.getEmail().equals(userDTO.getEmail()) && 
            userRepository.existsByEmail(userDTO.getEmail())) {
            result.rejectValue("email", "error.user", "Email đã tồn tại");
            return "users/edit";
        }

        // Cập nhật thông tin người dùng
        existingUser.setFullName(userDTO.getFullName());
        existingUser.setUsername(userDTO.getUsername());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        existingUser.setAddress(userDTO.getAddress());
        existingUser.setRole(userDTO.getRole());
        existingUser.setStatus(userDTO.getStatus());

        // Cập nhật mật khẩu nếu có thay đổi
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPasswordHash(userDTO.getPassword()); // Trong thực tế, cần mã hóa mật khẩu
        }

        userRepository.save(existingUser);

        // Ghi log
        Log log = new Log();
        log.setAction("UPDATE_USER");
        log.setDetails("Cập nhật thông tin người dùng: " + existingUser.getUsername());
        log.setTime(LocalDateTime.now());
        logRepository.save(log);

        return "redirect:/users";
    }

    // Xử lý xóa người dùng
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Integer id, HttpServletRequest request) {
        // Kiểm tra đăng nhập
        if (request.getSession().getAttribute("admin") == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            userRepository.delete(user);

            // Ghi log
            Log log = new Log();
            log.setAction("DELETE_USER");
            log.setDetails("Xóa người dùng: " + user.getUsername());
            log.setTime(LocalDateTime.now());
            logRepository.save(log);
        }

        return "redirect:/users";
    }
}