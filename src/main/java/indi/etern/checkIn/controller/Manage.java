package indi.etern.checkIn.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class Manage {
    Logger logger = LoggerFactory.getLogger(getClass());
    public Manage() {
//        System.out.println("Manage");
    }
    @GetMapping("/manage/")
    public ModelAndView manage(HttpServletRequest request){
        /*if (request.getSession().getAttribute("user") == null){
            ModelAndView mv = new ModelAndView();
            mv.setViewName("redirect:/login");
            return mv;
        } else */if (request.getParameter("page")==null){
            ModelAndView mv = new ModelAndView();
            mv.setViewName("manage/manage");
//        return mv;
            return mv;
        } else {
            ModelAndView mv = new ModelAndView();
            mv.setViewName("manage/"+request.getParameter("pageClass")+"_"+request.getParameter("page"));//类似于"server_0" "user_0"
            return mv;
        }
    }
}
