package com.aiknow.demo.modules.user.domain;

import com.aiknow.demo.modules.user.domain.enums.UserRole;
import jakarta.persistence.Id;

import java.util.Objects;

public abstract class AbstractUserEntity {

    @Id
    public Long Id;
    public String FirstName;
    public String LastName;
    public UserRole Role;
    public String Email;
    public String Password;

    public String getFullName(){
        return FirstName + " " + LastName;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public UserRole getRole() {
        return Role;
    }

    public void setRole(UserRole role) {
        Role = role;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AbstractUserEntity that = (AbstractUserEntity) o;
        return Objects.equals(Id, that.Id) && Objects.equals(FirstName, that.FirstName) && Objects.equals(LastName, that.LastName) && Role == that.Role && Objects.equals(Email, that.Email) && Objects.equals(Password, that.Password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Id, FirstName, LastName, Role, Email, Password);
    }
}
