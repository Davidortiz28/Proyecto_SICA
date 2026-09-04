package com.sica.service;

import com.sica.exception.PermisoDenegadoException;
import com.sica.exception.SicaException;
import com.sica.model.entity.Persona;
import com.sica.model.entity.Usuario;
import com.sica.model.entity.Visita;
import com.sica.model.entity.VisitaEstado;
import com.sica.model.enums.AccionAuditoria;
import com.sica.repository.PersonaRepository;
import com.sica.repository.VisitaRepository;
import com.sica.security.SessionManager;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de lógica de negocio para la gestión de Visitas.
 * 
 * Este es el CORAZÓN del sistema SICA.
 * 
 * Responsabilidades:
 * - Check-In (registro de entrada)
 * - Check-Out (registro de salida)
 * - Aprobación/Rechazo de visitas
 * - Regularización de salidas olvidadas
 * - Verificación de permisos RBAC
 * - Auditoría de operaciones críticas
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo gestiona lógica de negocio de visitas
 * - Dependency Inversion: Depende de abstracciones
 * - Open/Closed: Abierto para extensión (nuevos estados, flujos)
 * 
 * PATRÓN DE DISEÑO: FACADE
 * El método procesarSalidaOlvidada() encapsula un flujo complejo de múltiples operaciones.
 * 
 * @author SICA Team
 * @version 1.0
 */
public class VisitaService {
    
    private final VisitaRepository visitaRepository;
    private final PersonaRepository personaRepository;
    private final AuthorizationService authorizationService;
    private final AuditoriaService auditoriaService;
    private final SessionManager sessionManager;
    
    // IDs de estados (según data.sql)
    private static final int ESTADO_DENTRO_ID = 1;
    private static final int ESTADO_FUERA_ID = 2;
    private static final int ESTADO_PENDIENTE_APROBACION_ID = 3;
    private static final int ESTADO_APROBADO_ID = 4;
    private static final int ESTADO_RECHAZADO_ID = 5;
    private static final int ESTADO_CERRADA_POR_SISTEMA_ID = 7;
    
    public VisitaService() throws SQLException {
        this.visitaRepository = new VisitaRepository();
        this.personaRepository = new PersonaRepository();
        this.authorizationService = new AuthorizationService();
        this.auditoriaService = new AuditoriaService();
        this.sessionManager = SessionManager.getInstance();
    }
    
    /**
     * Crea una nueva visita (pre-registro).
     * 
     * Permisos: requiere "registrar_visita"
     * 
     * @param visita Visita a crear
     * @return Visita creada
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error
     */
    public Visita crear(Visita visita) throws SicaException {
        authorizationService.requirePermiso("registrar_visita");
        
        try {
            validarVisita(visita);
            
            Visita nuevaVisita = visitaRepository.save(visita);
            
            auditoriaService.registrar(
                AccionAuditoria.CREACION_VISITA,
                "visitas",
                nuevaVisita.getId(),
                String.format("Visita creada: %s (Doc: %s, Estado: %s)", 
                    visita.getPersona().getNombre(),
                    visita.getPersona().getDocumentoIdentidad(),
                    visita.getEstado().getNombreEstado())
            );
            
            return nuevaVisita;
            
        } catch (SQLException e) {
            throw new SicaException("Error al crear visita: " + e.getMessage(), e);
        }
    }
    
    /**
     * Registra el CHECK-IN (entrada) de una persona.
     * 
     * Flujo:
     * 1. Verificar que la persona puede ingresar
     * 2. Verificar si tiene visita anterior abierta → Regularizar
     * 3. Verificar estado de acceso de la persona
     * 4. Si tiene visita aprobada → Check-in directo
     * 5. Si no tiene visita → Crear pendiente de aprobación
     * 
     * Permisos: requiere "registrar_check_in"
     * 
     * @param personaId ID de la persona
     * @param motivoVisita Motivo de la visita
     * @param vehiculoPlaca Placa del vehículo (opcional)
     * @return Visita con check-in registrado
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error o la persona no puede ingresar
     */
    public Visita registrarCheckIn(Integer personaId, String motivoVisita, String vehiculoPlaca) 
            throws SicaException {
        // 1. Verificar permiso
        authorizationService.requirePermiso("registrar_check_in");
        
        try {
            // 2. Buscar persona
            Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new SicaException(
                    "No se encontró la persona con ID: " + personaId));
            
            // 3. Verificar estado de acceso
            if (!persona.puedeIngresar()) {
                throw new SicaException(
                    "La persona " + persona.getNombre() + " tiene prohibición de ingreso");
            }
            
            // 4. Verificar si tiene visita anterior abierta (salida olvidada)
            Optional<Visita> visitaAbierta = visitaRepository.findUltimaVisitaAbiertaPorPersona(personaId);
            if (visitaAbierta.isPresent()) {
                // Regularizar salida olvidada
                procesarSalidaOlvidada(visitaAbierta.get());
            }
            
            // 5. Crear nueva visita con estado "Dentro"
            Visita nuevaVisita = new Visita();
            nuevaVisita.setPersona(persona);
            nuevaVisita.setFechaEntrada(LocalDateTime.now());
            nuevaVisita.setEstado(crearEstado(ESTADO_DENTRO_ID, "Dentro"));
            nuevaVisita.setMotivoVisita(motivoVisita);
            nuevaVisita.setVehiculoPlaca(vehiculoPlaca);
            
            Visita visitaGuardada = visitaRepository.save(nuevaVisita);
            
            // 6. Auditoría
            auditoriaService.registrar(
                AccionAuditoria.CHECK_IN,
                "visitas",
                visitaGuardada.getId(),
                String.format("Check-In: %s (Doc: %s) ingresó al complejo",
                    persona.getNombre(), persona.getDocumentoIdentidad())
            );
            
            return visitaGuardada;
            
        } catch (SQLException e) {
            throw new SicaException("Error al registrar check-in: " + e.getMessage(), e);
        }
    }
    
    /**
     * Registra el CHECK-OUT (salida) de una persona.
     * 
     * Permisos: requiere "registrar_check_out"
     * 
     * @param visitaId ID de la visita a cerrar
     * @param observaciones Observaciones de la salida (opcional)
     * @return Visita actualizada con check-out
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error o la visita no existe
     */
    public Visita registrarCheckOut(Integer visitaId, String observaciones) throws SicaException {
        // 1. Verificar permiso
        authorizationService.requirePermiso("registrar_check_out");
        
        try {
            // 2. Buscar visita
            Visita visita = visitaRepository.findById(visitaId)
                .orElseThrow(() -> new SicaException(
                    "No se encontró la visita con ID: " + visitaId));
            
            // 3. Validar que la visita esté abierta
            if (!visita.estaDentro()) {
                throw new SicaException(
                    "La visita ya fue cerrada o no está en estado 'Dentro'");
            }
            
            // 4. Registrar salida
            visita.setFechaSalida(LocalDateTime.now());
            visita.setEstado(crearEstado(ESTADO_FUERA_ID, "Fuera"));
            if (observaciones != null && !observaciones.trim().isEmpty()) {
                visita.setObservaciones(observaciones);
            }
            
            Visita visitaActualizada = visitaRepository.update(visita);
            
            // 5. Auditoría
            long duracionMinutos = visitaActualizada.calcularDuracionMinutos();
            auditoriaService.registrar(
                AccionAuditoria.CHECK_OUT,
                "visitas",
                visitaActualizada.getId(),
                String.format("Check-Out: %s (Doc: %s) salió del complejo. Duración: %d minutos",
                    visita.getPersona().getNombre(),
                    visita.getPersona().getDocumentoIdentidad(),
                    duracionMinutos)
            );
            
            return visitaActualizada;
            
        } catch (SQLException e) {
            throw new SicaException("Error al registrar check-out: " + e.getMessage(), e);
        }
    }
    
    /**
     * Aprueba una visita pendiente.
     * 
     * Permisos: requiere "aprobar_visita"
     * 
     * @param visitaId ID de la visita a aprobar
     * @return Visita aprobada
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error
     */
    public Visita aprobar(Integer visitaId) throws SicaException {
        // 1. Verificar permiso
        authorizationService.requirePermiso("aprobar_visita");
        
        try {
            // 2. Buscar visita
            Visita visita = visitaRepository.findById(visitaId)
                .orElseThrow(() -> new SicaException(
                    "No se encontró la visita con ID: " + visitaId));
            
            // 3. Validar que esté pendiente
            if (!visita.getEstado().requiereAprobacion()) {
                throw new SicaException(
                    "La visita no está pendiente de aprobación");
            }
            
            // 4. Aprobar
            Usuario usuarioActual = sessionManager.getUsuarioActual();
            visita.setEstado(crearEstado(ESTADO_APROBADO_ID, "Aprobado"));
            visita.setVisitaAprobadaPor(usuarioActual);
            
            Visita visitaAprobada = visitaRepository.update(visita);
            
            // 5. Auditoría
            auditoriaService.registrar(
                AccionAuditoria.APROBACION_VISITA,
                "visitas",
                visitaAprobada.getId(),
                String.format("Visita aprobada: %s (Doc: %s) aprobada por %s",
                    visita.getPersona().getNombre(),
                    visita.getPersona().getDocumentoIdentidad(),
                    usuarioActual.getNombre())
            );
            
            return visitaAprobada;
            
        } catch (SQLException e) {
            throw new SicaException("Error al aprobar visita: " + e.getMessage(), e);
        }
    }
    
    /**
     * Rechaza una visita pendiente.
     * 
     * Permisos: requiere "rechazar_visita"
     * 
     * @param visitaId ID de la visita a rechazar
     * @param motivo Motivo del rechazo
     * @return Visita rechazada
     * @throws PermisoDenegadoException si no tiene permiso
     * @throws SicaException si hay error
     */
    public Visita rechazar(Integer visitaId, String motivo) throws SicaException {
        // 1. Verificar permiso
        authorizationService.requirePermiso("rechazar_visita");
        
        try {
            // 2. Buscar visita
            Visita visita = visitaRepository.findById(visitaId)
                .orElseThrow(() -> new SicaException(
                    "No se encontró la visita con ID: " + visitaId));
            
            // 3. Validar que esté pendiente
            if (!visita.getEstado().requiereAprobacion()) {
                throw new SicaException(
                    "La visita no está pendiente de aprobación");
            }
            
            // 4. Rechazar
            Usuario usuarioActual = sessionManager.getUsuarioActual();
            visita.setEstado(crearEstado(ESTADO_RECHAZADO_ID, "Rechazado"));
            visita.setVisitaAprobadaPor(usuarioActual);
            if (motivo != null && !motivo.trim().isEmpty()) {
                visita.setObservaciones(motivo);
            }
            
            Visita visitaRechazada = visitaRepository.update(visita);
            
            // 5. Auditoría
            auditoriaService.registrar(
                AccionAuditoria.RECHAZO_VISITA,
                "visitas",
                visitaRechazada.getId(),
                String.format("Visita rechazada: %s (Doc: %s) rechazada por %s. Motivo: %s",
                    visita.getPersona().getNombre(),
                    visita.getPersona().getDocumentoIdentidad(),
                    usuarioActual.getNombre(),
                    motivo != null ? motivo : "No especificado")
            );
            
            return visitaRechazada;
            
        } catch (SQLException e) {
            throw new SicaException("Error al rechazar visita: " + e.getMessage(), e);
        }
    }
    
    /**
     * Procesa una salida olvidada.
     * 
     * PATRÓN DE DISEÑO: FACADE
     * Encapsula la complejidad de regularizar una salida olvidada.
     * 
     * Flujo:
     * 1. Cerrar visita anterior con fecha estimada (entrada + 12 horas)
     * 2. Cambiar estado a "Cerrada por Sistema (Salida Olvidada)"
     * 3. Registrar en auditoría
     * 
     * @param visitaAbierta Visita abierta a regularizar
     * @throws SicaException si hay error
     */
    private void procesarSalidaOlvidada(Visita visitaAbierta) throws SicaException {
        try {
            // Estimar fecha de salida (entrada + 12 horas)
            LocalDateTime fechaSalidaEstimada = visitaAbierta.getFechaEntrada().plusHours(12);
            
            visitaAbierta.setFechaSalida(fechaSalidaEstimada);
            visitaAbierta.setEstado(
                crearEstado(ESTADO_CERRADA_POR_SISTEMA_ID, "Cerrada por Sistema (Salida Olvidada)"));
            visitaAbierta.setObservaciones(
                "Salida no registrada. Cerrada automáticamente por el sistema.");
            
            visitaRepository.update(visitaAbierta);
            
            // Auditoría
            auditoriaService.registrar(
                AccionAuditoria.REGULARIZACION_SALIDA_OLVIDADA,
                "visitas",
                visitaAbierta.getId(),
                String.format("Salida olvidada regularizada: %s (Doc: %s). Fecha salida estimada: %s",
                    visitaAbierta.getPersona().getNombre(),
                    visitaAbierta.getPersona().getDocumentoIdentidad(),
                    fechaSalidaEstimada)
            );
            
        } catch (SQLException e) {
            throw new SicaException("Error al procesar salida olvidada: " + e.getMessage(), e);
        }
    }
    
    /**
     * Busca una visita por ID.
     * 
     * Permisos: requiere "consultar_visitas"
     */
    public Visita buscarPorId(Integer id) throws SicaException {
        if (!authorizationService.tieneAlgunoDeEstosPermisos(
                "consultar_visitas", "registrar_check_in", "registrar_check_out")) {
            throw new PermisoDenegadoException("consultar_visitas", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            return visitaRepository.findById(id)
                .orElseThrow(() -> new SicaException(
                    "No se encontró la visita con ID: " + id));
        } catch (SQLException e) {
            throw new SicaException("Error al buscar visita: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lista todas las visitas.
     * 
     * Permisos: requiere "consultar_visitas"
     */
    public List<Visita> listarTodas() throws SicaException {
        if (!authorizationService.tienePermiso("consultar_visitas")) {
            throw new PermisoDenegadoException("consultar_visitas", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            return visitaRepository.findAll();
        } catch (SQLException e) {
            throw new SicaException("Error al listar visitas: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lista visitas por persona.
     * 
     * Permisos: requiere "consultar_visitas"
     */
    public List<Visita> listarPorPersona(Integer personaId) throws SicaException {
        if (!authorizationService.tieneAlgunoDeEstosPermisos(
                "consultar_visitas", "registrar_check_in")) {
            throw new PermisoDenegadoException("consultar_visitas", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            return visitaRepository.findByPersona(personaId);
        } catch (SQLException e) {
            throw new SicaException("Error al listar visitas por persona: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lista personas actualmente dentro del complejo.
     * 
     * Permisos: requiere "consultar_visitas" o "generar_reporte"
     */
    public List<Visita> listarPersonasDentro() throws SicaException {
        if (!authorizationService.tieneAlgunoDeEstosPermisos(
                "consultar_visitas", "generar_reporte")) {
            throw new PermisoDenegadoException("consultar_visitas", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            return visitaRepository.findPersonasDentro();
        } catch (SQLException e) {
            throw new SicaException("Error al listar personas dentro: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lista visitas pendientes de aprobación.
     * 
     * Permisos: requiere "aprobar_visita" o "consultar_visitas"
     */
    public List<Visita> listarPendientesAprobacion() throws SicaException {
        if (!authorizationService.tieneAlgunoDeEstosPermisos(
                "aprobar_visita", "consultar_visitas")) {
            throw new PermisoDenegadoException("aprobar_visita", 
                sessionManager.getUsuarioActual().getEmail());
        }
        
        try {
            return visitaRepository.findPendientesAprobacion();
        } catch (SQLException e) {
            throw new SicaException("Error al listar visitas pendientes: " + e.getMessage(), e);
        }
    }
    
    /**
     * Valida una visita.
     */
    private void validarVisita(Visita visita) throws SicaException {
        if (visita == null) {
            throw new SicaException("La visita no puede ser nula");
        }
        
        if (visita.getPersona() == null || visita.getPersona().getId() == null) {
            throw new SicaException("La persona es obligatoria");
        }
        
        if (visita.getEstado() == null || visita.getEstado().getId() == null) {
            throw new SicaException("El estado es obligatorio");
        }
    }
    
    /**
     * Crea un objeto VisitaEstado con ID y nombre.
     */
    private VisitaEstado crearEstado(int id, String nombre) {
        VisitaEstado estado = new VisitaEstado();
        estado.setId(id);
        estado.setNombreEstado(nombre);
        return estado;
    }
}
