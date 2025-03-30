package indi.etern.checkIn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableCaching
public class CheckInApplication {
	public static ConfigurableApplicationContext applicationContext;
	
	public static void main(String[] args) {
        CheckInApplication.applicationContext = SpringApplication.run(CheckInApplication.class, args);
	}
	
	@Getter
	private static ObjectMapper objectMapper;
	
	@Bean
	public ObjectMapper objectMapper(){
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		return objectMapper;
	}
	
	@Bean
	public Cache<String, Object> caffeineCache() {
		return Caffeine.newBuilder()
				.expireAfterWrite(60, TimeUnit.SECONDS)
				.initialCapacity(100)
				.maximumSize(1000)
				.build();
	}
}