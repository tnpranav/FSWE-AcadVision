package edu.acadvision.security.user;

import edu.acadvision.model.AppUser;
import edu.acadvision.repository.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
  private final AppUserRepository userRepository;

  public CustomUserDetailsService(AppUserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
    AppUser user = userRepository.findByUserIdOrEmail(identifier, identifier)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with ID or email: " + identifier));
    return new UserPrincipal(user);
  }
}
