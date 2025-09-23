package com.project;

import com.project.auth.jwt.JwtAccessProperty;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
//@ComponentScan(basePackages = "com.project")
@EnableConfigurationProperties(JwtAccessProperty.class)
public class UserApplication extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(UserApplication.class, args);
	}
}
