package com.sica.security;

import com.sica.exception.SicaException;
import com.sica.service.ReporteService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Módulo de Informe de Tasa de Ocupación por Hora.
 * 
 * Este módulo permite analizar patrones de ocupación del complejo,
 * generando reportes detallados hora por hora para un día específico.
 * 
 * Funcionalidades:
 * - Análisis de ocupación por franja horaria (24 horas)
 * - Identificación de horas pico
 * - Estadísticas de ocupación máxima
 * - Soporte para casos complejos:
 *   * Visitas que abarcan múltiples horas
 *   * Visitas iniciadas antes del día analizado
 *   * Visitas sin fecha de salida (aún dentro)
 * 
 * Uso intensivo de:
 * - API java.time para manejo de fechas y horas
 * - Stream API para procesamiento de datos
 * - Lambda expressions para filtrado y transformación
 * 
 * Principios SOLID aplicados:
 * - Single Responsibility: Solo maneja la interfaz de usuario del reporte
 * - Dependency Inversion: Depende de abstracciones (ReporteService)
 * 
 * @author SICA Team
 * @version 1.0
 * @see ReporteService#reporteOcupacionPorHora(LocalDate)
 */
public class Examen {
    
    private final ReporteService reporteService;
    private final Scanner scanner;
    private static final DateTimeFormatter FORMATO_FECHA_INPUT = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /**
     * Constructor que inicializa los servicios necesarios.
     * 
     * @throws SQLException si hay error al inicializar servicios
     */
    public Examen() throws SQLException {
        this.reporteService = new ReporteService();
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Muestra el menú principal del módulo de ocupación por hora.
     * Diseñado para ser integrado en el menú del Supervisor de Seguridad.
     */
    public void mostrarMenu() {
        boolean continuar = true;
        
        while (continuar) {
            limpiarPantalla();
            mostrarEncabezado();
            
            System.out.println("1. Generar Reporte de Ocupación por Hora");
            System.out.println("0. Volver al Menú Principal\n");
            
            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine().trim();
            
            switch (opcion) {
                case "1":
                    generarReporteOcupacion();
                    break;
                case "0":
                    continuar = false;
                    break;
                default:
                    System.out.println("\n❌ Opción no válida");
                    pausa();
            }
        }
    }
    
    /**
     * Genera el reporte de ocupación por hora solicitando la fecha al usuario.
     * 
     * Proceso:
     * 1. Solicita fecha al usuario en formato dd/MM/yyyy
     * 2. Valida el formato de la fecha
     * 3. Invoca el servicio de reportes
     * 4. Muestra el reporte generado
     * 5. Maneja errores de validación y permisos
     */
    private void generarReporteOcupacion() {
        limpiarPantalla();
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("        GENERAR REPORTE DE OCUPACIÓN POR HORA");
        System.out.println("═══════════════════════════════════════════════════════════════\n");
        
        System.out.println("Este reporte analiza la ocupación del complejo hora por hora");
        System.out.println("para una fecha específica, permitiendo identificar patrones");
        System.out.println("de afluencia y horas pico.\n");
        
        System.out.print("Ingrese la fecha a analizar (formato dd/MM/yyyy): ");
        String fechaInput = scanner.nextLine().trim();
        
        try {
            // Parsear la fecha ingresada
            LocalDate fecha = LocalDate.parse(fechaInput, FORMATO_FECHA_INPUT);
            
            // Validar que la fecha no sea futura
            if (fecha.isAfter(LocalDate.now())) {
                System.out.println("\n⚠️  ADVERTENCIA: La fecha ingresada es futura.");
                System.out.println("    Los datos pueden estar incompletos.\n");
                System.out.print("¿Desea continuar de todos modos? (S/N): ");
                String confirmacion = scanner.nextLine().trim().toUpperCase();
                
                if (!confirmacion.equals("S") && !confirmacion.equals("SI")) {
                    System.out.println("\n❌ Operación cancelada");
                    pausa();
                    return;
                }
            }
            
            System.out.println("\n⏳ Generando reporte...");
            System.out.println("   Analizando 24 horas de datos de ocupación...\n");
            
            // Generar el reporte usando el servicio
            String reporte = reporteService.reporteOcupacionPorHora(fecha);
            
            // Mostrar el reporte
            System.out.println(reporte);
            
            // Ofrecer opciones adicionales
            System.out.println("\n¿Qué desea hacer?");
            System.out.println("1. Generar otro reporte");
            System.out.println("2. Volver al menú");
            System.out.print("\nSeleccione una opción: ");
            String siguiente = scanner.nextLine().trim();
            
            if (siguiente.equals("1")) {
                generarReporteOcupacion(); // Recursivo para generar otro reporte
            }
            
        } catch (DateTimeParseException e) {
            System.out.println("\n❌ ERROR: Formato de fecha inválido");
            System.out.println("   Use el formato dd/MM/yyyy (ejemplo: 26/08/2025)");
            pausa();
        } catch (SicaException e) {
            System.out.println("\n❌ ERROR: " + e.getMessage());
            pausa();
        }
    }
    
    /**
     * Muestra el encabezado del módulo.
     */
    private void mostrarEncabezado() {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║         MÓDULO DE ANÁLISIS DE OCUPACIÓN POR HORA             ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Propósito: Optimizar asignación de personal de seguridad");
        System.out.println("           y limpieza mediante análisis de patrones de afluencia");
        System.out.println("───────────────────────────────────────────────────────────────\n");
    }
    
    /**
     * Limpia la pantalla de la consola.
     */
    private void limpiarPantalla() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Si falla la limpieza, simplemente agregar líneas en blanco
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
    
    /**
     * Pausa la ejecución hasta que el usuario presione Enter.
     */
    private void pausa() {
        System.out.print("\nPresione ENTER para continuar...");
        scanner.nextLine();
    }
    
    /**
     * Método principal para testing standalone del módulo.
     * 
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("   SICA - Módulo de Ocupación por Hora");
        System.out.println("   Modo de Prueba Independiente");
        System.out.println("═══════════════════════════════════════════════════════════════\n");
        
        try {
            Examen modulo = new Examen();
            modulo.mostrarMenu();
            
            System.out.println("\n✅ Módulo finalizado correctamente");
        } catch (SQLException e) {
            System.err.println("\n❌ Error al inicializar el módulo: " + e.getMessage());
            System.err.println("   Verifique la conexión a la base de datos");
        }
    }
}
