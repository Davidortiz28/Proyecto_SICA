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
    private String nombreEmpresa;
    private String nit;
    private String telefono;
    private String email;
    private String direccion;
    private Boolean estaActiva;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
    
    /**
     * Constructor vacío
     */
    public Empresa() {
    }
    
    /**
     * Constructor con parámetros principales
     * 
     * @param nombreEmpresa Nombre de la empresa
     * @param nit NIT de la empresa
     */
    public Empresa(String nombreEmpresa, String nit) {
        this.nombreEmpresa = nombreEmpresa;
        this.nit = nit;
        this.estaActiva = true;
    }
    
    // Getters y Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getNombreEmpresa() {
        return nombreEmpresa;
    }
    
    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }
    
    public String getNit() {
        return nit;
    }
    
    public void setNit(String nit) {
        this.nit = nit;
    }
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    public Boolean getEstaActiva() {
        return estaActiva;
    }
    
    public void setEstaActiva(Boolean estaActiva) {
        this.estaActiva = estaActiva;
    }
    
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
    
    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
    
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
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
    

    
    @Override
    public String toString() {
        return "Empresa{" +
                "id=" + id +
                ", nombreEmpresa='" + nombreEmpresa + '\'' +
                ", nit='" + nit + '\'' +
                ", telefono='" + telefono + '\'' +
                ", email='" + email + '\'' +
                ", estaActiva=" + estaActiva +
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
