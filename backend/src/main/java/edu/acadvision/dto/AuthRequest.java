package edu.acadvision.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
  @NotBlank(message = "User ID or Email is required")
  private String username;

  @NotBlank(message = "Password is required")
  private String password;
}
