package com.aiknow.demo.modules.company.domain;

import com.aiknow.demo.modules.user.domain.Employee;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "company_seq")
    @SequenceGenerator(name = "company_seq", sequenceName = "company_seq", allocationSize = 1)
    private Long Id;
    private String Name;
    @OneToMany
    private Set<Employee> Employees;
}
