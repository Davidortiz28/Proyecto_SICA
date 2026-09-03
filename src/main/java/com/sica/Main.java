package com.sica;

import com.sica.exception.*;
import com.sica.model.entity.Usuario;
import com.sica.service.AuthService;
import com.sica.service.AuthorizationService;
import com.sica.security.SessionManager;

import java.util.Scanner;

/**
 * Clase principal del sistema SICA.
 * 
 * NOTA: Esta es una versión TEMPORAL básica para probar el sistema.
 * En las próximas etapas se implementará una interfaz completa con menús.
 * 
 * @author SICA Team
 * @version 0.1 (Temporal)
 */
public class Main {
    
    private static final Scanner scanner = new Scanner(System.in);
    private static final AuthService authService = new AuthService();
    private static final AuthorizationService authzService = new AuthorizationService();
    private static final SessionManager sessionManager = SessionManager.getInstance();
    
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("   SICA - Sistema Integrado de Control de Acceso");
        System.out.println("   Complejo Empresarial 'Zona Acme'");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println();
        
        // Verificar conexión a BD
        if (!verificarConexionBD()) {
            System.out.println("❌ ERROR: No se pudo conectar a la base de datos");
            System.out.println("Por favor verifica:");
            System.out.println("  1. MySQL está en ejecución");
            System.out.println("  2. La base de datos 'sica' existe");
            System.out.println("  3. DatabaseConfig.java tiene las credenciales correctas");
            return;
        }
        
        System.out.println("✅ Conexión a base de datos exitosa");
        System.out.println();
        
        // Ciclo principal
        boolean continuar = true;
        
        while (continuar) {
            if (!sessionManager.haySesionActiva()) {
                // No hay sesión activa, mostrar login
                if (!mostrarLogin()) {
                    // Usuario quiere salir
                    continuar = false;
                }
            } else {
                // Hay sesión activa, mostrar menú principal
                continuar = mostrarMenuPrincipal();
            }
        }
        
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("   ¡Gracias por usar SICA!");
        System.out.println("═══════════════════════════════════════════════════════════════");
        
        scanner.close();
    }
    
    /**
     * Verifica la conexión a la base de datos.
     */
    private static boolean verificarConexionBD() {
        try {
            com.sica.database.DatabaseConnection dbConn = 
                com.sica.database.DatabaseConnection.getInstance();
            return dbConn.testConnection();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Muestra la pantalla de login.
     * 
     * @return true si el login fue exitoso o el usuario quiere reintentar,
     *         false si el usuario quiere salir
     */
    private static boolean mostrarLogin() {
        System.out.println("┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│                      INICIAR SESIÓN                         │");
        System.out.println("└─────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        
        if (email.isEmpty()) {
            System.out.println("❌ Email no puede estar vacío");
            return true; // Reintentar
        }
        
        System.out.print("Contraseña: ");
        String password = scanner.nextLine().trim();
        
        if (password.isEmpty()) {
            System.out.println("❌ Contraseña no puede estar vacía");
            return true; // Reintentar
        }
        
        try {
            // Intentar login
            Usuario usuario = authService.login(email, password);
            
            System.out.println();
            System.out.println("✅ Login exitoso!");
            System.out.println("   Bienvenido/a: " + usuario.getNombre());
            System.out.println("   Rol: " + usuario.getRol().getNombreRol());
            System.out.println("   Permisos: " + usuario.getPermisos().size());
            System.out.println();
            
            presionarEnterParaContinuar();
            
            return true;
            
        } catch (UsuarioNoEncontradoException e) {
            System.out.println();
            System.out.println("❌ ERROR: " + e.getMessage());
            System.out.println();
            return preguntarReintentar();
            
        } catch (CredencialesInvalidasException e) {
            System.out.println();
            System.out.println("❌ ERROR: " + e.getMessage());
            System.out.println();
            return preguntarReintentar();
            
        } catch (UsuarioInactivoException e) {
            System.out.println();
            System.out.println("❌ ERROR: " + e.getMessage());
            System.out.println("   Contacte al administrador del sistema");
            System.out.println();
            return preguntarReintentar();
            
        } catch (SicaException e) {
            System.out.println();
            System.out.println("❌ ERROR: " + e.getMessage());
            System.out.println();
            return preguntarReintentar();
        }
    }
    
    /**
     * Muestra el menú principal (versión temporal básica).
     * 
     * @return true si debe continuar en el ciclo, false si debe salir
     */
    private static boolean mostrarMenuPrincipal() {
        Usuario usuario = sessionManager.getUsuarioActual();
        
        System.out.println("┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│                      MENÚ PRINCIPAL                         │");
        System.out.println("└─────────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("Usuario: " + usuario.getNombre());
        System.out.println("Rol: " + usuario.getRol().getNombreRol());
        System.out.println();
        System.out.println("──────────────────────────────────────────────────────────────");
        System.out.println("1. Ver mis permisos");
        System.out.println("2. Ver información de mi sesión");
        System.out.println("3. Cambiar mi contraseña");
        System.out.println("0. Cerrar sesión");
        System.out.println("──────────────────────────────────────────────────────────────");
        System.out.println();
        System.out.println("NOTA: Las funcionalidades completas se implementarán en las");
        System.out.println("próximas etapas (View y Controller)");
        System.out.println();
        System.out.print("Seleccione una opción: ");
        
        String opcion = scanner.nextLine().trim();
        System.out.println();
        
        switch (opcion) {
            case "1":
                mostrarMisPermisos();
                return true;
                
            case "2":
                mostrarInfoSesion();
                return true;
                
            case "3":
                cambiarMiPassword();
                return true;
                
            case "0":
                cerrarSesion();
                return true;
                
            default:
                System.out.println("❌ Opción no válida");
                System.out.println();
                presionarEnterParaContinuar();
                return true;
        }
    }
    
    /**
     * Muestra los permisos del usuario actual.
     */
    private static void mostrarMisPermisos() {
        System.out.println("┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│                     MIS PERMISOS                            │");
        System.out.println("└─────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        String[] permisos = authzService.listarPermisosActuales();
        
        System.out.println("Tienes " + permisos.length + " permiso(s):");
        System.out.println();
        
        for (int i = 0; i < permisos.length; i++) {
            System.out.println("  " + (i + 1) + ". " + permisos[i]);
        }
        
        System.out.println();
        presionarEnterParaContinuar();
    }
    
    /**
     * Muestra información de la sesión actual.
     */
    private static void mostrarInfoSesion() {
        System.out.println("┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│               INFORMACIÓN DE SESIÓN                         │");
        System.out.println("└─────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        System.out.println(sessionManager.obtenerInfoSesion());
        
        System.out.println();
        presionarEnterParaContinuar();
    }
    
    /**
     * Permite cambiar la contraseña del usuario actual.
     */
    private static void cambiarMiPassword() {
        System.out.println("┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│                 CAMBIAR CONTRASEÑA                          │");
        System.out.println("└─────────────────────────────────────────────────────────────┘");
        System.out.println();
        
        System.out.print("Contraseña actual: ");
        String passwordActual = scanner.nextLine().trim();
        
        System.out.print("Nueva contraseña: ");
        String passwordNueva = scanner.nextLine().trim();
        
        System.out.print("Confirmar nueva contraseña: ");
        String passwordConfirm = scanner.nextLine().trim();
        
        System.out.println();
        
        if (!passwordNueva.equals(passwordConfirm)) {
            System.out.println("❌ ERROR: Las contraseñas no coinciden");
            System.out.println();
            presionarEnterParaContinuar();
            return;
        }
        
        try {
            authService.cambiarPassword(passwordActual, passwordNueva);
            System.out.println("✅ Contraseña cambiada exitosamente");
            System.out.println();
            presionarEnterParaContinuar();
            
        } catch (SicaException e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            System.out.println();
            presionarEnterParaContinuar();
        }
    }
    
    /**
     * Cierra la sesión actual.
     */
    private static void cerrarSesion() {
        System.out.println("Cerrando sesión...");
        
        try {
            authService.logout();
            System.out.println("✅ Sesión cerrada exitosamente");
            System.out.println();
            presionarEnterParaContinuar();
            
        } catch (SicaException e) {
            System.out.println("❌ ERROR al cerrar sesión: " + e.getMessage());
            System.out.println();
            presionarEnterParaContinuar();
        }
    }
    
    /**
     * Pregunta si el usuario quiere reintentar el login.
     */
    private static boolean preguntarReintentar() {
        System.out.print("¿Desea intentar nuevamente? (S/N): ");
        String respuesta = scanner.nextLine().trim().toUpperCase();
        System.out.println();
        
        return respuesta.equals("S") || respuesta.equals("SI") || respuesta.equals("SÍ");
    }
    
    /**
     * Espera a que el usuario presione Enter.
     */
    private static void presionarEnterParaContinuar() {
        System.out.print("Presione Enter para continuar...");
        scanner.nextLine();
        System.out.println();
    }
}
