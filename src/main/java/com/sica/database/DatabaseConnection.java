package com.sica.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase Singleton que gestiona la conexión a la base de datos MySQL.
 * 
 * PATRÓN DE DISEÑO: SINGLETON
 * - Garantiza una única instancia de conexión a la base de datos
 * - Proporciona un punto de acceso global a la conexión
 * - Inicialización perezosa (lazy initialization)
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo gestiona la conexión a la base de datos
 * - Dependency Inversion: Otras clases dependen de Connection (interfaz), no de implementación
 * 
 * ¿Por qué Singleton aquí?
 * - Evita múltiples conexiones innecesarias a la BD (costosas en recursos)
 * - Centraliza la gestión de conexiones
 * - Facilita el control de transacciones
 * 
 * @author SICA Team
 * @version 1.0
 */
public class DatabaseConnection {
    
    // Instancia única de la clase (patrón Singleton)
    private static DatabaseConnection instance;
    
    // Conexión activa a la base de datos
    private Connection connection;
    
    /**
     * Constructor privado para evitar instanciación externa.
     * Implementa el patrón Singleton.
     * 
     * @throws SQLException si hay error al conectar
     */
    private DatabaseConnection() throws SQLException {
        try {
            // Cargar el driver JDBC de MySQL
            Class.forName(DatabaseConfig.DRIVER);
            
            // Establecer la conexión
            this.connection = DriverManager.getConnection(
                DatabaseConfig.FULL_URL,
                DatabaseConfig.USER,
                DatabaseConfig.PASSWORD
            );
            
            System.out.println("✅ Conexión a la base de datos establecida exitosamente");
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Error: Driver JDBC de MySQL no encontrado");
            System.err.println("Asegúrate de que mysql-connector-j.jar esté en la carpeta lib/");
            throw new SQLException("Driver JDBC no encontrado", e);
        } catch (SQLException e) {
            System.err.println("❌ Error al conectar a la base de datos MySQL");
            System.err.println("Verifica:");
            System.err.println("  1. MySQL está corriendo");
            System.err.println("  2. La base de datos 'sica' existe");
            System.err.println("  3. Usuario y contraseña en DatabaseConfig.java son correctos");
            System.err.println("  4. Puerto 3306 está disponible");
            throw e;
        }
    }
    
    /**
     * Obtiene la instancia única de DatabaseConnection (patrón Singleton).
     * Implementa lazy initialization: crea la instancia solo cuando se necesita.
     * 
     * IMPORTANTE: Este método NO es thread-safe. Si se requiere uso concurrente,
     * debe sincronizarse o usar inicialización eager.
     * 
     * @return Instancia única de DatabaseConnection
     * @throws SQLException si hay error al crear la conexión
     */
    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseConnection();
        } else if (instance.getConnection().isClosed()) {
            // Si la conexión se cerró, crear una nueva instancia
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Obtiene la conexión activa a la base de datos.
     * 
     * @return Conexión JDBC activa
     */
    public Connection getConnection() {
        return connection;
    }
    
    /**
     * Cierra la conexión a la base de datos.
     * 
     * IMPORTANTE: Solo debe llamarse al finalizar la aplicación.
     * Durante la ejecución normal, mantener la conexión abierta.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("✅ Conexión a la base de datos cerrada");
            } catch (SQLException e) {
                System.err.println("❌ Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
    
    /**
     * Verifica si la conexión está activa.
     * 
     * @return true si la conexión está activa, false en caso contrario
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Prueba la conexión a la base de datos.
     * Útil para diagnóstico y verificación inicial.
     * 
     * @return true si la conexión funciona correctamente
     */
    public boolean testConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                // Ejecutar una consulta simple de prueba
                var stmt = connection.createStatement();
                var rs = stmt.executeQuery("SELECT 1");
                boolean result = rs.next();
                rs.close();
                stmt.close();
                return result;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("❌ Error al probar la conexión: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene información sobre la conexión actual.
     * 
     * @return String con información de la conexión
     */
    public String getConnectionInfo() {
        try {
            if (connection != null && !connection.isClosed()) {
                var metaData = connection.getMetaData();
                return String.format(
                    "Conexión activa:\n" +
                    "  - Base de datos: %s\n" +
                    "  - Versión: %s\n" +
                    "  - Driver: %s\n" +
                    "  - Usuario: %s",
                    metaData.getDatabaseProductName(),
                    metaData.getDatabaseProductVersion(),
                    metaData.getDriverName(),
                    metaData.getUserName()
                );
            }
            return "No hay conexión activa";
        } catch (SQLException e) {
            return "Error al obtener información de la conexión: " + e.getMessage();
        }
    }
}
