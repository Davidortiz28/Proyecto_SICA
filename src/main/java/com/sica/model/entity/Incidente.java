package com.sica.model.entity;

import java.time.LocalDateTime;

/**
 * Entidad que representa un incidente de seguridad en el complejo.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo representa un incidente con sus atributos
 * - Encapsulación: Atributos privados con getters/setters
 * 
 * @author SICA Team
 * @version 1.0
 */
public class Incidente {
    private Integer id;
    private Visita visita;
    private Usuario reportadoPor;
    private LocalDateTime fecha;
    private String descripcion;
    private String tipoIncidente;
    private NivelGravedad nivelGravedad;
    private String estadoResolucion;  // Pendiente, En Proceso, Resuelto
    private LocalDateTime createdAt;
    
    /**
     * Enumeración para el nivel de gravedad del incidente
     */
    public enum NivelGravedad {
        BAJO("Bajo"),
        MEDIO("Medio"),
        ALTO("Alto"),
        CRITICO("Crítico");
        
        private final String descripcion;
        
        NivelGravedad(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
        
        @Override
        public String toString() {
            return descripcion;
        }
    }
    
    /**
     * Constructor vacío
     */
    public Incidente() {
        this.nivelGravedad = NivelGravedad.MEDIO;
    }
    
    /**
     * Constructor con parámetros principales
     * 
     * @param reportadoPor Usuario que reporta el incidente
     * @param fecha Fecha y hora del incidente
     * @param descripcion Descripción detallada
     */
    public Incidente(Usuario reportadoPor, LocalDateTime fecha, String descripcion) {
        this();
        this.reportadoPor = reportadoPor;
        this.fecha = fecha;
        this.descripcion = descripcion;
    }
    
    /**
     * Constructor completo
     * 
     * @param id ID del incidente
     * @param visita Visita relacionada (opcional)
     * @param reportadoPor Usuario que reporta
     * @param fecha Fecha y hora
     * @param descripcion Descripción
     * @param tipoIncidente Tipo de incidente
     * @param nivelGravedad Nivel de gravedad
     */
    public Incidente(Integer id, Visita visita, Usuario reportadoPor, LocalDateTime fecha,
                     String descripcion, String tipoIncidente, NivelGravedad nivelGravedad) {
        this.id = id;
        this.visita = visita;
        this.reportadoPor = reportadoPor;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.tipoIncidente = tipoIncidente;
        this.nivelGravedad = nivelGravedad;
    }
    
    // Getters y Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Visita getVisita() {
        return visita;
    }
    
    public void setVisita(Visita visita) {
        this.visita = visita;
    }
    
    public Usuario getReportadoPor() {
        return reportadoPor;
    }
    
    public void setReportadoPor(Usuario reportadoPor) {
        this.reportadoPor = reportadoPor;
    }
    
    public LocalDateTime getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getTipoIncidente() {
        return tipoIncidente;
    }
    
    public void setTipoIncidente(String tipoIncidente) {
        this.tipoIncidente = tipoIncidente;
    }
    
    public NivelGravedad getNivelGravedad() {
        return nivelGravedad;
    }
    
    public void setNivelGravedad(NivelGravedad nivelGravedad) {
        this.nivelGravedad = nivelGravedad;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Métodos de negocio
    
    /**
     * Obtiene la gravedad como String (para compatibilidad con IncidenteService)
     */
    public String getGravedad() {
        return nivelGravedad != null ? nivelGravedad.getDescripcion() : "Medio";
    }
    
    /**
     * Establece la gravedad desde String
     */
    public void setGravedad(String gravedad) {
        switch (gravedad) {
            case "Baja":
            case "Bajo":
                this.nivelGravedad = NivelGravedad.BAJO;
                break;
            case "Media":
            case "Medio":
                this.nivelGravedad = NivelGravedad.MEDIO;
                break;
            case "Alta":
            case "Alto":
                this.nivelGravedad = NivelGravedad.ALTO;
                break;
            case "Crítica":
            case "Crítico":
                this.nivelGravedad = NivelGravedad.CRITICO;
                break;
            default:
                this.nivelGravedad = NivelGravedad.MEDIO;
        }
    }
    
    /**
     * Obtiene la fecha del incidente
     */
    public LocalDateTime getFechaIncidente() {
        return fecha;
    }
    
    /**
     * Establece la fecha del incidente
     */
    public void setFechaIncidente(LocalDateTime fecha) {
        this.fecha = fecha;
    }
    
    /**
     * Obtiene el estado de resolución (para compatibilidad con IncidenteRepository)
     */
    public String getEstadoResolucion() {
        return estadoResolucion;
    }
    
    /**
     * Establece el estado de resolución
     */
    public void setEstadoResolucion(String estado) {
        this.estadoResolucion = estado;
    }
    
    /**
     * Verifica si el incidente tiene visita asociada
     * 
     * @return true si tiene visita, false en caso contrario
     */
    public boolean tieneVisitaAsociada() {
        return visita != null;
    }
    
    /**
     * Verifica si el incidente es crítico
     * 
     * @return true si es crítico, false en caso contrario
     */
    public boolean esCritico() {
        return nivelGravedad == NivelGravedad.CRITICO;
    }
    
    /**
     * Verifica si el incidente es de alta gravedad (Alto o Crítico)
     * 
     * @return true si es de alta gravedad, false en caso contrario
     */
    public boolean esAltaGravedad() {
        return nivelGravedad == NivelGravedad.ALTO || nivelGravedad == NivelGravedad.CRITICO;
    }
    
    @Override
    public String toString() {
        return "Incidente{" +
                "id=" + id +
                ", visita=" + (visita != null ? visita.getId() : "Sin visita") +
                ", reportadoPor=" + (reportadoPor != null ? reportadoPor.getNombre() : "Sin reportador") +
                ", fecha=" + fecha +
                ", tipoIncidente='" + tipoIncidente + '\'' +
                ", nivelGravedad=" + nivelGravedad +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Incidente incidente = (Incidente) o;
        return id != null && id.equals(incidente.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
