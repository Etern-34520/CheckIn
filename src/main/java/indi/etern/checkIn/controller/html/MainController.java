package indi.etern.checkIn.controller.html;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Arrays;

@Controller
public class MainController {
    @GetMapping({"/", "/index.html"})
    public String index(HttpServletRequest request) {//FIXME worker
        final Cookie[] cookies = request.getCookies();
        final String referer = request.getHeader("referer");
        final boolean byServiceWorker = referer != null && referer.contains("sw.js"); /*For workbox.js auto ajax request below*/;
        final boolean isLogin = cookies != null && Arrays.stream(cookies).anyMatch(cookie -> cookie.getName().equals("token"));
        if (byServiceWorker) {
            return "manage/index.html";
        }
        if (isLogin) {
            return "redirect:/manage/";
        } else {
            return "redirect:/exam/";
        }
    }
    
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
    
    @GetMapping("/workbox-{id}.js")
    public String workbox(@PathVariable String id) {
        return "manage/workbox-" + id + ".js";
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