package indi.etern.checkIn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
//@SpringBootApplication(exclude= {SecurityAutoConfiguration.class })//TODO
public class CheckInApplication {
	public static ConfigurableApplicationContext applicationContext;

	public static void main(String[] args) {
        CheckInApplication.applicationContext = SpringApplication.run(CheckInApplication.class, args);
	}

}
