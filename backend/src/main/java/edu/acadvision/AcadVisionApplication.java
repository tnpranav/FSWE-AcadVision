package edu.acadvision;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableScheduling
public class AcadVisionApplication {
  public static void main(String[] args) { SpringApplication.run(AcadVisionApplication.class, args); }
}
