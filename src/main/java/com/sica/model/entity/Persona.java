package com.sica.model.entity;

import com.sica.model.enums.TipoPersona;
import java.time.LocalDateTime;

/**
 * Entidad que representa una persona en el sistema SICA.
 * Una persona puede ser un Trabajador o un Invitado.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo representa una persona con sus atributos
 * - Encapsulación: Atributos privados con getters/setters
 * 
 * @author SICA Team
 * @version 1.0
 */
public class Persona {
    private Integer id;
    private String nombre;
    private String documentoIdentidad;
    private Empresa empresa;
    private TipoPersona tipoPersona;
    private PersonaEstadoAcceso estadoAcceso;
    private String urlFoto;
    private String telefono;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Constructor vacío
     */
    public Persona() {
    }
    
    /**
     * Constructor con parámetros principales
     * 
     * @param nombre Nombre completo de la persona
     * @param documentoIdentidad Documento de identidad (único)
     * @param tipoPersona Tipo de persona (Trabajador o Invitado)
     * @param estadoAcceso Estado de acceso actual
     */
    public Persona(String nombre, String documentoIdentidad, TipoPersona tipoPersona, PersonaEstadoAcceso estadoAcceso) {
        this.nombre = nombre;
        this.documentoIdentidad = documentoIdentidad;
        this.tipoPersona = tipoPersona;
        this.estadoAcceso = estadoAcceso;
    }
    
    /**
     * Constructor completo
     * 
     * @param id ID de la persona
     * @param nombre Nombre completo
     * @param documentoIdentidad Documento de identidad
     * @param empresa Empresa asociada
     * @param tipoPersona Tipo de persona
     * @param estadoAcceso Estado de acceso
     * @param urlFoto URL de la foto
     * @param telefono Teléfono de contacto
     * @param email Email de contacto
     */
    public Persona(Integer id, String nombre, String documentoIdentidad, Empresa empresa,
                   TipoPersona tipoPersona, PersonaEstadoAcceso estadoAcceso,
                   String urlFoto, String telefono, String email) {
        this.id = id;
        this.nombre = nombre;
        this.documentoIdentidad = documentoIdentidad;
        this.empresa = empresa;
        this.tipoPersona = tipoPersona;
        this.estadoAcceso = estadoAcceso;
        this.urlFoto = urlFoto;
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
    
    public String getDocumentoIdentidad() {
        return documentoIdentidad;
    }
    
    public void setDocumentoIdentidad(String documentoIdentidad) {
        this.documentoIdentidad = documentoIdentidad;
    }
    
    public Empresa getEmpresa() {
        return empresa;
    }
    
    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }
    
    public TipoPersona getTipoPersona() {
        return tipoPersona;
    }
    
    public void setTipoPersona(TipoPersona tipoPersona) {
        this.tipoPersona = tipoPersona;
    }
    
    public PersonaEstadoAcceso getEstadoAcceso() {
        return estadoAcceso;
    }
    
    public void setEstadoAcceso(PersonaEstadoAcceso estadoAcceso) {
        this.estadoAcceso = estadoAcceso;
    }
    
    public String getUrlFoto() {
        return urlFoto;
    }
    
    public void setUrlFoto(String urlFoto) {
        this.urlFoto = urlFoto;
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
    
    // Métodos de negocio
    
    /**
     * Verifica si la persona es un trabajador
     * 
     * @return true si es trabajador, false en caso contrario
     */
    public boolean esTrabajador() {
        return tipoPersona == TipoPersona.TRABAJADOR;
    }
    
    /**
     * Verifica si la persona es un invitado
     * 
     * @return true si es invitado, false en caso contrario
     */
    public boolean esInvitado() {
        return tipoPersona == TipoPersona.INVITADO;
    }
    
    /**
     * Verifica si la persona puede ingresar al complejo
     * 
     * @return true si su estado de acceso permite el ingreso, false en caso contrario
     */
    public boolean puedeIngresar() {
        return estadoAcceso != null && estadoAcceso.permiteIngreso();
    }
    
    /**
     * Verifica si la persona está bloqueada
     * 
     * @return true si está bloqueada, false en caso contrario
     */
    public boolean estaBloqueada() {
        return estadoAcceso != null && estadoAcceso.estaBloqueado();
    }
    
    /**
     * Bloquea el acceso de la persona
     * 
     * @param estadoBloqueado Estado de acceso bloqueado
     */
    public void bloquear(PersonaEstadoAcceso estadoBloqueado) {
        this.estadoAcceso = estadoBloqueado;
    }
    
    /**
     * Desbloquea el acceso de la persona
     * 
     * @param estadoActivo Estado de acceso activo
     */
    public void desbloquear(PersonaEstadoAcceso estadoActivo) {
        this.estadoAcceso = estadoActivo;
    }
    
    @Override
    public String toString() {
        return "Persona{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", documentoIdentidad='" + documentoIdentidad + '\'' +
                ", empresa=" + (empresa != null ? empresa.getNombre() : "Sin empresa") +
                ", tipoPersona=" + tipoPersona +
                ", estadoAcceso=" + (estadoAcceso != null ? estadoAcceso.getNombreEstado() : "Sin estado") +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Persona persona = (Persona) o;
        return id != null && id.equals(persona.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
