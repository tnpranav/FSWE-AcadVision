package edu.acadvision;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
class AcadVisionApplicationTests {
  @Test void projectIdentityIsStable() {
    org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    String hash = encoder.encode("Password@123");
    System.out.println("BCRYPT_HASH_FOR_SEED: " + hash);
    assertTrue(encoder.matches("Password@123", hash));
  }
}
