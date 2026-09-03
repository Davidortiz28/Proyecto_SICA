package com.sica.service;

import com.sica.exception.*;
import com.sica.model.entity.Usuario;
import com.sica.security.SessionManager;

import java.sql.SQLException;

/**
 * Clase de prueba para los servicios de autenticación y autorización.
 * 
 * SOLO PARA DESARROLLO Y TESTING.
 * 
 * @author SICA Team
 * @version 1.0
 */
public class TestAuthServices {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("PRUEBA DE SERVICIOS - AUTENTICACIÓN Y RBAC");
        System.out.println("=".repeat(70));
        System.out.println();
        
        try {
            testLoginExitoso();
            System.out.println();
            
            testVerificarPermisos();
            System.out.println();
            
            testPermisoDenegado();
            System.out.println();
            
            testLogout();
            System.out.println();
            
            testLoginFallido();
            System.out.println();
            
            System.out.println("=".repeat(70));
            System.out.println("✅ TODAS LAS PRUEBAS COMPLETADAS");
            System.out.println("=".repeat(70));
            
        } catch (Exception e) {
            System.err.println();
            System.err.println("❌ ERROR EN LAS PRUEBAS");
            System.err.println("=".repeat(70));
            e.printStackTrace();
        }
    }
    
    private static void testLoginExitoso() throws Exception {
        System.out.println("🔐 PRUEBA: Login Exitoso");
        System.out.println("-".repeat(70));
        
        AuthService authService = new AuthService();
        
        // Intentar login con superusuario
        String email = "superuser@sica.com";
        String password = "super123";
        
        System.out.println("  Intentando login...");
        System.out.println("    Email: " + email);
        System.out.println("    Password: " + password);
        
        Usuario usuario = authService.login(email, password);
        
        System.out.println("  ✅ Login exitoso!");
        System.out.println("    Usuario: " + usuario.getNombre());
        System.out.println("    Rol: " + usuario.getRol().getNombreRol());
        System.out.println("    Permisos: " + usuario.getPermisos().size());
        
        // Verificar sesión
        SessionManager sessionManager = SessionManager.getInstance();
        if (sessionManager.haySesionActiva()) {
            System.out.println("  ✅ Sesión activa creada correctamente");
            System.out.println();
            System.out.println(sessionManager.obtenerInfoSesion());
        }
    }
    
    private static void testVerificarPermisos() throws Exception {
        System.out.println("🔑 PRUEBA: Verificar Permisos (RBAC)");
        System.out.println("-".repeat(70));
        
        AuthorizationService authzService = new AuthorizationService();
        
        // Permisos que debe tener el Superusuario
        String[] permisosEsperados = {
            "gestionar_usuarios",
            "gestionar_roles",
            "gestionar_empresas",
            "registrar_check_in",
            "generar_reporte",
            "consultar_bitacora"
        };
        
        System.out.println("  Verificando permisos del Superusuario...");
        System.out.println();
        
        for (String permiso : permisosEsperados) {
            boolean tiene = authzService.tienePermiso(permiso);
            if (tiene) {
                System.out.println("    ✅ Tiene permiso: " + permiso);
            } else {
                System.out.println("    ❌ NO tiene permiso: " + permiso);
            }
        }
        
        System.out.println();
        System.out.println("  Rol actual: " + authzService.getRolActual());
        System.out.println("  Es Superusuario: " + authzService.esSuperusuario());
        
        // Listar todos los permisos
        System.out.println();
        System.out.println("  Permisos completos del usuario:");
        String[] todosLosPermisos = authzService.listarPermisosActuales();
        for (int i = 0; i < todosLosPermisos.length; i++) {
            System.out.println("    " + (i + 1) + ". " + todosLosPermisos[i]);
        }
    }
    
    private static void testPermisoDenegado() throws Exception {
        System.out.println("⛔ PRUEBA: Permiso Denegado");
        System.out.println("-".repeat(70));
        
        // Cerrar sesión actual
        AuthService authService = new AuthService();
        authService.logout();
        
        // Login con Guarda de Seguridad (permisos limitados)
        System.out.println("  Login como Guarda de Seguridad...");
        Usuario guarda = authService.login("guarda@sica.com", "guarda123");
        System.out.println("  Usuario: " + guarda.getNombre());
        System.out.println("  Rol: " + guarda.getRol().getNombreRol());
        System.out.println("  Permisos: " + guarda.getPermisos().size());
        System.out.println();
        
        AuthorizationService authzService = new AuthorizationService();
        
        // Intentar verificar un permiso que NO tiene
        System.out.println("  Verificando permisos...");
        boolean puedeGestionarUsuarios = authzService.tienePermiso("gestionar_usuarios");
        boolean puedeRegistrarCheckIn = authzService.tienePermiso("registrar_check_in");
        
        System.out.println("    ¿Puede gestionar usuarios?: " + 
            (puedeGestionarUsuarios ? "✅ Sí" : "❌ No"));
        System.out.println("    ¿Puede registrar check-in?: " + 
            (puedeRegistrarCheckIn ? "✅ Sí" : "❌ No"));
        
        // Intentar requerir un permiso que NO tiene (debe lanzar excepción)
        System.out.println();
        System.out.println("  Intentando requerir permiso 'gestionar_usuarios'...");
        try {
            authzService.requirePermiso("gestionar_usuarios");
            System.out.println("    ❌ ERROR: No se lanzó excepción!");
        } catch (PermisoDenegadoException e) {
            System.out.println("    ✅ Excepción lanzada correctamente:");
            System.out.println("       " + e.getMessage());
        }
    }
    
    private static void testLogout() throws Exception {
        System.out.println("🚪 PRUEBA: Logout");
        System.out.println("-".repeat(70));
        
        SessionManager sessionManager = SessionManager.getInstance();
        
        System.out.println("  Estado antes del logout:");
        System.out.println("    Sesión activa: " + sessionManager.haySesionActiva());
        
        AuthService authService = new AuthService();
        authService.logout();
        
        System.out.println("  Estado después del logout:");
        System.out.println("    Sesión activa: " + sessionManager.haySesionActiva());
        System.out.println("  ✅ Logout exitoso");
    }
    
    private static void testLoginFallido() throws Exception {
        System.out.println("❌ PRUEBA: Login Fallido");
        System.out.println("-".repeat(70));
        
        AuthService authService = new AuthService();
        
        // Caso 1: Usuario no existe
        System.out.println("  Caso 1: Usuario no existe");
        try {
            authService.login("noexiste@example.com", "password");
            System.out.println("    ❌ ERROR: No se lanzó excepción!");
        } catch (UsuarioNoEncontradoException e) {
            System.out.println("    ✅ Excepción correcta: " + e.getMessage());
        }
        
        // Caso 2: Contraseña incorrecta
        System.out.println();
        System.out.println("  Caso 2: Contraseña incorrecta");
        try {
            authService.login("superuser@sica.com", "wrong_password");
            System.out.println("    ❌ ERROR: No se lanzó excepción!");
        } catch (CredencialesInvalidasException e) {
            System.out.println("    ✅ Excepción correcta: " + e.getMessage());
        }
        
        System.out.println();
        System.out.println("  ✅ Todas las excepciones funcionan correctamente");
    }
}
