package com.gharnata.controller;



import com.gharnata.service.ServNotification;
import com.gharnata.service.ServProduit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/gharnata")
public class UserController {

    @Autowired
    private ServNotification servNotification;
    @Autowired
    private ServProduit servProduit;

    @GetMapping("/utilisateurs")
    public String listClient(Model model){
        model.addAttribute("notif",servNotification.nbNotif());
        model.addAttribute("cats", this.servProduit.getAllCats());
        return "gest/userManagement";
    }
}
