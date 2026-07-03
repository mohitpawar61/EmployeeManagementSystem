package com.employeeapp.service;



import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.employeeapp.entity.Employee;


public interface EmployeeService {
	
	Employee saveEmployee(Employee employee);
	
	boolean existsByEmail(String email);
	
	Page<Employee> getAllEmployees(int page,int size);

	Employee getEmployeeById(int id);
	
	Employee updateEmployee(int id,Employee employee,MultipartFile file);
}
