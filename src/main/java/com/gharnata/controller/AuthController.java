package com.gharnata.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
        @GetMapping("/")
        public String ff(){
                return "redirect:/gharnata/show-products";
        }
        @GetMapping("/login")
        public String login(){
                return "auth/loginP";
        }
}
