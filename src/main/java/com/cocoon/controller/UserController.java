package com.cocoon.controller;

import com.cocoon.dto.UserDTO;
import com.cocoon.exception.CocoonException;
import com.cocoon.service.CompanyService;
import com.cocoon.service.RoleService;
import com.cocoon.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Controller
@RequestMapping("/user")
public class UserController {

    private UserService userService;
    private RoleService roleService;
    private CompanyService companyService;

    public UserController(UserService userService, RoleService roleService, CompanyService companyService) {
        this.userService = userService;
        this.roleService = roleService;
        this.companyService = companyService;
    }

    @GetMapping("/list")
    public String findUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "user/user-list";
    }

    @PostMapping("/create")
    public String createUser(UserDTO userDTO) throws CocoonException {
        userService.save(userDTO);
        return "redirect:/user/list";
    }

    @GetMapping("/create")
    public String getCreatePage(Model model) {
        model.addAttribute("user", new UserDTO());
        model.addAttribute("roles", roleService.findAllRoles());
        model.addAttribute("companies", companyService.getAllCompanies());
        return "user/user-add";
    }

    @GetMapping("update/{id}")
    public String getUpdatePage(@PathVariable Long id, Model model) throws CocoonException {
        UserDTO foundUser = userService.findById(id);
        model.addAttribute("userToEdit", foundUser);
        model.addAttribute("companies", companyService.getAllCompanies());
        model.addAttribute("roles", roleService.findAllRoles());
        return "user/user-update";
    }

    @PostMapping("update/{id}")
    public String updateUser(@ModelAttribute UserDTO userDTO) throws CocoonException {
        userService.update(userDTO);
        return "redirect:/user/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) throws CocoonException {
        userService.delete(id);
        return "redirect:/user/list";
    }

}
