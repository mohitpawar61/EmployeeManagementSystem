package com.employeeapp.rest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.employeeapp.entity.Employee;
import com.employeeapp.service.EmployeeService;

@RestController
@RequestMapping("/employee")
@CrossOrigin(origins = "http://localhost:4200")
public class EmployeeRestController {

	@Autowired
	EmployeeService employeeService;
	
	@GetMapping("/all")
	public Page<Employee> getAllEmployees(@RequestParam(defaultValue="0") int page,
			@RequestParam(defaultValue="5") int size)
	{
		Page<Employee> allEmployees = employeeService.getAllEmployees(page, size);
		
		
		return allEmployees;
	}
	
	private final String uploadProfilePicturesPath = "D:\\mohit\\RESUME\\EmployeeProfiles\\";
	
	@PostMapping("/save")
	public ResponseEntity<?> saveEmployee(
			@RequestParam String name,
			@RequestParam String gender,
			@RequestParam String email,
			@RequestParam String address,
			@RequestParam String mobileNo,
			@RequestParam String dep,
			@RequestParam String dob,
			@RequestParam MultipartFile file
			) throws Exception
	{
		Map<String,Object> response = new HashMap<>();
		
		try {
		
		String fileName = file.getOriginalFilename();
		
		long value = System.currentTimeMillis();
		
		String newFileName = value + fileName;
		
		file.transferTo(new File(uploadProfilePicturesPath + newFileName));
		
		
		Employee emp = new Employee();
		emp.setName(name);
		emp.setGender(gender);
		emp.setEmail(email);
		emp.setAddress(address);
		emp.setMobileNo(mobileNo);
		emp.setDep(dep);
		emp.setDob(dob);
		emp.setProfilePicture(newFileName);
		
		if(employeeService.existsByEmail(email))
		{
		    response.put("message", "Email already exists");
		    response.put("status", false);
		    
		    return ResponseEntity
		            .status(HttpStatus.BAD_REQUEST)
		            .body(response);
		}
		
		Employee savedEmployee =  employeeService.saveEmployee(emp);
		
		
		response.put("message",
				"Employee Saved Successfully");
		
		response.put("status", true);
		
		response.put("employee", savedEmployee);
		
		return ResponseEntity.ok(response);
		
		}
		
		catch(DataIntegrityViolationException e)
		{
			response.put("message", 
					"Duplicate Email! Employee already exists");
			
			response.put("status", false);
			
			return ResponseEntity
					.status(HttpStatus.BAD_REQUEST)
					.body(response);
		}
		
		catch(Exception e)
		{
			response.put("message",
					"Something went wrong");
			
			response.put("status", false);
			
			return ResponseEntity
					.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(response);
		}
		
		
	}
	
	@GetMapping("/{id}")
	public Map<String,Employee> getById(@PathVariable int id)
	{
		Employee employeeById = employeeService.getEmployeeById(id);
		
		Map<String,Employee> map = new HashMap<>();
		
		map.put("Employee", employeeById);
		
		return map;
	}
	
	@GetMapping("/get/{fileName}")
	public ResponseEntity<?> getImage(@PathVariable String fileName) throws Exception
	{
		
		String path = "D:\\mohit\\RESUME\\EmployeeProfiles\\";
		
		File file = new File(path,fileName);
		
		Resource resource = new UrlResource(file.toURI());
		
		return ResponseEntity.ok().body(resource);
	}
	
	@PutMapping("/update/{id}")
	public Map<String, Employee> update(
			@PathVariable int id,
			@RequestParam String name,
			@RequestParam String gender,
			@RequestParam String email,
			@RequestParam String address,
			@RequestParam String mobileNo,
			@RequestParam String dep,
			@RequestParam String dob,
			@RequestParam (required = false) MultipartFile file) 
	{
		Employee e = new Employee();
		e.setAddress(address);
		e.setDep(dep);
		e.setDob(dob);
		e.setEmail(email);
		e.setGender(gender);
		e.setMobileNo(mobileNo);
		e.setName(name);
		
		Employee employee = employeeService.updateEmployee(id, e, file);
		
		Map<String, Employee> response = new HashMap<>();
		
		response.put("Employee", employee);
		
		return response;
	}
	
}
