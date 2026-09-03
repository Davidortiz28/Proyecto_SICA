package com.sica.database;

import java.sql.SQLException;

/**
 * Clase de prueba para verificar la conexión a la base de datos.
 * 
 * Esta clase es solo para propósitos de testing y desarrollo.
 * NO debe incluirse en la versión final de producción.
 * 
 * USO:
 * 1. Asegúrate de que MySQL está corriendo
 * 2. Verifica que la base de datos 'sica' existe
 * 3. Configura usuario y contraseña en DatabaseConfig.java
 * 4. Ejecuta esta clase
 * 
 * @author SICA Team
 * @version 1.0
 */
public class TestConnection {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("PRUEBA DE CONEXIÓN A LA BASE DE DATOS SICA");
        System.out.println("=".repeat(60));
        System.out.println();
        
        try {
            // Intentar obtener la instancia de conexión (Singleton)
            System.out.println("📡 Intentando conectar a MySQL...");
            System.out.println("   Host: localhost:3306");
            System.out.println("   Base de datos: sica");
            System.out.println("   Usuario: " + DatabaseConfig.USER);
            System.out.println();
            
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            
            // Verificar que la conexión esté activa
            if (dbConnection.isConnected()) {
                System.out.println("✅ CONEXIÓN EXITOSA");
                System.out.println();
                
                // Mostrar información de la conexión
                System.out.println(dbConnection.getConnectionInfo());
                System.out.println();
                
                // Probar la conexión con una consulta
                System.out.println("🔍 Probando conexión con consulta...");
                if (dbConnection.testConnection()) {
                    System.out.println("✅ Consulta de prueba exitosa");
                    System.out.println();
                    
                    // Realizar consultas de verificación
                    verificarBaseDatos(dbConnection);
                    
                } else {
                    System.out.println("❌ Error al ejecutar consulta de prueba");
                }
                
            } else {
                System.out.println("❌ La conexión no está activa");
            }
            
            System.out.println();
            System.out.println("=".repeat(60));
            System.out.println("PRUEBA COMPLETADA");
            System.out.println("=".repeat(60));
            
        } catch (SQLException e) {
            System.err.println();
            System.err.println("❌ ERROR DE CONEXIÓN");
            System.err.println("=".repeat(60));
            System.err.println("Mensaje: " + e.getMessage());
            System.err.println();
            System.err.println("POSIBLES SOLUCIONES:");
            System.err.println("1. Verifica que MySQL esté corriendo:");
            System.err.println("   - Windows: Busca 'Services' y verifica que 'MySQL80' esté activo");
            System.err.println("   - CMD: mysql --version");
            System.err.println();
            System.err.println("2. Verifica que la base de datos 'sica' existe:");
            System.err.println("   - Abre MySQL Workbench");
            System.err.println("   - Ejecuta: SHOW DATABASES;");
            System.err.println("   - Si no existe, ejecuta database/schema.sql");
            System.err.println();
            System.err.println("3. Verifica las credenciales en DatabaseConfig.java:");
            System.err.println("   - USER debe ser tu usuario de MySQL (por defecto 'root')");
            System.err.println("   - PASSWORD debe ser tu contraseña de MySQL");
            System.err.println();
            System.err.println("4. Verifica que el puerto 3306 esté disponible");
            System.err.println("=".repeat(60));
        }
    }
    
    /**
     * Verifica que las tablas principales de la base de datos existan.
     * 
     * @param dbConnection Conexión a la base de datos
     */
    private static void verificarBaseDatos(DatabaseConnection dbConnection) {
        System.out.println("📊 Verificando estructura de la base de datos...");
        System.out.println();
        
        String[] tablasEsperadas = {
            "usuarios", "roles", "permisos", "rol_permisos",
            "empresas", "personas", "visitas", "incidentes",
            "persona_estados_acceso", "visita_estados", "bitacora_auditoria"
        };
        
        int tablasEncontradas = 0;
        
        try {
            var metaData = dbConnection.getConnection().getMetaData();
            
            for (String tabla : tablasEsperadas) {
                var rs = metaData.getTables(null, null, tabla, null);
                if (rs.next()) {
                    System.out.println("  ✅ Tabla '" + tabla + "' encontrada");
                    tablasEncontradas++;
                } else {
                    System.out.println("  ❌ Tabla '" + tabla + "' NO encontrada");
                }
                rs.close();
            }
            
            System.out.println();
            System.out.println("📊 Resultado: " + tablasEncontradas + "/" + tablasEsperadas.length + " tablas encontradas");
            
            if (tablasEncontradas == tablasEsperadas.length) {
                System.out.println("✅ Base de datos completa");
                
                // Verificar datos de prueba
                verificarDatosPrueba(dbConnection);
            } else {
                System.out.println("⚠️  Base de datos incompleta");
                System.out.println("   Ejecuta database/schema.sql para crear las tablas");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al verificar tablas: " + e.getMessage());
        }
    }
    
    /**
     * Verifica que existan datos de prueba en la base de datos.
     * 
     * @param dbConnection Conexión a la base de datos
     */
    private static void verificarDatosPrueba(DatabaseConnection dbConnection) {
        System.out.println();
        System.out.println("📦 Verificando datos de prueba...");
        System.out.println();
        
        try {
            var stmt = dbConnection.getConnection().createStatement();
            
            // Contar roles
            var rs = stmt.executeQuery("SELECT COUNT(*) as total FROM roles");
            if (rs.next()) {
                int totalRoles = rs.getInt("total");
                System.out.println("  📋 Roles: " + totalRoles);
            }
            rs.close();
            
            // Contar permisos
            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM permisos");
            if (rs.next()) {
                int totalPermisos = rs.getInt("total");
                System.out.println("  🔐 Permisos: " + totalPermisos);
            }
            rs.close();
            
            // Contar usuarios
            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM usuarios");
            if (rs.next()) {
                int totalUsuarios = rs.getInt("total");
                System.out.println("  👤 Usuarios: " + totalUsuarios);
            }
            rs.close();
            
            // Contar empresas
            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM empresas");
            if (rs.next()) {
                int totalEmpresas = rs.getInt("total");
                System.out.println("  🏢 Empresas: " + totalEmpresas);
            }
            rs.close();
            
            // Contar personas
            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM personas");
            if (rs.next()) {
                int totalPersonas = rs.getInt("total");
                System.out.println("  👥 Personas: " + totalPersonas);
            }
            rs.close();
            
            stmt.close();
            
            System.out.println();
            System.out.println("✅ Datos de prueba cargados correctamente");
            
        } catch (SQLException e) {
            System.err.println("⚠️  No hay datos de prueba");
            System.err.println("   Ejecuta database/data.sql para cargar datos iniciales");
        }
    }
}
