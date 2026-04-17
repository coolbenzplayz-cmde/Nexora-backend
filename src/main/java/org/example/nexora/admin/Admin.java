package org.example.nexora.admin;

import jakarta.persistence.*;

@Entity
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    public Admin() {}

    public Admin(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
}