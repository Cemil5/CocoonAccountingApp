package com.cocoon.controller;

import com.cocoon.repository.TestRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TestController {


    TestRepository testRepository;

    public TestController(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    @RequestMapping(value = {"/test"})
    public String test(Model model){

        model.addAttribute("dbmessage", testRepository.getById(0).getTestval());
        return "test";
    }

    @RequestMapping(value = "/test/dashboard")
    public String getDashBoard(){
        return "dashboard";
    }
}
