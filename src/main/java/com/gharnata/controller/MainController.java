package com.gharnata.controller;

import com.gharnata.service.ServNotification;
import com.gharnata.service.ServProduit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/gharnata")
@Controller
public class MainController {
    @Autowired
    private ServNotification servNotification;
    @Autowired
    private ServProduit servProduit;

    @GetMapping("/notif-stock")
    public String notif(Model model){
        model.addAttribute("pds", this.servNotification.getAll());
        model.addAttribute("notif",servNotification.nbNotif());
        model.addAttribute("cats", this.servProduit.getAllCats());
        return "gest/notification";
    }

    @GetMapping("/test")
    public String test(Model model){
        return "test";
    }



}
