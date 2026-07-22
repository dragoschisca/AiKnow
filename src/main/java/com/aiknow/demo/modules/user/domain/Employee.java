package com.aiknow.demo.modules.user.domain;

import com.aiknow.demo.modules.company.domain.Company;
import com.aiknow.demo.modules.user.domain.enums.EmployeePosition;
import jakarta.persistence.*;

@Entity
@Table(name = "employees")
public class Employee extends AbstractUserEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employee_seq")
    @SequenceGenerator(name = "employee_seq", sequenceName = "employee_seq", allocationSize = 1)
    private Long id;
    @OneToOne
    private Company company;
    private EmployeePosition Position;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
