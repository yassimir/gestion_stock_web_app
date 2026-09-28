package com.gharnata;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class GharnataApplication {

	public static void main(String[] args) {
		SpringApplication.run(GharnataApplication.class, args);
	}

}
