package com.employeeapp.service.impl;

import java.io.File;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.employeeapp.entity.Employee;
import com.employeeapp.repository.EmployeeRepo;
import com.employeeapp.service.EmployeeService;

@Service
public class EmployeeServiceImpl implements EmployeeService{

	@Autowired
	EmployeeRepo employeeRepository;
	
	
	private final String uploadProfilePicturesPath = "D:\\mohit\\RESUME\\EmployeeProfiles\\";

	
	@Override
	public Employee saveEmployee(Employee employee) {
		
		if(employee.getDep().equals("IT"))
		{
			employee.setSalary(45000.00);
		}
		else if(employee.getDep().equals("HR"))
		{
			employee.setSalary(30000.00);
		}
		else if(employee.getDep().equals("Finance"))
		{
			employee.setSalary(25000.00);
		}
		else if(employee.getDep().equals("Marketing"))
		{
			employee.setSalary(22000.00);
		}
		else {
			employee.setSalary(15000.00);
		}
		
		return employeeRepository.save(employee);	
		
	}

	@Override
	public boolean existsByEmail(String email) {
		
		return employeeRepository.existsByEmail(email);
	}

	@Override
	public Page<Employee> getAllEmployees(int page, int size) {
		
		PageRequest of = PageRequest.of(page, size);
		
		Page<Employee> employees = employeeRepository.findAll(of);
		
		return employees;
	}

	@Override
	public Employee getEmployeeById(int id) {
		
		Optional<Employee> byId = employeeRepository.findById(id);
		
		Employee employee = null;
		
		if(byId.isPresent())
		{
			employee = byId.get();
		}
		
		return employee;
	}

	@Override
	public Employee updateEmployee(int id, Employee employee, MultipartFile file) {
		
		Optional<Employee> byId = employeeRepository.findById(id);
		
        Employee existingEmployee = null;
		
		if(byId.isPresent())
		{
			existingEmployee = byId.get();
		}
		
		
		existingEmployee.setAddress(employee.getAddress());
		existingEmployee.setDep(employee.getDep());
		existingEmployee.setDob(employee.getDob());
		existingEmployee.setEmail(employee.getEmail());
		existingEmployee.setGender(employee.getGender());
		existingEmployee.setMobileNo(employee.getMobileNo());
		existingEmployee.setName(employee.getName());
		String newFileName = existingEmployee.getProfilePicture();
		
		try {
        String fileName = file.getOriginalFilename();
		
		long value = System.currentTimeMillis();
		
		 newFileName = value + fileName;
		
		file.transferTo(new File(uploadProfilePicturesPath + newFileName));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		existingEmployee.setProfilePicture(newFileName);
		
		 return employeeRepository.save(existingEmployee);
		
		

	}

	

	
	
}
