package com.ecommerce.repositories;

import com.ecommerce.model.AppRole;
import com.ecommerce.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByRoleName(AppRole appRole);
}
