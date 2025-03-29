package indi.etern.checkIn;

import indi.etern.checkIn.service.dao.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@SpringBootApplication
@EnableCaching
public class CheckInApplication {
	public static ConfigurableApplicationContext applicationContext;
	private final UserService userService;
	
	public CheckInApplication(UserService userService) {
		this.userService = userService;
	}
	
	public static void main(String[] args) {
        CheckInApplication.applicationContext = SpringApplication.run(CheckInApplication.class, args);
	}
	
	@Bean
	public DataSourceInitializer dataSourceInitializer(final DataSource dataSource) {
		if (userService.findAll().isEmpty()) {
			ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
			resourceDatabasePopulator.addScript(new ClassPathResource("/data.sql"));
			DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
			dataSourceInitializer.setDataSource(dataSource);
			dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);
			return dataSourceInitializer;
		} else {
			return null;
		}
	}
}