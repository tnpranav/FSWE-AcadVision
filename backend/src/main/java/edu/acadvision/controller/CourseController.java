package edu.acadvision.controller;
import edu.acadvision.model.Course;
import edu.acadvision.repository.CourseRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
@RestController @RequestMapping("/api/admin/courses")
public class CourseController {
  private final CourseRepository repository;
  public CourseController(CourseRepository repository){this.repository=repository;}
  @GetMapping public List<Course> all(){return repository.findAll();}
  @GetMapping("/{courseCode}") public Course get(@PathVariable String courseCode){return repository.findByCourseCode(courseCode).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Course not found"));}
  @PostMapping @ResponseStatus(HttpStatus.CREATED) public Course create(@Valid @RequestBody Course course){
    if(repository.findByCourseCode(course.getCourseCode()).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Course code already exists");
    return repository.save(course);
  }
  @PutMapping("/{courseCode}") public Course update(@PathVariable String courseCode,@Valid @RequestBody Course input){
    Course c=get(courseCode);c.setTitle(input.getTitle());c.setFacultyName(input.getFacultyName());c.setEnrolledStudents(input.getEnrolledStudents());c.setCompletion(input.getCompletion());c.setColor(input.getColor());c.setActive(input.getActive());return repository.save(c);
  }
  @DeleteMapping("/{courseCode}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deactivate(@PathVariable String courseCode){Course c=get(courseCode);c.setActive(false);repository.save(c);}
}
