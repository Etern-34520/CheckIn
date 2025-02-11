package indi.etern.checkIn.controller.html;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.Optional;

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
    public String exam(HttpServletRequest httpServletRequest) {
        Optional<Cookie> optionalPhraseCookie = Arrays.stream(httpServletRequest.getCookies())
                .filter(cookie -> cookie.getName().equals("phrase")).findFirst();
        if (optionalPhraseCookie.isEmpty() || optionalPhraseCookie.get().getValue().equals("facade")) {
            HttpSession httpSession = httpServletRequest.getSession();
            if (!httpSession.isNew()) {
                httpSession.invalidate();
            }
        }
        return "exam/index";
    }
}