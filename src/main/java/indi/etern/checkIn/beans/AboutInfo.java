package indi.etern.checkIn.beans;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AboutInfo {
    Map<String,String> relyUrlMap = new HashMap<>();
    public AboutInfo() {
        relyUrlMap.put("Spring Boot","https://spring.io/projects/spring-boot");
        relyUrlMap.put("Jjwt","https://github.com/jwtk/jjwt");
        relyUrlMap.put("H2database","https://h2database.com/html/main.html");
        relyUrlMap.put("Gson","https://github.com/google/gson");
        relyUrlMap.put("Jackson","https://github.com/FasterXML/jackson");
        relyUrlMap.put("Tomcat","https://tomcat.apache.org/");
        relyUrlMap.put("Jakarta","https://jakarta.ee/zh/");
        relyUrlMap.put("Lombok","https://projectlombok.org/");
        relyUrlMap.put("Slf4j","https://slf4j.org/");
        relyUrlMap.put("Testng","https://testng.org/");
        relyUrlMap.put("Junit","https://junit.org/junit5/");
    }
    public Map<String,String> getRelyUrlMap() {
        return relyUrlMap;
    }
}
