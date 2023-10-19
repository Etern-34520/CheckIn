package indi.etern.checkIn.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
public class QuestionController {
    @GetMapping(value = "/question",consumes = "multipart/form-data")
//    @RequestMapping(value = "/insert",method = RequestMethod.POST, consumes = "multipart/form-data")
    public void insert(HttpServletRequest request) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
    }
}
