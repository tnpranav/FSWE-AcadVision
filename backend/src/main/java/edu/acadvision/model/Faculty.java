package edu.acadvision.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
@Entity @Table(name="faculty")
public class Faculty {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="faculty_id",nullable=false,unique=true) @NotBlank private String facultyId;
  @Column(name="user_id") private String userId;
  @Column(name="full_name",nullable=false) @NotBlank private String fullName;
  @Column(nullable=false) @Email @NotBlank private String email;
  @Column(nullable=false) @NotBlank private String department;
  @Column(name="course_count",nullable=false) @Min(0) private Integer courseCount=0;
  @Column(nullable=false) private String status="Active";
  @Column(name="created_at",insertable=false,updatable=false) private OffsetDateTime createdAt;
  public Long getId(){return id;} public String getFacultyId(){return facultyId;} public void setFacultyId(String v){facultyId=v;}
  public String getUserId(){return userId;} public void setUserId(String v){userId=v;}
  public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
  public String getEmail(){return email;} public void setEmail(String v){email=v;}
  public String getDepartment(){return department;} public void setDepartment(String v){department=v;}
  public Integer getCourseCount(){return courseCount;} public void setCourseCount(Integer v){courseCount=v;}
  public String getStatus(){return status;} public void setStatus(String v){status=v;}
}
