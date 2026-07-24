package com.axis.nachimbal.domain.user.repository;

import com.axis.nachimbal.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
