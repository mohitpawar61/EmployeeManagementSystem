package com.employeeapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.employeeapp.entity.Employee;

@Repository
public interface EmployeeRepo extends JpaRepository<Employee,Integer>{

	boolean existsByEmail(String email);
}
