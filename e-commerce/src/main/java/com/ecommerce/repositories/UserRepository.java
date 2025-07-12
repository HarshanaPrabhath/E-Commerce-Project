package com.ecommerce.repositories;

import com.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

     Optional<User> findByEmail(String email);

     Optional<User> findByUserName(String username);

     boolean existsByUserName( String username);

     boolean existsByEmail(String email);
}
