package com.gharnata.config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;




@Controller
public class GlobalErrorController implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(GlobalErrorController.class);



    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, HttpServletResponse response, Model model) {

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Throwable exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        if (exception != null) {
            logger.error("Erreur {} sur {} : ", status, request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI), exception);
        } else {
            logger.error("Erreur {} sur {} — message : {}", status, request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI), message);
        }

        int code = (status != null) ? (int) status : response.getStatus(); // fallback fiable

        model.addAttribute("code", code);
        model.addAttribute("message", message != null ? message : "Une erreur est survenue.");

        return "auth/error";
    }
}

    /*@RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Throwable exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        if (exception != null) {
            logger.error("Erreur {} sur {} : ", status, request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI), exception);
        } else {
            logger.error("Erreur {} sur {} — message : {}", status, request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI), message);
        }

        model.addAttribute("code", status != null ? status : 500);
        model.addAttribute("message", message != null ? message : "Une erreur est survenue.");

        return "auth/error";
    }
}*/
/*@Controller
public class GlobalErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        model.addAttribute("code", status != null ? status : 500);
        model.addAttribute(
                "message",
                message != null ? message : "Une erreur est survenue."
        );

        return "auth/error";
    }
}*/

