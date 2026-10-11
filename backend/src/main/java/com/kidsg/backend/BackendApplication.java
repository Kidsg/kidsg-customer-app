package com.kidsg.backend;

import com.kidsg.backend.models.ERole;
import com.kidsg.backend.models.Role;
import com.kidsg.backend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	CommandLineRunner initRoles(RoleRepository roleRepository) {
		return args -> {
			for (ERole role : ERole.values()) {
				if (roleRepository.findByName(role).isEmpty()) {
					roleRepository.save(new Role(role));
				}
			}
		};
	}
}