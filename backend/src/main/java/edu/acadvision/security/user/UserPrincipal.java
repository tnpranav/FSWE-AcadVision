package edu.acadvision.security.user;

import edu.acadvision.model.AppUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

@Getter
public class UserPrincipal implements UserDetails {
  private final Long id;
  private final String userId;
  private final String fullName;
  private final String email;
  private final String password;
  private final String role;
  private final boolean active;
  private final Collection<? extends GrantedAuthority> authorities;

  public UserPrincipal(AppUser user) {
    this.id = user.getId();
    this.userId = user.getUserId();
    this.fullName = user.getFullName();
    this.email = user.getEmail();
    this.password = user.getPasswordHash();
    this.role = user.getRole();
    this.active = Boolean.TRUE.equals(user.getActive());
    this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return userId;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return active;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return active;
  }
}
