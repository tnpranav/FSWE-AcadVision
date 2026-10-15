package edu.acadvision.controller;
import edu.acadvision.model.Faculty;
import edu.acadvision.repository.FacultyRepository;
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
@RestController @RequestMapping("/api/admin/faculty")
public class FacultyController {
  private final FacultyRepository repository;
  private final AppUserRepository users;
  private final PasswordEncoder passwords;
  public FacultyController(FacultyRepository repository, AppUserRepository users, PasswordEncoder passwords){this.repository=repository;this.users=users;this.passwords=passwords;}
  @GetMapping public List<Faculty> all(){return repository.findAll();}
  @GetMapping("/{facultyId}") public Faculty get(@PathVariable String facultyId){return repository.findByFacultyId(facultyId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Faculty not found"));}
  @PostMapping @ResponseStatus(HttpStatus.CREATED) @Transactional public Faculty create(@Valid @RequestBody CreateFaculty input){
    String facultyId=input.facultyId().trim(), email=input.email().trim().toLowerCase();
    if(repository.findByFacultyId(facultyId).isPresent() || users.existsByUserId(facultyId)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Faculty ID already exists");
    if(users.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Email is already assigned to an account");
    users.save(AppUser.builder().userId(facultyId).fullName(input.fullName().trim()).email(email)
        .passwordHash(passwords.encode(input.initialPassword())).role("FACULTY")
        .active(!"Inactive".equalsIgnoreCase(input.status())).mustChangePassword(true).mfaEnabled(false).build());
    Faculty faculty=new Faculty(); faculty.setFacultyId(facultyId); faculty.setUserId(facultyId);
    faculty.setFullName(input.fullName().trim()); faculty.setEmail(email); faculty.setDepartment(input.department().trim());
    faculty.setCourseCount(input.courseCount()); faculty.setStatus(input.status());
    return repository.save(faculty);
  }
  @PutMapping("/{facultyId}") public Faculty update(@PathVariable String facultyId,@Valid @RequestBody Faculty input){
    Faculty f=get(facultyId);f.setFullName(input.getFullName());f.setEmail(input.getEmail());f.setDepartment(input.getDepartment());f.setCourseCount(input.getCourseCount());f.setStatus(input.getStatus());
    users.findByUserId(facultyId).ifPresent(user -> { user.setActive(!"Inactive".equalsIgnoreCase(input.getStatus())); users.save(user); });return repository.save(f);
  }
  @DeleteMapping("/{facultyId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deactivate(@PathVariable String facultyId){Faculty f=get(facultyId);f.setStatus("Inactive");repository.save(f);users.findByUserId(facultyId).ifPresent(user -> { user.setActive(false); users.save(user); });}
  public record CreateFaculty(@NotBlank @Size(max=60) String facultyId, @NotBlank @Size(max=160) String fullName,
      @NotBlank @Email @Size(max=180) String email, @NotBlank @Size(max=120) String department,
      @NotBlank @Size(min=12,max=72) String initialPassword, @Min(0) Integer courseCount,
      @NotBlank String status) {}
}
