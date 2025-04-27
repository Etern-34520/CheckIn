package indi.etern.checkIn;

import indi.etern.checkIn.service.dao.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    private final UserService userService;
    
    public DataSourceConfig(UserService userService) {
        this.userService = userService;
    }
    
    @Bean
    public DataSourceInitializer dataSourceInitializer(final DataSource dataSource) {
        if (userService.findAll().isEmpty()) {
            ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
            resourceDatabasePopulator.addScript(new ClassPathResource("/db/initialization/data.sql"));
            DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
            dataSourceInitializer.setDataSource(dataSource);
            dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);
            return dataSourceInitializer;
        } else {
            return null;
        }
    }
}