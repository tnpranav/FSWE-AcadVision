package edu.acadvision.repository;

import edu.acadvision.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
  Optional<AppUser> findByUserId(String userId);
  Optional<AppUser> findByEmail(String email);
  Optional<AppUser> findByUserIdOrEmail(String userId, String email);
  boolean existsByUserId(String userId);
  boolean existsByEmail(String email);
}
