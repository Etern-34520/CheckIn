package indi.etern.checkIn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
public class MVCConfig implements WebMvcConfigurer {
@Getter
    private static Gson gson;
    
    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        
        viewResolver.setPrefix("/view/");
        viewResolver.setSuffix(".html");
        
        viewResolver.setExposeContextBeansAsAttributes(true);
        return viewResolver;
    }
    
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
    
    @Bean
    public Gson gson(){
        gson = new Gson();
        return gson;
    }
    
    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry resourceHandlerRegistry) {
        resourceHandlerRegistry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/view/exam/assets/")
                .addResourceLocations("classpath:/static/view/manage/assets/");
    }
}
