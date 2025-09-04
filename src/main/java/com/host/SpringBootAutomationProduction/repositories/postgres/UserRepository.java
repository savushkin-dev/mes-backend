package com.host.SpringBootAutomationProduction.repositories.postgres;


import com.host.SpringBootAutomationProduction.model.postgres.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);


}
