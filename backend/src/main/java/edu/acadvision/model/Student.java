package edu.acadvision.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

@Entity @Table(name="students")
public class Student {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="student_id",nullable=false,unique=true) @NotBlank private String studentId;
  @Column(name="user_id") private String userId;
  @Column(name="full_name",nullable=false) @NotBlank private String fullName;
  @Column(nullable=false) @Email @NotBlank private String email;
  @Column(nullable=false) @NotBlank private String department;
  @Column(nullable=false) @Min(1) private Integer semester=1;
  @Column(nullable=false) @DecimalMin("0.0") @DecimalMax("10.0") private Double cgpa=0.0;
  @Column(nullable=false) @DecimalMin("0.0") @DecimalMax("100.0") private Double attendance=100.0;
  @Column(nullable=false) private String status="Active";
  @Column(columnDefinition="TEXT") private String hobbies;
  @Column(name="created_at",insertable=false,updatable=false) private OffsetDateTime createdAt;
  @Column(name="updated_at",insertable=false,updatable=false) private OffsetDateTime updatedAt;
  public Long getId(){return id;} public String getStudentId(){return studentId;} public void setStudentId(String v){studentId=v;}
  public String getUserId(){return userId;} public void setUserId(String v){userId=v;}
  public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
  public String getEmail(){return email;} public void setEmail(String v){email=v;}
  public String getDepartment(){return department;} public void setDepartment(String v){department=v;}
  public Integer getSemester(){return semester;} public void setSemester(Integer v){semester=v;}
  public Double getCgpa(){return cgpa;} public void setCgpa(Double v){cgpa=v;}
  public Double getAttendance(){return attendance;} public void setAttendance(Double v){attendance=v;}
  public String getStatus(){return status;} public void setStatus(String v){status=v;}
  public String getHobbies(){return hobbies;} public void setHobbies(String v){hobbies=v;}
  public OffsetDateTime getCreatedAt(){return createdAt;}
}
