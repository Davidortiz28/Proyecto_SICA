package com.sica.service;

import com.sica.exception.PermisoDenegadoException;
import com.sica.exception.SicaException;
import com.sica.model.entity.Incidente;
import com.sica.model.entity.Persona;
import com.sica.model.entity.Visita;
import com.sica.repository.IncidenteRepository;
import com.sica.repository.PersonaRepository;
import com.sica.repository.VisitaRepository;
import com.sica.security.SessionManager;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de generación de reportes.
 * 
 * Responsabilidades:
 * - Generar reportes de visitas
 * - Generar reportes de incidentes
 * - Generar reportes de personas
 * - Estadísticas del sistema
 * - Verificación de permisos RBAC
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo genera reportes
 * - Open/Closed: Fácil agregar nuevos tipos de reportes
 * 
 * @author SICA Team
 * @version 1.0
 */
public class ReporteService {
    
    private final VisitaRepository visitaRepository;
    private final PersonaRepository personaRepository;
    private final IncidenteRepository incidenteRepository;
    private final AuthorizationService authorizationService;
    private final SessionManager sessionManager;
    
    private static final DateTimeFormatter FORMATO_FECHA = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public ReporteService() throws SQLException {
        this.visitaRepository = new VisitaRepository();
        this.personaRepository = new PersonaRepository();
        this.incidenteRepository = new IncidenteRepository();
        this.authorizationService = new AuthorizationService();
        this.sessionManager = SessionManager.getInstance();
    }
    
    /**
     * Genera reporte de personas actualmente dentro del complejo.
     * 
     * Permisos: requiere "generar_reporte"
     * 
     * @return String con el reporte formateado
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error
     */
    public String reportePersonasDentro() throws SicaException {
        authorizationService.requirePermiso("generar_reporte");
        
        try {
            List<Visita> visitasDentro = visitaRepository.findPersonasDentro();
            
            StringBuilder reporte = new StringBuilder();
            reporte.append("═══════════════════════════════════════════════════════════════\n");
            reporte.append("           REPORTE: PERSONAS ACTUALMENTE DENTRO\n");
            reporte.append("═══════════════════════════════════════════════════════════════\n");
            reporte.append("Fecha: ").append(LocalDateTime.now().format(FORMATO_FECHA)).append("\n");
            reporte.append("Usuario: ").append(sessionManager.getUsuarioActual().getNombre()).append("\n");
            reporte.append("───────────────────────────────────────────────────────────────\n");
            reporte.append(String.format("Total de personas dentro: %d\n", visitasDentro.size()));
            reporte.append("───────────────────────────────────────────────────────────────\n\n");
            
            if (visitasDentro.isEmpty()) {
                reporte.append("No hay personas dentro del complejo actualmente.\n");
            } else {
                int num = 1;
                for (Visita visita : visitasDentro) {
                    Persona persona = visita.getPersona();
                    long minutosAdentro = visita.calcularDuracionMinutos();
                    
                    reporte.append(String.format("%d. %s\n", num++, persona.getNombre()));
                    reporte.append(String.format("   Documento: %s\n", persona.getDocumentoIdentidad()));
                    reporte.append(String.format("   Tipo: %s\n", persona.getTipoPersona()));
                    reporte.append(String.format("   Hora entrada: %s\n", 
                        visita.getFechaEntrada().format(FORMATO_FECHA)));
                    reporte.append(String.format("   Tiempo dentro: %d minutos\n", minutosAdentro));
                    if (visita.getVehiculoPlaca() != null && !visita.getVehiculoPlaca().isEmpty()) {
                        reporte.append(String.format("   Vehículo: %s\n", visita.getVehiculoPlaca()));
                    }
                    reporte.append("\n");
                }
            }
            
            reporte.append("═══════════════════════════════════════════════════════════════\n");
            
            return reporte.toString();
            
        } catch (SQLException e) {
            throw new SicaException("Error al generar reporte: " + e.getMessage(), e);
        }
    }
    
    /**
     * Genera reporte de historial de visitas en un rango de fechas.
     * 
     * Permisos: requiere "generar_reporte"
     * 
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return String con el reporte formateado
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error
     */
    public String reporteHistorialVisitas(LocalDateTime fechaInicio, LocalDateTime fechaFin) 
            throws SicaException {
        authorizationService.requirePermiso("generar_reporte");
        
        try {
            // Obtener todas las visitas y filtrar por fecha
            List<Visita> todasVisitas = visitaRepository.findAll();
            List<Visita> visitasFiltradas = todasVisitas.stream()
                .filter(v -> v.getCreatedAt() != null)
                .filter(v -> !v.getCreatedAt().isBefore(fechaInicio) && 
                            !v.getCreatedAt().isAfter(fechaFin))
                .collect(Collectors.toList());
            
            StringBuilder reporte = new StringBuilder();
            reporte.append("═══════════════════════════════════════════════════════════════\n");
            reporte.append("              REPORTE: HISTORIAL DE VISITAS\n");
            reporte.append("═══════════════════════════════════════════════════════════════\n");
            reporte.append("Período: ").append(fechaInicio.format(FORMATO_FECHA))
                   .append(" - ").append(fechaFin.format(FORMATO_FECHA)).append("\n");
            reporte.append("Fecha reporte: ").append(LocalDateTime.now().format(FORMATO_FECHA)).append("\n");
            reporte.append("Usuario: ").append(sessionManager.getUsuarioActual().getNombre()).append("\n");
            reporte.append("───────────────────────────────────────────────────────────────\n");
            reporte.append(String.format("Total de visitas: %d\n", visitasFiltradas.size()));
            reporte.append("───────────────────────────────────────────────────────────────\n\n");
            
            if (visitasFiltradas.isEmpty()) {
                reporte.append("No hay visitas en el período especificado.\n");
            } else {
                // Estadísticas
                long visitasCompletadas = visitasFiltradas.stream()
                    .filter(v -> v.getFechaSalida() != null)
                    .count();
                
                reporte.append("Estadísticas:\n");
                reporte.append(String.format("  - Visitas completadas: %d\n", visitasCompletadas));
                reporte.append(String.format("  - Visitas en curso: %d\n\n", 
                    visitasFiltradas.size() - visitasCompletadas));
                
                // Detalle de visitas
                int num = 1;
                for (Visita visita : visitasFiltradas) {
                    Persona persona = visita.getPersona();
                    
                    reporte.append(String.format("%d. %s (%s)\n", 
                        num++, persona.getNombre(), persona.getDocumentoIdentidad()));
                    reporte.append(String.format("   Estado: %s\n", visita.getEstado().getNombreEstado()));
                    reporte.append(String.format("   Entrada: %s\n", 
                        visita.getFechaEntrada() != null ? 
                        visita.getFechaEntrada().format(FORMATO_FECHA) : "N/A"));
                    reporte.append(String.format("   Salida: %s\n", 
                        visita.getFechaSalida() != null ? 
                        visita.getFechaSalida().format(FORMATO_FECHA) : "En curso"));
                    
                    if (visita.getFechaSalida() != null) {
                        reporte.append(String.format("   Duración: %d minutos\n", 
                            visita.calcularDuracionMinutos()));
                    }
                    reporte.append("\n");
                }
            }
            
            reporte.append("═══════════════════════════════════════════════════════════════\n");
            
            return reporte.toString();
            
        } catch (SQLException e) {
            throw new SicaException("Error al generar reporte: " + e.getMessage(), e);
        }
    }
    
    /**
     * Genera reporte de incidentes.
     * 
     * Permisos: requiere "generar_reporte" o "consultar_bitacora"
     * 
     * @return String con el reporte formateado
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error
     */
    public String reporteIncidentes() throws SicaException {
        if (!authorizationService.tieneAlgunoDeEstosPermisos("generar_reporte", "consultar_bitacora")) {
            throw new PermisoDenegadoException("generar_reporte", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            List<Incidente> incidentes = incidenteRepository.findAll();
            
            StringBuilder reporte = new StringBuilder();
            reporte.append("═══════════════════════════════════════════════════════════════\n");
            reporte.append("                 REPORTE: INCIDENTES\n");
            reporte.append("═══════════════════════════════════════════════════════════════\n");
            reporte.append("Fecha: ").append(LocalDateTime.now().format(FORMATO_FECHA)).append("\n");
            reporte.append("Usuario: ").append(sessionManager.getUsuarioActual().getNombre()).append("\n");
            reporte.append("───────────────────────────────────────────────────────────────\n");
            reporte.append(String.format("Total de incidentes: %d\n", incidentes.size()));
            reporte.append("───────────────────────────────────────────────────────────────\n\n");
            
            if (incidentes.isEmpty()) {
                reporte.append("No hay incidentes registrados.\n");
            } else {
                // Estadísticas por gravedad
                Map<String, Long> porGravedad = incidentes.stream()
                    .collect(Collectors.groupingBy(Incidente::getGravedad, Collectors.counting()));
                
                reporte.append("Estadísticas por Gravedad:\n");
                porGravedad.forEach((gravedad, cantidad) -> 
                    reporte.append(String.format("  - %s: %d\n", gravedad, cantidad)));
                
                // Estadísticas por estado
                Map<String, Long> porEstado = incidentes.stream()
                    .collect(Collectors.groupingBy(Incidente::getEstadoResolucion, Collectors.counting()));
                
                reporte.append("\nEstadísticas por Estado:\n");
                porEstado.forEach((estado, cantidad) -> 
                    reporte.append(String.format("  - %s: %d\n", estado, cantidad)));
                
                reporte.append("\n───────────────────────────────────────────────────────────────\n\n");
                
                // Detalle de incidentes
                int num = 1;
                for (Incidente incidente : incidentes) {
                    reporte.append(String.format("%d. [%s] %s\n", 
                        num++, incidente.getGravedad(), 
                        incidente.getDescripcion().substring(0, 
                            Math.min(60, incidente.getDescripcion().length()))));
                    reporte.append(String.format("   Fecha: %s\n", 
                        incidente.getFechaIncidente().format(FORMATO_FECHA)));
                    reporte.append(String.format("   Estado: %s\n", incidente.getEstadoResolucion()));
                    reporte.append(String.format("   Reportado por: %s\n", 
                        incidente.getReportadoPor().getNombre()));
                    reporte.append("\n");
                }
            }
            
            reporte.append("═══════════════════════════════════════════════════════════════\n");
            
            return reporte.toString();
            
        } catch (SQLException e) {
            throw new SicaException("Error al generar reporte: " + e.getMessage(), e);
        }
    }
    
    /**
     * Genera reporte de personas bloqueadas.
     * 
     * Permisos: requiere "generar_reporte"
     * 
     * @return String con el reporte formateado
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error
     */
    public String reportePersonasBloqueadas() throws SicaException {
        authorizationService.requirePermiso("generar_reporte");
        
        try {
            // Estado "Con Prohibición de Ingreso" tiene ID 2
            List<Persona> personasBloqueadas = personaRepository.findByEstadoAcceso(2);
            
            StringBuilder reporte = new StringBuilder();
            reporte.append("═══════════════════════════════════════════════════════════════\n");
            reporte.append("            REPORTE: PERSONAS BLOQUEADAS\n");
            reporte.append("═══════════════════════════════════════════════════════════════\n");
            reporte.append("Fecha: ").append(LocalDateTime.now().format(FORMATO_FECHA)).append("\n");
            reporte.append("Usuario: ").append(sessionManager.getUsuarioActual().getNombre()).append("\n");
            reporte.append("───────────────────────────────────────────────────────────────\n");
            reporte.append(String.format("Total de personas bloqueadas: %d\n", 
                personasBloqueadas.size()));
            reporte.append("───────────────────────────────────────────────────────────────\n\n");
            
            if (personasBloqueadas.isEmpty()) {
                reporte.append("No hay personas bloqueadas actualmente.\n");
            } else {
                int num = 1;
                for (Persona persona : personasBloqueadas) {
                    reporte.append(String.format("%d. %s\n", num++, persona.getNombre()));
                    reporte.append(String.format("   Documento: %s\n", persona.getDocumentoIdentidad()));
                    reporte.append(String.format("   Tipo: %s\n", persona.getTipoPersona()));
                    if (persona.getEmpresa() != null) {
                        reporte.append(String.format("   Empresa: %s\n", 
                            persona.getEmpresa().getNombreEmpresa()));
                    }
                    reporte.append(String.format("   Estado: %s\n", 
                        persona.getEstadoAcceso().getNombreEstado()));
                    reporte.append("\n");
                }
            }
            
            reporte.append("═══════════════════════════════════════════════════════════════\n");
            
            return reporte.toString();
            
        } catch (SQLException e) {
            throw new SicaException("Error al generar reporte: " + e.getMessage(), e);
        }
    }
    
    /**
     * Genera estadísticas generales del sistema.
     * 
     * Permisos: requiere "generar_reporte"
     * 
     * @return String con las estadísticas formateadas
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error
     */
    public String estadisticasGenerales() throws SicaException {
        authorizationService.requirePermiso("generar_reporte");
        
        try {
            List<Persona> todasPersonas = personaRepository.findAll();
            List<Visita> todasVisitas = visitaRepository.findAll();
            List<Visita> personasDentro = visitaRepository.findPersonasDentro();
            List<Incidente> todosIncidentes = incidenteRepository.findAll();
            
            StringBuilder reporte = new StringBuilder();
            reporte.append("═══════════════════════════════════════════════════════════════\n");
            reporte.append("            ESTADÍSTICAS GENERALES DEL SISTEMA\n");
            reporte.append("═══════════════════════════════════════════════════════════════\n");
            reporte.append("Fecha: ").append(LocalDateTime.now().format(FORMATO_FECHA)).append("\n");
            reporte.append("Usuario: ").append(sessionManager.getUsuarioActual().getNombre()).append("\n");
            reporte.append("───────────────────────────────────────────────────────────────\n\n");
            
            // Personas
            reporte.append("PERSONAS:\n");
            reporte.append(String.format("  Total registradas: %d\n", todasPersonas.size()));
            long trabajadores = todasPersonas.stream()
                .filter(p -> p.getTipoPersona().name().equals("TRABAJADOR"))
                .count();
            reporte.append(String.format("  Trabajadores: %d\n", trabajadores));
            reporte.append(String.format("  Invitados: %d\n\n", todasPersonas.size() - trabajadores));
            
            // Visitas
            reporte.append("VISITAS:\n");
            reporte.append(String.format("  Total registradas: %d\n", todasVisitas.size()));
            reporte.append(String.format("  Personas actualmente dentro: %d\n", personasDentro.size()));
            long visitasCompletadas = todasVisitas.stream()
                .filter(v -> v.getFechaSalida() != null)
                .count();
            reporte.append(String.format("  Visitas completadas: %d\n\n", visitasCompletadas));
            
            // Incidentes
            reporte.append("INCIDENTES:\n");
            reporte.append(String.format("  Total registrados: %d\n", todosIncidentes.size()));
            long pendientes = todosIncidentes.stream()
                .filter(i -> "Pendiente".equals(i.getEstadoResolucion()))
                .count();
            reporte.append(String.format("  Pendientes de resolución: %d\n", pendientes));
            long criticos = todosIncidentes.stream()
                .filter(i -> "Crítica".equals(i.getGravedad()))
                .count();
            reporte.append(String.format("  Críticos: %d\n", criticos));
            
            reporte.append("\n═══════════════════════════════════════════════════════════════\n");
            
            return reporte.toString();
            
        } catch (SQLException e) {
            throw new SicaException("Error al generar estadísticas: " + e.getMessage(), e);
        }
    }
}
