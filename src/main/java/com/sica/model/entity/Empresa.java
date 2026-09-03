package com.sica.model.entity;

import java.time.LocalDateTime;

/**
 * Entidad que representa una empresa dentro del Complejo Zona Acme.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo representa una empresa con sus atributos
 * - Encapsulación: Atributos privados con getters/setters
 * 
 * @author SICA Team
 * @version 1.0
 */
public class Empresa {
    private Integer id;
    private String nombre;
    private String contactoPrincipal;
    private String telefono;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Constructor vacío
     */
    public Empresa() {
    }
    
    /**
     * Constructor con parámetros principales
     * 
     * @param nombre Nombre de la empresa
     * @param contactoPrincipal Nombre del contacto principal
     */
    public Empresa(String nombre, String contactoPrincipal) {
        this.nombre = nombre;
        this.contactoPrincipal = contactoPrincipal;
    }
    
    /**
     * Constructor completo
     * 
     * @param id ID de la empresa
     * @param nombre Nombre de la empresa
     * @param contactoPrincipal Nombre del contacto principal
     * @param telefono Teléfono de contacto
     * @param email Email de contacto
     */
    public Empresa(Integer id, String nombre, String contactoPrincipal, String telefono, String email) {
        this.id = id;
        this.nombre = nombre;
        this.contactoPrincipal = contactoPrincipal;
        this.telefono = telefono;
        this.email = email;
    }
    
    // Getters y Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getContactoPrincipal() {
        return contactoPrincipal;
    }
    
    public void setContactoPrincipal(String contactoPrincipal) {
        this.contactoPrincipal = contactoPrincipal;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "Empresa{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", contactoPrincipal='" + contactoPrincipal + '\'' +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Empresa empresa = (Empresa) o;
        return id != null && id.equals(empresa.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
