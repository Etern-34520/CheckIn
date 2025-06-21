package indi.etern.checkIn.controller.html;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class MainController {
    @GetMapping("/manage/**")
    public String manage() {
        return "manage/index.html";
    }

    @GetMapping("/login/")
    public String login() {
        return "manage/index.html";
    }

    @GetMapping("/exam/**")
    public String exam() {
        return "exam/index.html";
    }
    
    @GetMapping("/manifest.webmanifest")
    public String manifest() {
        return "manage/manifest.webmanifest";
    }
    
    @GetMapping("/registerSW.js")
    public String registerSW() {
        return "manage/registerSW.js";
    }
    
    @GetMapping("/sw.js")
    public String sw() {
        return "manage/sw.js";
    }
    
    @GetMapping("/icons/{fileName}")
    public String icons(@PathVariable String fileName) {
        return "icons/" + fileName;
    }
}