package indi.etern.checkIn.controller.html;

import indi.etern.checkIn.service.dao.UserTrafficService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class Exam {
    private final UserTrafficService userTrafficService;
    public Exam(UserTrafficService userTrafficService) {
        this.userTrafficService = userTrafficService;
    }
    @RequestMapping(method = RequestMethod.GET,path = "/exam/")
    public ModelAndView exam(HttpServletRequest httpServletRequest) {
        httpServletRequest.setAttribute("action","view exam");
        userTrafficService.log(-1,httpServletRequest);
        return new ModelAndView("exam/home");
    }
}
