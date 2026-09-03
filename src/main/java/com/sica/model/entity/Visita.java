package com.sica.model.entity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Entidad que representa una visita al complejo.
 * Registra entradas, salidas y estados de las visitas de personas.
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo representa una visita con sus atributos
 * - Encapsulación: Atributos privados con getters/setters
 * 
 * @author SICA Team
 * @version 1.0
 */
public class Visita {
    private Integer id;
    private Persona persona;
    private LocalDateTime fechaEntrada;
    private LocalDateTime fechaSalida;
    private VisitaEstado estado;
    private String vehiculoPlaca;
    private Usuario visitaAprobadaPor;
    private String motivoVisita;
    private String observaciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Constructor vacío
     */
    public Visita() {
    }
    
    /**
     * Constructor con parámetros principales
     * 
     * @param persona Persona que realiza la visita
     * @param estado Estado de la visita
     */
    public Visita(Persona persona, VisitaEstado estado) {
        this.persona = persona;
        this.estado = estado;
    }
    
    /**
     * Constructor completo
     * 
     * @param id ID de la visita
     * @param persona Persona que realiza la visita
     * @param fechaEntrada Fecha y hora de entrada
     * @param fechaSalida Fecha y hora de salida
     * @param estado Estado de la visita
     * @param vehiculoPlaca Placa del vehículo
     * @param visitaAprobadaPor Usuario que aprobó la visita
     * @param motivoVisita Motivo de la visita
     * @param observaciones Observaciones adicionales
     */
    public Visita(Integer id, Persona persona, LocalDateTime fechaEntrada, LocalDateTime fechaSalida,
                  VisitaEstado estado, String vehiculoPlaca, Usuario visitaAprobadaPor,
                  String motivoVisita, String observaciones) {
        this.id = id;
        this.persona = persona;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.estado = estado;
        this.vehiculoPlaca = vehiculoPlaca;
        this.visitaAprobadaPor = visitaAprobadaPor;
        this.motivoVisita = motivoVisita;
        this.observaciones = observaciones;
    }
    
    // Getters y Setters
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Persona getPersona() {
        return persona;
    }
    
    public void setPersona(Persona persona) {
        this.persona = persona;
    }
    
    public LocalDateTime getFechaEntrada() {
        return fechaEntrada;
    }
    
    public void setFechaEntrada(LocalDateTime fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }
    
    public LocalDateTime getFechaSalida() {
        return fechaSalida;
    }
    
    public void setFechaSalida(LocalDateTime fechaSalida) {
        this.fechaSalida = fechaSalida;
    }
    
    public VisitaEstado getEstado() {
        return estado;
    }
    
    public void setEstado(VisitaEstado estado) {
        this.estado = estado;
    }
    
    public String getVehiculoPlaca() {
        return vehiculoPlaca;
    }
    
    public void setVehiculoPlaca(String vehiculoPlaca) {
        this.vehiculoPlaca = vehiculoPlaca;
    }
    
    public Usuario getVisitaAprobadaPor() {
        return visitaAprobadaPor;
    }
    
    public void setVisitaAprobadaPor(Usuario visitaAprobadaPor) {
        this.visitaAprobadaPor = visitaAprobadaPor;
    }
    
    public String getMotivoVisita() {
        return motivoVisita;
    }
    
    public void setMotivoVisita(String motivoVisita) {
        this.motivoVisita = motivoVisita;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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
     * Verifica si la visita está actualmente abierta (sin fecha de salida)
     * 
     * @return true si está abierta, false en caso contrario
     */
    public boolean estaAbierta() {
        return fechaSalida == null && fechaEntrada != null;
    }
    
    /**
     * Verifica si la persona está actualmente dentro del complejo
     * 
     * @return true si está dentro, false en caso contrario
     */
    public boolean estaDentro() {
        return estado != null && estado.estaDentro();
    }
    
    /**
     * Calcula la duración de la visita en minutos
     * 
     * @return Minutos de permanencia (si está dentro, hasta ahora; si salió, total)
     */
    public long calcularDuracionMinutos() {
        if (fechaEntrada == null) {
            return 0;
        }
        LocalDateTime fin = fechaSalida != null ? fechaSalida : LocalDateTime.now();
        return ChronoUnit.MINUTES.between(fechaEntrada, fin);
    }
    
    /**
     * Registra el check-in (entrada)
     * 
     * @param estadoDentro Estado "Dentro"
     */
    public void registrarCheckIn(VisitaEstado estadoDentro) {
        this.fechaEntrada = LocalDateTime.now();
        this.estado = estadoDentro;
    }
    
    /**
     * Registra el check-out (salida)
     * 
     * @param estadoFuera Estado "Fuera"
     */
    public void registrarCheckOut(VisitaEstado estadoFuera) {
        this.fechaSalida = LocalDateTime.now();
        this.estado = estadoFuera;
    }
    
    /**
     * Aprueba la visita
     * 
     * @param estadoAprobado Estado "Aprobado"
     * @param usuarioAprobador Usuario que aprueba
     */
    public void aprobar(VisitaEstado estadoAprobado, Usuario usuarioAprobador) {
        this.estado = estadoAprobado;
        this.visitaAprobadaPor = usuarioAprobador;
    }
    
    /**
     * Rechaza la visita
     * 
     * @param estadoRechazado Estado "Rechazado"
     * @param usuarioRechazador Usuario que rechaza
     */
    public void rechazar(VisitaEstado estadoRechazado, Usuario usuarioRechazador) {
        this.estado = estadoRechazado;
        this.visitaAprobadaPor = usuarioRechazador;
    }
    
    /**
     * Cierra la visita por salida olvidada
     * 
     * @param estadoCerrado Estado "Cerrada por Sistema (Salida Olvidada)"
     * @param fechaSalidaEstimada Fecha de salida estimada
     */
    public void cerrarPorSalidaOlvidada(VisitaEstado estadoCerrado, LocalDateTime fechaSalidaEstimada) {
        this.fechaSalida = fechaSalidaEstimada;
        this.estado = estadoCerrado;
    }
    
    @Override
    public String toString() {
        return "Visita{" +
                "id=" + id +
                ", persona=" + (persona != null ? persona.getNombre() : "Sin persona") +
                ", fechaEntrada=" + fechaEntrada +
                ", fechaSalida=" + fechaSalida +
                ", estado=" + (estado != null ? estado.getNombreEstado() : "Sin estado") +
                ", motivoVisita='" + motivoVisita + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Visita visita = (Visita) o;
        return id != null && id.equals(visita.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
