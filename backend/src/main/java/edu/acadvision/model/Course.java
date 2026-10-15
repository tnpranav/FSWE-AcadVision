package edu.acadvision.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
@Entity @Table(name="courses")
public class Course {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="course_code",nullable=false,unique=true) @NotBlank private String courseCode;
  @Column(nullable=false) @NotBlank private String title;
  @Column(name="faculty_name",nullable=false) @NotBlank private String facultyName;
  @Column(name="enrolled_students",nullable=false) @Min(0) private Integer enrolledStudents=0;
  @Column(nullable=false) @Min(0) @Max(100) private Integer completion=0;
  @Column(nullable=false) private String color="#7065f0";
  @Column(nullable=false) private Boolean active=true;
  public Long getId(){return id;} public String getCourseCode(){return courseCode;} public void setCourseCode(String v){courseCode=v;}
  public String getTitle(){return title;} public void setTitle(String v){title=v;}
  public String getFacultyName(){return facultyName;} public void setFacultyName(String v){facultyName=v;}
  public Integer getEnrolledStudents(){return enrolledStudents;} public void setEnrolledStudents(Integer v){enrolledStudents=v;}
  public Integer getCompletion(){return completion;} public void setCompletion(Integer v){completion=v;}
  public String getColor(){return color;} public void setColor(String v){color=v;}
  public Boolean getActive(){return active;} public void setActive(Boolean v){active=v;}
}
