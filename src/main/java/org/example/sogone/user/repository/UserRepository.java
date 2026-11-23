package org.example.sogone.user.repository;

import org.example.sogone.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
	
}
