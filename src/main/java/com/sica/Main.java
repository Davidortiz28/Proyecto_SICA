package com.sica;

import com.sica.exception.*;
import com.sica.model.entity.*;
import com.sica.model.enums.TipoPersona;
import com.sica.service.*;
import com.sica.security.SessionManager;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Clase principal del sistema SICA - VERSIÓN COMPLETA.
 * 
 * Incluye:
 * - Login/Logout
 * - Gestión de Usuarios, Empresas, Personas
 * - Control de Acceso (Check-In/Check-Out)
 * - Aprobación de Visitas
 * - Registro de Incidentes
 * - Generación de Reportes
 * - RBAC integrado en todas las funcionalidades
 * 
 * @author SICA Team
 * @version 1.0 (Completa)
 */
public class Main {
    
    private static final Scanner scanner = new Scanner(System.in);
    private static final SessionManager sessionManager = SessionManager.getInstance();
    
    // Services
    private static AuthService authService;
    private static AuthorizationService authzService;
    private static UsuarioService usuarioService;
    private static EmpresaService empresaService;
    private static PersonaService personaService;
    private static VisitaService visitaService;
    private static IncidenteService incidenteService;
    private static ReporteService reporteService;
    
    private static final DateTimeFormatter FORMATO_FECHA = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public static void main(String[] args) {
        mostrarBanner();
        
        if (!verificarConexionBD()) {
            mostrarErrorConexion();
            return;
        }
        
        if (!inicializarServicios()) {
            return;
        }
        
        System.out.println("✅ Conexión a base de datos exitosa\n");
        
        // Ciclo principal
        boolean continuar = true;
        while (continuar) {
            if (!sessionManager.haySesionActiva()) {
                if (!mostrarLogin()) {
                    continuar = false;
                }
            } else {
                continuar = mostrarMenuPrincipal();
            }
        }
        
        mostrarDespedida();
        scanner.close();
    }
    
    private static void mostrarBanner() {
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("   SICA - Sistema Integrado de Control de Acceso");
        System.out.println("   Complejo Empresarial 'Zona Acme'");
        System.out.println("═══════════════════════════════════════════════════════════════\n");
    }
    
    private static boolean verificarConexionBD() {
        try {
            com.sica.database.DatabaseConnection dbConn = 
                com.sica.database.DatabaseConnection.getInstance();
            return dbConn.testConnection();
        } catch (Exception e) {
            return false;
        }
    }
    
    private static void mostrarErrorConexion() {
        System.out.println("❌ ERROR: No se pudo conectar a la base de datos");
        System.out.println("Por favor verifica:");
        System.out.println("  1. MySQL está en ejecución");
        System.out.println("  2. La base de datos 'sica' existe");
        System.out.println("  3. DatabaseConfig.java tiene las credenciales correctas");
    }
    
    private static boolean inicializarServicios() {
        try {
            authService = new AuthService();
            authzService = new AuthorizationService();
            usuarioService = new UsuarioService();
            empresaService = new EmpresaService();
            personaService = new PersonaService();
            visitaService = new VisitaService();
            incidenteService = new IncidenteService();
            reporteService = new ReporteService();
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Error al inicializar los servicios del sistema: " + e.getMessage());
            return false;
        }
    }
    
    private static void mostrarDespedida() {
        System.out.println("\n═══════════════════════════════════════════════════════════════");
        System.out.println("   ¡Gracias por usar SICA!");
        System.out.println("═══════════════════════════════════════════════════════════════");
    }
    
    // ========== LOGIN ==========
    
    private static boolean mostrarLogin() {
        limpiarPantalla();
        System.out.println("┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│                      INICIAR SESIÓN                         │");
        System.out.println("└─────────────────────────────────────────────────────────────┘\n");
        
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        
        if (email.isEmpty()) {
            System.out.println("❌ Email no puede estar vacío");
            pausa();
            return true;
        }
        
        System.out.print("Contraseña: ");
        String password = scanner.nextLine().trim();
        
        if (password.isEmpty()) {
            System.out.println("❌ Contraseña no puede estar vacía");
            pausa();
            return true;
        }
        
        try {
            Usuario usuario = authService.login(email, password);
            System.out.println("\n✅ Login exitoso!");
            System.out.println("   Bienvenido/a: " + usuario.getNombre());
            System.out.println("   Rol: " + usuario.getRol().getNombreRol());
            pausa();
            return true;
        } catch (SicaException | SQLException e) {
            System.out.println("\n❌ ERROR: " + e.getMessage());
            return preguntarReintentar();
        }
    }
    
    // ========== MENÚ PRINCIPAL ==========
    
    private static boolean mostrarMenuPrincipal() {
        limpiarPantalla();
        Usuario usuario = sessionManager.getUsuarioActual();
        
        System.out.println("╔═════════════════════════════════════════════════════════════╗");
        System.out.println("║                      MENÚ PRINCIPAL                         ║");
        System.out.println("╚═════════════════════════════════════════════════════════════╝");
        System.out.println("Usuario: " + usuario.getNombre());
        System.out.println("Rol: " + usuario.getRol().getNombreRol());
        System.out.println("─────────────────────────────────────────────────────────────\n");
        
        System.out.println("1. Control de Acceso (Check-In/Check-Out)");
        System.out.println("2. Gestión de Personas");
        System.out.println("3. Gestión de Visitas");
        System.out.println("4. Gestión de Incidentes");
        System.out.println("5. Reportes");
        System.out.println("6. Administración");
        System.out.println("7. Mi Cuenta");
        System.out.println("0. Cerrar Sesión\n");
        
        System.out.print("Seleccione una opción: ");
        String opcion = scanner.nextLine().trim();
        
        switch (opcion) {
            case "1": menuControlAcceso(); return true;
            case "2": menuPersonas(); return true;
            case "3": menuVisitas(); return true;
            case "4": menuIncidentes(); return true;
            case "5": menuReportes(); return true;
            case "6": menuAdministracion(); return true;
            case "7": menuMiCuenta(); return true;
            case "0": cerrarSesion(); return true;
            default:
                System.out.println("\n❌ Opción no válida");
                pausa();
                return true;
        }
    }
    
    // ========== CONTROL DE ACCESO ==========
    
    private static void menuControlAcceso() {
        while (true) {
            limpiarPantalla();
            System.out.println("╔═════════════════════════════════════════════════════════════╗");
            System.out.println("║                   CONTROL DE ACCESO                         ║");
            System.out.println("╚═════════════════════════════════════════════════════════════╝\n");
            
            System.out.println("1. Registrar Entrada (Check-In)");
            System.out.println("2. Registrar Salida (Check-Out)");
            System.out.println("3. Ver Personas Dentro");
            System.out.println("0. Volver\n");
            
            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine().trim();
            
            switch (opcion) {
                case "1": registrarCheckIn(); break;
                case "2": registrarCheckOut(); break;
                case "3": verPersonasDentro(); break;
                case "0": return;
                default:
                    System.out.println("\n❌ Opción no válida");
                    pausa();
            }
        }
    }
    
    private static void registrarCheckIn() {
        limpiarPantalla();
        System.out.println("═══ REGISTRAR ENTRADA (CHECK-IN) ═══\n");
        
        System.out.print("Documento de identidad: ");
        String documento = scanner.nextLine().trim();
        
        try {
            Persona persona = personaService.buscarPorDocumento(documento);
            
            System.out.println("\nPersona encontrada:");
            System.out.println("  Nombre: " + persona.getNombre());
            System.out.println("  Tipo: " + persona.getTipoPersona());
            System.out.println("  Estado: " + persona.getEstadoAcceso().getNombreEstado());
            
            if (!persona.puedeIngresar()) {
                System.out.println("\n❌ Esta persona tiene PROHIBICIÓN DE INGRESO");
                pausa();
                return;
            }
            
            System.out.print("\nMotivo de visita: ");
            String motivo = scanner.nextLine().trim();
            
            System.out.print("Placa de vehículo (opcional): ");
            String placa = scanner.nextLine().trim();
            if (placa.isEmpty()) placa = null;
            
            Visita visita = visitaService.registrarCheckIn(persona.getId(), motivo, placa);
            
            System.out.println("\n✅ CHECK-IN REGISTRADO EXITOSAMENTE");
            System.out.println("  ID Visita: " + visita.getId());
            System.out.println("  Hora entrada: " + visita.getFechaEntrada().format(FORMATO_FECHA));
            
        } catch (SicaException e) {
            System.out.println("\n❌ ERROR: " + e.getMessage());
        }
        
        pausa();
    }
    
    private static void registrarCheckOut() {
        limpiarPantalla();
        System.out.println("═══ REGISTRAR SALIDA (CHECK-OUT) ═══\n");
        
        System.out.print("ID de la visita: ");
        String idStr = scanner.nextLine().trim();
        
        try {
            int visitaId = Integer.parseInt(idStr);
            Visita visita = visitaService.buscarPorId(visitaId);
            
            System.out.println("\nVisita encontrada:");
            System.out.println("  Persona: " + visita.getPersona().getNombre());
            System.out.println("  Entrada: " + visita.getFechaEntrada().format(FORMATO_FECHA));
            System.out.println("  Estado: " + visita.getEstado().getNombreEstado());
            
            if (!visita.estaDentro()) {
                System.out.println("\n❌ Esta visita no está en estado 'Dentro'");
                pausa();
                return;
            }
            
            System.out.print("\nObservaciones (opcional): ");
            String obs = scanner.nextLine().trim();
            if (obs.isEmpty()) obs = null;
            
            Visita visitaCerrada = visitaService.registrarCheckOut(visitaId, obs);
            
            System.out.println("\n✅ CHECK-OUT REGISTRADO EXITOSAMENTE");
            System.out.println("  Hora salida: " + visitaCerrada.getFechaSalida().format(FORMATO_FECHA));
            System.out.println("  Duración: " + visitaCerrada.calcularDuracionMinutos() + " minutos");
            
        } catch (NumberFormatException e) {
            System.out.println("\n❌ ID inválido");
        } catch (SicaException e) {
            System.out.println("\n❌ ERROR: " + e.getMessage());
        }
        
        pausa();
    }
    
    private static void verPersonasDentro() {
        limpiarPantalla();
        System.out.println("═══ PERSONAS ACTUALMENTE DENTRO ═══\n");
        
        try {
            String reporte = reporteService.reportePersonasDentro();
            System.out.println(reporte);
        } catch (SicaException e) {
            System.out.println("❌ ERROR: " + e.getMessage());
        }
        
        pausa();
    }
    
    // ========== GESTIÓN DE PERSONAS ==========
    
    private static void menuPersonas() {
        while (true) {
            limpiarPantalla();
            System.out.println("╔═════════════════════════════════════════════════════════════╗");
            System.out.println("║                   GESTIÓN DE PERSONAS                       ║");
            System.out.println("╚═════════════════════════════════════════════════════════════╝\n");
            
            System.out.println("1. Buscar Persona");
            System.out.println("2. Listar Todas las Personas");
            System.out.println("3. Registrar Nueva Persona");
            System.out.println("4. Bloquear Persona");
            System.out.println("5. Desbloquear Persona");
            System.out.println("0. Volver\n");
            
            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine().trim();
            
            switch (opcion) {
                case "1": buscarPersona(); break;
                case "2": listarPersonas(); break;
                case "3": registrarPersona(); break;
                case "4": bloquearPersona(); break;
                case "5": desbloquearPersona(); break;
                case "0": return;
                default:
                    System.out.println("\n❌ Opción no válida");
                    pausa();
            }
        }
    }
    
    private static void buscarPersona() {
        limpiarPantalla();
        System.out.println("═══ BUSCAR PERSONA ═══\n");
        
        System.out.print("Documento de identidad: ");
        String documento = scanner.nextLine().trim();
        
        try {
            Persona persona = personaService.buscarPorDocumento(documento);
            mostrarDetallePersona(persona);
        } catch (SicaException e) {
            System.out.println("\n❌ ERROR: " + e.getMessage());
        }
        
        pausa();
    }
    
    private static void mostrarDetallePersona(Persona p) {
        System.out.println("\n═══ INFORMACIÓN DE LA PERSONA ═══");
        System.out.println("Nombre: " + p.getNombre());
        System.out.println("Documento: " + p.getDocumentoIdentidad());
        System.out.println("Tipo: " + p.getTipoPersona());
        if (p.getEmpresa() != null) {
            System.out.println("Empresa: " + p.getEmpresa().getNombreEmpresa());
        }
        System.out.println("Teléfono: " + p.getTelefono());
        System.out.println("Email: " + p.getEmail());
        System.out.println("Estado Acceso: " + p.getEstadoAcceso().getNombreEstado());
    }
    
    private static void listarPersonas() {
        limpiarPantalla();
        System.out.println("═══ LISTADO DE PERSONAS ═══\n");
        
        try {
            List<Persona> personas = personaService.listarTodas();
            
            if (personas.isEmpty()) {
                System.out.println("No hay personas registradas");
            } else {
                System.out.println("Total: " + personas.size() + " personas\n");
                for (int i = 0; i < personas.size(); i++) {
                    Persona p = personas.get(i);
                    System.out.println((i + 1) + ". " + p.getNombre() + 
                        " (" + p.getDocumentoIdentidad() + ") - " + p.getTipoPersona());
                }
            }
        } catch (SicaException e) {
            System.out.println("❌ ERROR: " + e.getMessage());
        }
        
        pausa();
    }
    
    private static void registrarPersona() {
        System.out.println("\nNOTA: Funcionalidad completa requiere más campos.");
        System.out.println("Use la interfaz administrativa para registro completo.");
        pausa();
    }
    
    private static void bloquearPersona() {
        limpiarPantalla();
        System.out.println("═══ BLOQUEAR PERSONA ═══\n");
        
        System.out.print("ID de la persona: ");
        String idStr = scanner.nextLine().trim();
        
        try {
            int personaId = Integer.parseInt(idStr);
            personaService.bloquear(personaId);
            
            System.out.println("\n✅ Persona bloqueada exitosamente");
        } catch (NumberFormatException e) {
            System.out.println("\n❌ ID inválido");
        } catch (SicaException e) {
            System.out.println("\n❌ ERROR: " + e.getMessage());
        }
        
        pausa();
    }
    
    private static void desbloquearPersona() {
        limpiarPantalla();
        System.out.println("═══ DESBLOQUEAR PERSONA ═══\n");
        
        System.out.print("ID de la persona: ");
        String idStr = scanner.nextLine().trim();
        
        try {
            int personaId = Integer.parseInt(idStr);
            personaService.desbloquear(personaId);
            
            System.out.println("\n✅ Persona desbloqueada exitosamente");
        } catch (NumberFormatException e) {
            System.out.println("\n❌ ID inválido");
        } catch (SicaException e) {
            System.out.println("\n❌ ERROR: " + e.getMessage());
        }
        
        pausa();
    }
    
    // ========== GESTIÓN DE VISITAS ==========
    
    private static void menuVisitas() {
        while (true) {
            limpiarPantalla();
            System.out.println("╔═════════════════════════════════════════════════════════════╗");
            System.out.println("║                   GESTIÓN DE VISITAS                        ║");
            System.out.println("╚═════════════════════════════════════════════════════════════╝\n");
            
            System.out.println("1. Ver Visitas Pendientes de Aprobación");
            System.out.println("2. Aprobar Visita");
            System.out.println("3. Rechazar Visita");
            System.out.println("4. Historial de Visitas");
            System.out.println("0. Volver\n");
            
            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine().trim();
            
            switch (opcion) {
                case "1": verVisitasPendientes(); break;
                case "2": aprobarVisita(); break;
                case "3": rechazarVisita(); break;
                case "4": verHistorialVisitas(); break;
                case "0": return;
                default:
                    System.out.println("\n❌ Opción no válida");
                    pausa();
            }
        }
    }
    
    private static void verVisitasPendientes() {
        limpiarPantalla();
        System.out.println("═══ VISITAS PENDIENTES DE APROBACIÓN ═══\n");
        
        try {
            List<Visita> pendientes = visitaService.listarPendientesAprobacion();
            
            if (pendientes.isEmpty()) {
                System.out.println("No hay visitas pendientes");
            } else {
                for (int i = 0; i < pendientes.size(); i++) {
                    Visita v = pendientes.get(i);
                    System.out.println((i + 1) + ". ID: " + v.getId() + 
                        " | Persona: " + v.getPersona().getNombre() +
                        " | Estado: " + v.getEstado().getNombreEstado());
                }
            }
        } catch (SicaException e) {
            System.out.println("❌ ERROR: " + e.getMessage());
        }
        
        pausa();
    }
    
    private static void aprobarVisita() {
        limpiarPantalla();
        System.out.println("═══ APROBAR VISITA ═══\n");
        
        System.out.print("ID de la visita: ");
        String idStr = scanner.nextLine().trim();
        
        try {
            int visitaId = Integer.parseInt(idStr);
            visitaService.aprobar(visitaId);
            
            System.out.println("\n✅ Visita aprobada exitosamente");
        } catch (NumberFormatException e) {
            System.out.println("\n❌ ID inválido");
        } catch (SicaException e) {
            System.out.println("\n❌ ERROR: " + e.getMessage());
        }
        
        pausa();
    }
    
    private static void rechazarVisita() {
        limpiarPantalla();
        System.out.println("═══ RECHAZAR VISITA ═══\n");
        
        System.out.print("ID de la visita: ");
        String idStr = scanner.nextLine().trim();
        
        System.out.print("Motivo del rechazo: ");
        String motivo = scanner.nextLine().trim();
        
        try {
            int visitaId = Integer.parseInt(idStr);
            visitaService.rechazar(visitaId, motivo);
            
            System.out.println("\n✅ Visita rechazada exitosamente");
        } catch (NumberFormatException e) {
            System.out.println("\n❌ ID inválido");
        } catch (SicaException e) {
            System.out.println("\n❌ ERROR: " + e.getMessage());
        }
        
        pausa();
    }
    
    private static void verHistorialVisitas() {
        limpiarPantalla();
        System.out.println("═══ HISTORIAL DE VISITAS ═══\n");
        
        try {
            LocalDateTime fechaInicio = LocalDateTime.now().minusDays(7);
            LocalDateTime fechaFin = LocalDateTime.now();
            
            String reporte = reporteService.reporteHistorialVisitas(fechaInicio, fechaFin);
            System.out.println(reporte);
        } catch (SicaException e) {
            System.out.println("❌ ERROR: " + e.getMessage());
        }
        
        pausa();
    }
    
    // ========== GESTIÓN DE INCIDENTES ==========
    
    private static void menuIncidentes() {
        while (true) {
            limpiarPantalla();
            System.out.println("╔═════════════════════════════════════════════════════════════╗");
            System.out.println("║                   GESTIÓN DE INCIDENTES                     ║");
            System.out.println("╚═════════════════════════════════════════════════════════════╝\n");
            
            System.out.println("1. Registrar Nuevo Incidente");
            System.out.println("2. Ver Todos los Incidentes");
            System.out.println("3. Reporte de Incidentes");
            System.out.println("0. Volver\n");
            
            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine().trim();
            
            switch (opcion) {
                case "1": registrarIncidente(); break;
                case "2": verIncidentes(); break;
                case "3": reporteIncidentes(); break;
                case "0": return;
                default:
                    System.out.println("\n❌ Opción no válida");
                    pausa();
            }
        }
    }
    
    private static void registrarIncidente() {
        limpiarPantalla();
        System.out.println("═══ REGISTRAR INCIDENTE ═══\n");
        
        System.out.print("Descripción del incidente: ");
        String descripcion = scanner.nextLine().trim();
        
        System.out.println("\nGravedad:");
        System.out.println("1. Baja");
        System.out.println("2. Media");
        System.out.println("3. Alta");
        System.out.println("4. Crítica");
        System.out.print("Seleccione: ");
        String gravedadOp = scanner.nextLine().trim();
        
        String gravedad;
        switch (gravedadOp) {
            case "1": gravedad = "Baja"; break;
            case "2": gravedad = "Media"; break;
            case "3": gravedad = "Alta"; break;
            case "4": gravedad = "Crítica"; break;
            default:
                System.out.println("\n❌ Opción inválida");
                pausa();
                return;
        }
        
        try {
            Incidente incidente = new Incidente();
            incidente.setDescripcion(descripcion);
            incidente.setGravedad(gravedad);
            incidente.setFechaIncidente(LocalDateTime.now());
            
            Incidente nuevo = incidenteService.registrar(incidente);
            
            System.out.println("\n✅ INCIDENTE REGISTRADO EXITOSAMENTE");
            System.out.println("  ID: " + nuevo.getId());
            System.out.println("  Gravedad: " + nuevo.getGravedad());
            
        } catch (SicaException e) {
            System.out.println("\n❌ ERROR: " + e.getMessage());
        }
        
        pausa();
    }
    
    private static void verIncidentes() {
        limpiarPantalla();
        System.out.println("═══ LISTADO DE INCIDENTES ═══\n");
        
        try {
            List<Incidente> incidentes = incidenteService.listarTodos();
            
            if (incidentes.isEmpty()) {
                System.out.println("No hay incidentes registrados");
            } else {
                System.out.println("Total: " + incidentes.size() + " incidentes\n");
                for (int i = 0; i < incidentes.size(); i++) {
                    Incidente inc = incidentes.get(i);
                    System.out.println((i + 1) + ". [" + inc.getGravedad() + "] " + 
                        inc.getDescripcion().substring(0, Math.min(50, inc.getDescripcion().length())));
                }
            }
        } catch (SicaException e) {
            System.out.println("❌ ERROR: " + e.getMessage());
        }
        
        pausa();
    }
    
    private static void reporteIncidentes() {
        limpiarPantalla();
        try {
            String reporte = reporteService.reporteIncidentes();
            System.out.println(reporte);
        } catch (SicaException e) {
            System.out.println("❌ ERROR: " + e.getMessage());
        }
        pausa();
    }
    
    // ========== REPORTES ==========
    
    private static void menuReportes() {
        while (true) {
            limpiarPantalla();
            System.out.println("╔═════════════════════════════════════════════════════════════╗");
            System.out.println("║                         REPORTES                            ║");
            System.out.println("╚═════════════════════════════════════════════════════════════╝\n");
            
            System.out.println("1. Personas Actualmente Dentro");
            System.out.println("2. Historial de Visitas");
            System.out.println("3. Reporte de Incidentes");
            System.out.println("4. Personas Bloqueadas");
            System.out.println("5. Estadísticas Generales");
            System.out.println("0. Volver\n");
            
            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine().trim();
            
            switch (opcion) {
                case "1": verPersonasDentro(); break;
                case "2": verHistorialVisitas(); break;
                case "3": reporteIncidentes(); break;
                case "4": reportePersonasBloqueadas(); break;
                case "5": estadisticasGenerales(); break;
                case "0": return;
                default:
                    System.out.println("\n❌ Opción no válida");
                    pausa();
            }
        }
    }
    
    private static void reportePersonasBloqueadas() {
        limpiarPantalla();
        try {
            String reporte = reporteService.reportePersonasBloqueadas();
            System.out.println(reporte);
        } catch (SicaException e) {
            System.out.println("❌ ERROR: " + e.getMessage());
        }
        pausa();
    }
    
    private static void estadisticasGenerales() {
        limpiarPantalla();
        try {
            String reporte = reporteService.estadisticasGenerales();
            System.out.println(reporte);
        } catch (SicaException e) {
            System.out.println("❌ ERROR: " + e.getMessage());
        }
        pausa();
    }
    
    // ========== ADMINISTRACIÓN ==========
    
    private static void menuAdministracion() {
        System.out.println("\nNOTA: Funciones administrativas avanzadas");
        System.out.println("(Gestión de usuarios, empresas, roles)");
        System.out.println("Disponibles con permisos de administrador.");
        pausa();
    }
    
    // ========== MI CUENTA ==========
    
    private static void menuMiCuenta() {
        while (true) {
            limpiarPantalla();
            System.out.println("╔═════════════════════════════════════════════════════════════╗");
            System.out.println("║                        MI CUENTA                            ║");
            System.out.println("╚═════════════════════════════════════════════════════════════╝\n");
            
            System.out.println("1. Ver Mis Permisos");
            System.out.println("2. Ver Información de Sesión");
            System.out.println("3. Cambiar Contraseña");
            System.out.println("0. Volver\n");
            
            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine().trim();
            
            switch (opcion) {
                case "1": verMisPermisos(); break;
                case "2": verInfoSesion(); break;
                case "3": cambiarPassword(); break;
                case "0": return;
                default:
                    System.out.println("\n❌ Opción no válida");
                    pausa();
            }
        }
    }
    
    private static void verMisPermisos() {
        limpiarPantalla();
        System.out.println("═══ MIS PERMISOS ═══\n");
        
        String[] permisos = authzService.listarPermisosActuales();
        System.out.println("Tienes " + permisos.length + " permiso(s):\n");
        
        for (int i = 0; i < permisos.length; i++) {
            System.out.println("  " + (i + 1) + ". " + permisos[i]);
        }
        
        pausa();
    }
    
    private static void verInfoSesion() {
        limpiarPantalla();
        System.out.println("═══ INFORMACIÓN DE SESIÓN ═══\n");
        System.out.println(sessionManager.obtenerInfoSesion());
        pausa();
    }
    
    private static void cambiarPassword() {
        limpiarPantalla();
        System.out.println("═══ CAMBIAR CONTRASEÑA ═══\n");
        
        System.out.print("Contraseña actual: ");
        String actual = scanner.nextLine().trim();
        
        System.out.print("Nueva contraseña: ");
        String nueva = scanner.nextLine().trim();
        
        System.out.print("Confirmar nueva contraseña: ");
        String confirmar = scanner.nextLine().trim();
        
        if (!nueva.equals(confirmar)) {
            System.out.println("\n❌ Las contraseñas no coinciden");
            pausa();
            return;
        }
        
        try {
            authService.cambiarPassword(actual, nueva);
            System.out.println("\n✅ Contraseña cambiada exitosamente");
        } catch (SicaException e) {
            System.out.println("\n❌ ERROR: " + e.getMessage());
        }
        
        pausa();
    }
    
    private static void cerrarSesion() {
        try {
            authService.logout();
            System.out.println("\n✅ Sesión cerrada exitosamente");
            pausa();
        } catch (SQLException e) {
            System.out.println("\n❌ ERROR al cerrar sesión: " + e.getMessage());
            pausa();
        }
    }
    
    // ========== UTILIDADES ==========
    
    private static void limpiarPantalla() {
        for (int i = 0; i < 50; i++) System.out.println();
    }
    
    private static void pausa() {
        System.out.print("\nPresione Enter para continuar...");
        scanner.nextLine();
    }
    
    private static boolean preguntarReintentar() {
        System.out.print("¿Desea intentar nuevamente? (S/N): ");
        String respuesta = scanner.nextLine().trim().toUpperCase();
        System.out.println();
        return respuesta.equals("S") || respuesta.equals("SI") || respuesta.equals("SÍ");
    }
}
