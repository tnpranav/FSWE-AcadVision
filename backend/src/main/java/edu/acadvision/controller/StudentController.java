package edu.acadvision.controller;
import edu.acadvision.model.Student;
import edu.acadvision.repository.StudentRepository;
import edu.acadvision.repository.AppUserRepository;
import edu.acadvision.model.AppUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController @RequestMapping("/api/admin/students")
public class StudentController {
  private final StudentRepository repository;
  private final AppUserRepository users;
  private final PasswordEncoder passwords;
  public StudentController(StudentRepository repository, AppUserRepository users, PasswordEncoder passwords){this.repository=repository;this.users=users;this.passwords=passwords;}
  @GetMapping public List<Student> all(@RequestParam(required=false) String q) {
    return (q==null||q.isBlank())?repository.findAll():repository.findByFullNameContainingIgnoreCaseOrStudentIdContainingIgnoreCase(q,q);
  }
  @GetMapping("/{studentId}") public Student get(@PathVariable String studentId) {
    return repository.findByStudentId(studentId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Student not found"));
  }
  @PostMapping @ResponseStatus(HttpStatus.CREATED) @Transactional public Student create(@Valid @RequestBody CreateStudent input) {
    String studentId=input.studentId().trim(), email=input.email().trim().toLowerCase();
    if(repository.findByStudentId(studentId).isPresent() || users.existsByUserId(studentId)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Student ID already exists");
    if(users.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Email is already assigned to an account");
    users.save(AppUser.builder().userId(studentId).fullName(input.fullName().trim()).email(email)
        .passwordHash(passwords.encode(input.initialPassword())).role("STUDENT")
        .active(!"Inactive".equalsIgnoreCase(input.status())).mustChangePassword(true).mfaEnabled(false).build());
    Student student=new Student(); student.setStudentId(studentId); student.setUserId(studentId);
    student.setFullName(input.fullName().trim()); student.setEmail(email); student.setDepartment(input.department().trim());
    student.setSemester(input.semester()); student.setCgpa(input.cgpa()); student.setAttendance(input.attendance());
    student.setStatus(input.status()); student.setHobbies(input.hobbies());
    return repository.save(student);
  }
  @PutMapping("/{studentId}") public Student update(@PathVariable String studentId,@Valid @RequestBody Student input) {
    Student s=get(studentId); s.setFullName(input.getFullName());s.setEmail(input.getEmail());s.setDepartment(input.getDepartment());
    s.setSemester(input.getSemester());s.setCgpa(input.getCgpa());s.setAttendance(input.getAttendance());s.setStatus(input.getStatus());s.setHobbies(input.getHobbies());
    users.findByUserId(studentId).ifPresent(user -> { user.setActive(!"Inactive".equalsIgnoreCase(input.getStatus())); users.save(user); });
    return repository.save(s);
  }
  @DeleteMapping("/{studentId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deactivate(@PathVariable String studentId) {
    Student s=get(studentId); s.setStatus("Inactive"); repository.save(s);
    users.findByUserId(studentId).ifPresent(user -> { user.setActive(false); users.save(user); });
  }
  public record CreateStudent(@NotBlank @Size(max=60) String studentId, @NotBlank @Size(max=160) String fullName,
      @NotBlank @Email @Size(max=180) String email, @NotBlank @Size(max=120) String department,
      @NotBlank @Size(min=12,max=72) String initialPassword, @Min(1) Integer semester,
      @jakarta.validation.constraints.DecimalMin("0.0") @jakarta.validation.constraints.DecimalMax("10.0") Double cgpa,
      @jakarta.validation.constraints.DecimalMin("0.0") @jakarta.validation.constraints.DecimalMax("100.0") Double attendance,
      @NotBlank String status, String hobbies) {}
}
