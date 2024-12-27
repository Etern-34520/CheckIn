package indi.etern.checkIn.controller.html;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @GetMapping("/manage/**")
    public String manage() {
        return "manage/index";
    }

    @GetMapping("/login/")
    public String login() {
        return "manage/index";
    }

    @GetMapping("/exam/**")
    public String exam() {
        return "exam/index";
    }
}