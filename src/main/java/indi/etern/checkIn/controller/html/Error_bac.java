package indi.etern.checkIn.controller.html;

import org.springframework.web.bind.annotation.RequestMapping;

//@Controller
@RequestMapping("/error")
public class Error_bac {
    @RequestMapping("/404")
    public String error404() {
        return "error/404";
    }
    @RequestMapping("/500")
    public String error500() {
        return "error/500";
    }
    @RequestMapping(".")
    public String error() {
        return "error/index";
    }
}
