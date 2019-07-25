package com.internship.tmontica.user;

import lombok.Data;

import javax.validation.constraints.*;
import java.util.Date;

@Data
public class User {

    @Pattern(regexp="^[a-z0-9]{6,19}$")
    private String id;
    @NotNull
    private String name;
    @NotNull
    @Email
    private String email;
    @NotNull
    private Date birthDate;
    @NotNull
    @Pattern(regexp="^(?=.*[a-zA-Z])(?=.*[!@#$%^*+=-])(?=.*[0-9]).{6,19}")
    private String password;
    private String role;
    private Date createdDate;
    @Min(0)
    private int point;
}