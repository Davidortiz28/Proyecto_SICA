package com.sica.model.entity;

import com.sica.model.enums.AccionAuditoria;
import java.time.LocalDateTime;

/**
 * Entidad que representa un registro de auditoría en el sistema.
 * Registra todas las operaciones críticas realizadas por los usuarios.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo representa un registro de auditoría
 * - Encapsulación: Atributos privados con getters/setters
 * 
 * @author SICA Team
 * @version 1.0
 */
public class BitacoraAuditoria {
    private Long id;
    private Usuario usuario;
    private LocalDateTime fechaHora;
    private AccionAuditoria accionRealizada;
    private String tablaAfectada;
    private Integer registroIdAfectado;
    private String detalles;
    private String ipAddress;
    
    /**
     * Constructor vacío
     */
    public BitacoraAuditoria() {
        this.fechaHora = LocalDateTime.now();
    }
    
    /**
     * Constructor con parámetros principales
     * 
     * @param usuario Usuario que realizó la acción
     * @param accionRealizada Acción realizada
     * @param detalles Detalles adicionales
     */
    public BitacoraAuditoria(Usuario usuario, AccionAuditoria accionRealizada, String detalles) {
        this();
        this.usuario = usuario;
        this.accionRealizada = accionRealizada;
        this.detalles = detalles;
    }
    
    /**
     * Constructor con tabla y registro afectado
     * 
     * @param usuario Usuario que realizó la acción
     * @param accionRealizada Acción realizada
     * @param tablaAfectada Tabla afectada
     * @param registroIdAfectado ID del registro afectado
     * @param detalles Detalles adicionales
     */
    public BitacoraAuditoria(Usuario usuario, AccionAuditoria accionRealizada,
                             String tablaAfectada, Integer registroIdAfectado, String detalles) {
        this(usuario, accionRealizada, detalles);
        this.tablaAfectada = tablaAfectada;
        this.registroIdAfectado = registroIdAfectado;
    }
    
    /**
     * Constructor completo
     * 
     * @param id ID del registro de auditoría
     * @param usuario Usuario que realizó la acción
     * @param fechaHora Fecha y hora de la acción
     * @param accionRealizada Acción realizada
     * @param tablaAfectada Tabla afectada
     * @param registroIdAfectado ID del registro afectado
     * @param detalles Detalles adicionales
     * @param ipAddress Dirección IP desde donde se realizó la acción
     */
    public BitacoraAuditoria(Long id, Usuario usuario, LocalDateTime fechaHora,
                             AccionAuditoria accionRealizada, String tablaAfectada,
                             Integer registroIdAfectado, String detalles, String ipAddress) {
        this.id = id;
        this.usuario = usuario;
        this.fechaHora = fechaHora;
        this.accionRealizada = accionRealizada;
        this.tablaAfectada = tablaAfectada;
        this.registroIdAfectado = registroIdAfectado;
        this.detalles = detalles;
        this.ipAddress = ipAddress;
    }
    
    // Getters y Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
    
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
    
    public AccionAuditoria getAccionRealizada() {
        return accionRealizada;
    }
    
    public void setAccionRealizada(AccionAuditoria accionRealizada) {
        this.accionRealizada = accionRealizada;
    }
    
    public String getTablaAfectada() {
        return tablaAfectada;
    }
    
    public void setTablaAfectada(String tablaAfectada) {
        this.tablaAfectada = tablaAfectada;
    }
    
    public Integer getRegistroIdAfectado() {
        return registroIdAfectado;
    }
    
    public void setRegistroIdAfectado(Integer registroIdAfectado) {
        this.registroIdAfectado = registroIdAfectado;
    }
    
    public String getDetalles() {
        return detalles;
    }
    
    public void setDetalles(String detalles) {
        this.detalles = detalles;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    // Métodos de negocio
    
    /**
     * Verifica si la acción registrada es crítica
     * 
     * @return true si es crítica, false en caso contrario
     */
    public boolean esAccionCritica() {
        return accionRealizada != null && accionRealizada.esCritica();
    }
    
    /**
     * Verifica si la auditoría tiene tabla afectada
     * 
     * @return true si tiene tabla afectada, false en caso contrario
     */
    public boolean tieneTablaAfectada() {
        return tablaAfectada != null && !tablaAfectada.isEmpty();
    }
    
    /**
     * Construye un resumen del registro de auditoría
     * 
     * @return Resumen legible del registro
     */
    public String obtenerResumen() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(fechaHora).append("] ");
        sb.append(usuario != null ? usuario.getNombre() : "Usuario desconocido");
        sb.append(" - ").append(accionRealizada != null ? accionRealizada.getDescripcion() : "Acción desconocida");
        if (tieneTablaAfectada()) {
            sb.append(" en ").append(tablaAfectada);
            if (registroIdAfectado != null) {
                sb.append(" (ID: ").append(registroIdAfectado).append(")");
            }
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "BitacoraAuditoria{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getNombre() : "Sin usuario") +
                ", fechaHora=" + fechaHora +
                ", accionRealizada=" + accionRealizada +
                ", tablaAfectada='" + tablaAfectada + '\'' +
                ", registroIdAfectado=" + registroIdAfectado +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BitacoraAuditoria that = (BitacoraAuditoria) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
