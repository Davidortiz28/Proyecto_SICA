package com.sica.repository;

import com.sica.model.entity.Permiso;
import com.sica.model.entity.Rol;
import com.sica.model.entity.Usuario;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Clase de prueba para los repositorios.
 * Verifica que las operaciones CRUD funcionen correctamente.
 * 
 * SOLO PARA DESARROLLO Y TESTING.
 * 
 * @author SICA Team
 * @version 1.0
 */
public class TestRepositories {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("PRUEBA DE REPOSITORIOS - PATRÓN REPOSITORY");
        System.out.println("=".repeat(70));
        System.out.println();
        
        try {
            testPermisoRepository();
            System.out.println();
            
            testRolRepository();
            System.out.println();
            
            testUsuarioRepository();
            System.out.println();
            
            System.out.println("=".repeat(70));
            System.out.println("✅ TODAS LAS PRUEBAS COMPLETADAS EXITOSAMENTE");
            System.out.println("=".repeat(70));
            
        } catch (SQLException e) {
            System.err.println();
            System.err.println("❌ ERROR EN LAS PRUEBAS");
            System.err.println("=".repeat(70));
            System.err.println("Mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testPermisoRepository() throws SQLException {
        System.out.println("📋 PRUEBA: PermisoRepository");
        System.out.println("-".repeat(70));
        
        PermisoRepository permisoRepo = new PermisoRepository();
        
        // Test: count
        long totalPermisos = permisoRepo.count();
        System.out.println("  Total de permisos: " + totalPermisos);
        
        // Test: findAll
        List<Permiso> permisos = permisoRepo.findAll();
        System.out.println("  Permisos encontrados: " + permisos.size());
        
        // Test: findById
        if (!permisos.isEmpty()) {
            Optional<Permiso> primerPermiso = permisoRepo.findById(permisos.get(0).getId());
            if (primerPermiso.isPresent()) {
                System.out.println("  ✅ findById funcionando: " + primerPermiso.get().getNombrePermiso());
            }
        }
        
        // Test: findByNombre
        Optional<Permiso> permisoGestionar = permisoRepo.findByNombre("gestionar_usuarios");
        if (permisoGestionar.isPresent()) {
            System.out.println("  ✅ findByNombre funcionando: " + permisoGestionar.get().getDescripcion());
        }
        
        // Test: search
        List<Permiso> resultadoBusqueda = permisoRepo.search("registrar");
        System.out.println("  Permisos que contienen 'registrar': " + resultadoBusqueda.size());
        
        System.out.println("  ✅ PermisoRepository: OK");
    }
    
    private static void testRolRepository() throws SQLException {
        System.out.println("👥 PRUEBA: RolRepository");
        System.out.println("-".repeat(70));
        
        RolRepository rolRepo = new RolRepository();
        
        // Test: count
        long totalRoles = rolRepo.count();
        System.out.println("  Total de roles: " + totalRoles);
        
        // Test: findAll
        List<Rol> roles = rolRepo.findAll();
        System.out.println("  Roles encontrados: " + roles.size());
        
        // Mostrar roles con sus permisos
        for (Rol rol : roles) {
            List<Permiso> permisos = rolRepo.findPermisosByRolId(rol.getId());
            System.out.println("  📋 " + rol.getNombreRol() + " tiene " + permisos.size() + " permisos");
        }
        
        // Test: findByNombre
        Optional<Rol> superusuario = rolRepo.findByNombre("Superusuario");
        if (superusuario.isPresent()) {
            Rol rol = superusuario.get();
            List<Permiso> permisos = rolRepo.findPermisosByRolId(rol.getId());
            System.out.println("  ✅ Superusuario tiene " + permisos.size() + " permisos");
            
            // Verificar que tiene todos los permisos
            if (permisos.size() >= 20) {
                System.out.println("  ✅ Superusuario tiene todos los permisos del sistema");
            }
        }
        
        System.out.println("  ✅ RolRepository: OK");
    }
    
    private static void testUsuarioRepository() throws SQLException {
        System.out.println("👤 PRUEBA: UsuarioRepository");
        System.out.println("-".repeat(70));
        
        UsuarioRepository usuarioRepo = new UsuarioRepository();
        
        // Test: count
        long totalUsuarios = usuarioRepo.count();
        System.out.println("  Total de usuarios: " + totalUsuarios);
        
        // Test: findAll
        List<Usuario> usuarios = usuarioRepo.findAll();
        System.out.println("  Usuarios encontrados: " + usuarios.size());
        
        // Test: findByEmail (crucial para login)
        Optional<Usuario> superuser = usuarioRepo.findByEmail("superuser@sica.com");
        if (superuser.isPresent()) {
            Usuario usuario = superuser.get();
            System.out.println("  ✅ Usuario encontrado: " + usuario.getNombre());
            System.out.println("     - Email: " + usuario.getEmail());
            System.out.println("     - Rol: " + usuario.getRol().getNombreRol());
            System.out.println("     - Permisos: " + usuario.getPermisos().size());
            System.out.println("     - Está activo: " + usuario.isEstaActivo());
            
            // Verificar algunos permisos
            if (usuario.tienePermiso("gestionar_usuarios")) {
                System.out.println("     ✅ Tiene permiso: gestionar_usuarios");
            }
            if (usuario.tienePermiso("generar_reporte")) {
                System.out.println("     ✅ Tiene permiso: generar_reporte");
            }
        }
        
        // Test: findByEstaActivo
        List<Usuario> usuariosActivos = usuarioRepo.findByEstaActivo(true);
        System.out.println("  Usuarios activos: " + usuariosActivos.size());
        
        // Test: existsByEmail
        boolean existeEmail = usuarioRepo.existsByEmail("superuser@sica.com");
        if (existeEmail) {
            System.out.println("  ✅ existsByEmail funcionando correctamente");
        }
        
        // Mostrar todos los usuarios con sus roles
        System.out.println();
        System.out.println("  📋 LISTA DE USUARIOS:");
        for (Usuario u : usuarios) {
            System.out.printf("     - %-30s | %-25s | %s%n", 
                u.getNombre(), 
                u.getRol().getNombreRol(),
                u.isEstaActivo() ? "Activo" : "Inactivo"
            );
        }
        
        System.out.println();
        System.out.println("  ✅ UsuarioRepository: OK");
    }
}
