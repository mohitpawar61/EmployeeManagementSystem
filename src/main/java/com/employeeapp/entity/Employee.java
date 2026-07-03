package com.employeeapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="employees")
@Data

public class Employee {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private String name;
	
	private String gender;
	
	@Column(unique = true)
	private String email;
	
	private Double salary;
	
	private String address;
	
	private String mobileNo;
	
	private String dep;
	
	private String dob;
	
	private String profilePicture;
	
	
}
