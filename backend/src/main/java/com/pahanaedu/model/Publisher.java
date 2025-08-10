package com.pahanaedu.model;

import java.sql.Timestamp;

/**
 * Publisher model representing a book publisher
 */
public class Publisher {
    private int id;
    private String name;
    private String contactPerson;
    private String telephone;
    private String email;
    private String address;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Publisher() {
    }

    public Publisher(String name, String contactPerson, String telephone, String email, String address) {
        this.name = name;
        this.contactPerson = contactPerson;
        this.telephone = telephone;
        this.email = email;
        this.address = address;
    }

    public Publisher(int id, String name, String contactPerson, String telephone, String email,
                    String address, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.name = name;
        this.contactPerson = contactPerson;
        this.telephone = telephone;
        this.email = email;
        this.address = address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Publisher{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", telephone='" + telephone + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
