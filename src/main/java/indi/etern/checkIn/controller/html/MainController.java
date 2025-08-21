package indi.etern.checkIn.controller.html;

import indi.etern.checkIn.service.web.TurnstileService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Arrays;

@Controller
public class MainController {
    private final TurnstileService turnstileService;

    public MainController(TurnstileService turnstileService) {
        this.turnstileService = turnstileService;
    }

    @GetMapping({"/", "/index.html"})
    public String index(HttpServletRequest request, HttpServletResponse response) {
        final Cookie[] cookies = request.getCookies();
        final String referer = request.getHeader("referer");
        final boolean byServiceWorker = referer != null && referer.contains("sw.js"); /*For workbox.js auto ajax request below*/
        final boolean isLogin = cookies != null && Arrays.stream(cookies).anyMatch(cookie -> cookie.getName().equals("token"));
        boolean enabledOnLogin = turnstileService.isTurnstileEnabledOnLogin();
        boolean enabledOnExam = turnstileService.isTurnstileEnabledOnExam();
        if (enabledOnLogin || enabledOnExam) {
            response.addCookie(new Cookie("siteKey", turnstileService.getSiteKey()));
            response.addCookie(new Cookie("verifyLogin", String.valueOf(enabledOnLogin)));
            response.addCookie(new Cookie("verifyExam", String.valueOf(enabledOnExam)));
        }
        if (byServiceWorker) {
            return "front-face/index.html";
        }
        if (isLogin) {
            return "redirect:/manage/";
        } else {
            return "redirect:/exam/";
        }
    }

    @GetMapping("/exam/**")
    public String exam(HttpServletResponse response) {
        boolean enabledOnExam = turnstileService.isTurnstileEnabledOnExam();
        if (enabledOnExam) {
            response.addCookie(new Cookie("siteKey", turnstileService.getSiteKey()));
            response.addCookie(new Cookie("verifyExam", String.valueOf(enabledOnExam)));
        }
        return "front-face/index.html";
    }

    @GetMapping({"/manage/**", "/login/"})
    public String manage(HttpServletResponse response) {
        boolean enabledOnLogin = turnstileService.isTurnstileEnabledOnLogin();
        if (enabledOnLogin) {
            response.addCookie(new Cookie("siteKey", turnstileService.getSiteKey()));
            response.addCookie(new Cookie("verifyLogin", String.valueOf(enabledOnLogin)));
        }
        return "front-face/index.html";
    }

    @GetMapping("/manifest.webmanifest")
    public String manifest() {
        return "front-face/manifest.webmanifest";
    }

    @GetMapping("/registerSW.js")
    public String registerSW() {
        return "front-face/registerSW.js";
    }

    @GetMapping("/workbox-{id}.js")
    public String workbox(@PathVariable String id) {
        return "front-face/workbox-" + id + ".js";
    }

    @GetMapping("/sw.js")
    public String sw() {
        return "front-face/sw.js";
    }

    @GetMapping("/icons/{fileName}")
    public String icons(@PathVariable String fileName) {
        return "icons/" + fileName;
    }
}