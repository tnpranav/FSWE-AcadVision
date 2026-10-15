package edu.acadvision.controller;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
public class HealthController {
  @GetMapping("/actuator/health") public Map<String,String> health(){return Map.of("status","UP","application","AcadVision API");}
  @GetMapping("/api") public Map<String,Object> api(){return Map.of("name","AcadVision API","version","1.0.0","resources",new String[]{"admin/students","admin/faculty","admin/courses","faculty/attendance","student/fees"});}
}
