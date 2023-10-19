package indi.etern.checkIn;

import indi.etern.checkIn.dao.Dao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@Configuration
public class MVCConfig {
    private final Dao dao = new Dao("indi.etern.checkIn.beans");
    @Bean
    public String getDaoBeanPackReference() {
        return "indi.etern.checkIn.entities";
    }
    @Bean
    public Dao getDao(){
        return dao;
    }
    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        
        viewResolver.setViewClass(JstlView.class);
        viewResolver.setPrefix("/WEB-INF/view/");
        viewResolver.setSuffix(".jsp");
        
        viewResolver.setExposeContextBeansAsAttributes(true);
        return viewResolver;
    }
    
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}
