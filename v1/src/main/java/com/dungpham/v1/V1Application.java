package com.dungpham.v1;

import com.dungpham.v1.entity.Role;
import com.dungpham.v1.entity.User;
import com.dungpham.v1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class V1Application implements CommandLineRunner {

	@Autowired
	private UserRepository userRepository;

	public static void main(String[] args) {
		SpringApplication.run(V1Application.class, args);
	}

	public void run(String... args) throws Exception {
		User adminAccount = userRepository.findByRole(Role.ADMIN);
		if(null == adminAccount){
			// create admin account
			User user = new User();
			user.setEmail("admin@gmail.com");
			user.setFirstName("admin");
			user.setLastName("admin");
			user.setRole(Role.ADMIN);
			user.setPassword(new BCryptPasswordEncoder().encode("admin"));
			userRepository.save(user);

			// create employee accounts
			for(int i = 0; i < 5; i++) {
				User user1 = new User();
				user1.setEmail("employee" + i + "@gmail.com");
				user1.setFirstName("employee" + i);
				user1.setLastName("employee" + i);
				user1.setRole(Role.EMPLOYEE);
				user1.setPassword(new BCryptPasswordEncoder().encode("employee"));
				userRepository.save(user1);
			}

			for(int i = 0; i < 5; i++) {
				User user1 = new User();
				user1.setEmail("customer" + i + "@gmail.com");
				user1.setFirstName("customer" + i);
				user1.setLastName("customer" + i);
				user1.setRole(Role.CUSTOMER);
				user1.setPassword(new BCryptPasswordEncoder().encode("customer"));
				userRepository.save(user1);
			}
		}

	}
}
