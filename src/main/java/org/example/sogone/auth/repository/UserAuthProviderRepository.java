package org.example.sogone.auth.repository;

import org.example.sogone.auth.entity.UserAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuthProviderRepository extends JpaRepository<UserAuthProvider, Long> {
}
