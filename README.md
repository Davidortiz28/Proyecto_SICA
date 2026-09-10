# SICA - Sistema Integrado de Control de Acceso para Zona Acme

## 📋 Descripción

El Complejo Empresarial "Zona Acme" alberga más de 30 empresas y actualmente utiliza un sistema manual de control de acceso basado en libros de registro físicos y comunicación por radio.

### Problemas Actuales
- ❌ No existe un registro confiable de quién se encuentra dentro del complejo
- ❌ Las emergencias y evacuaciones son difíciles de gestionar
- ❌ Se forman largas filas en las entradas
- ❌ Los invitados no anunciados generan retrasos
- ❌ Investigar incidentes requiere revisar registros físicos
- ❌ No existe una forma eficiente de bloquear inmediatamente el acceso de una persona
- ❌ No existe una auditoría digital confiable de las acciones realizadas

### Solución: SICA
SICA es un sistema que permite digitalizar y automatizar el control de acceso, proporcionando:
- ✅ Registro digital de entradas y salidas
- ✅ Control de acceso basado en roles y permisos (RBAC)
- ✅ Gestión de visitas pre-registradas y no anunciadas
- ✅ Sistema de aprobaciones en tiempo real
- ✅ Auditoría completa de todas las operaciones críticas
- ✅ Reportes para análisis y toma de decisiones
- ✅ Gestión de incidentes
- ✅ Estados de acceso (bloqueo/desbloqueo de personas)

---

## 🚀 Tecnologías

| Tecnología | Versión Recomendada | Propósito |
|------------|---------------------|-----------|
| Java | 11+ | Lenguaje principal |
| JDBC | Incluido en JDK | Acceso a base de datos |
| MySQL / MariaDB | 8.0+ / 10.5+ | Base de datos relacional |
| Git | 2.x | Control de versiones |

**Nota:** Este proyecto NO utiliza frameworks externos (Spring, Hibernate, etc.) para mantener la simplicidad y enfocarse en los fundamentos de Java y arquitectura de software.

---

## 🏗️ Arquitectura

El proyecto sigue una **arquitectura MVC (Model-View-Controller)** estrictamente separada en capas:

### Estructura de Directorios

```
SICA/
│
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── sica/
│                   ├── Main.java                          # Punto de entrada
│                   │
│                   ├── model/                             # CAPA MODEL
│                   │   ├── entity/                        # Entidades del dominio
│                   │   │   ├── Usuario.java
│                   │   │   ├── Rol.java
│                   │   │   ├── Permiso.java
│                   │   │   ├── Empresa.java
│                   │   │   ├── Persona.java
│                   │   │   ├── Visita.java
│                   │   │   ├── Incidente.java
│                   │   │   ├── BitacoraAuditoria.java
│                   │   │   ├── PersonaEstadoAcceso.java
│                   │   │   └── VisitaEstado.java
│                   │   │
│                   │   └── enums/                         # Enumeraciones
│                   │       ├── TipoPersona.java
│                   │       └── AccionAuditoria.java
│                   │
│                   ├── view/                              # CAPA VIEW
│                   │   ├── LoginView.java
│                   │   ├── MenuPrincipalView.java
│                   │   ├── usuario/
│                   │   │   ├── UsuarioFormView.java
│                   │   │   └── UsuarioListaView.java
│                   │   ├── persona/
│                   │   │   ├── PersonaFormView.java
│                   │   │   ├── PersonaListaView.java
│                   │   │   └── PersonaBusquedaView.java
│                   │   ├── visita/
│                   │   │   ├── VisitaFormView.java
│                   │   │   ├── VisitaListaView.java
│                   │   │   ├── CheckInView.java
│                   │   │   ├── CheckOutView.java
│                   │   │   └── AprobacionVisitaView.java
│                   │   ├── incidente/
│                   │   │   └── IncidenteFormView.java
│                   │   ├── reporte/
│                   │   │   └── ReporteView.java
│                   │   └── util/
│                   │       └── ViewUtil.java              # Utilidades para vistas
│                   │
│                   ├── controller/                        # CAPA CONTROLLER
│                   │   ├── AuthController.java
│                   │   ├── UsuarioController.java
│                   │   ├── EmpresaController.java
│                   │   ├── PersonaController.java
│                   │   ├── VisitaController.java
│                   │   ├── IncidenteController.java
│                   │   └── ReporteController.java
│                   │
│                   ├── service/                           # CAPA SERVICE (Lógica de negocio)
│                   │   ├── AuthService.java
│                   │   ├── UsuarioService.java
│                   │   ├── RolService.java
│                   │   ├── PermisoService.java
│                   │   ├── EmpresaService.java
│                   │   ├── PersonaService.java
│                   │   ├── VisitaService.java
│                   │   ├── IncidenteService.java
│                   │   ├── AuditoriaService.java
│                   │   ├── ReporteService.java
│                   │   └── AuthorizationService.java      # Servicio RBAC
│                   │
│                   ├── repository/                        # CAPA REPOSITORY (Acceso a datos)
│                   │   ├── UsuarioRepository.java
│                   │   ├── RolRepository.java
│                   │   ├── PermisoRepository.java
│                   │   ├── EmpresaRepository.java
│                   │   ├── PersonaRepository.java
│                   │   ├── VisitaRepository.java
│                   │   ├── IncidenteRepository.java
│                   │   └── BitacoraAuditoriaRepository.java
│                   │
│                   ├── database/                          # Gestión de base de datos
│                   │   ├── DatabaseConnection.java        # Singleton para conexión
│                   │   └── DatabaseConfig.java            # Configuración
│                   │
│                   ├── security/                          # Seguridad
│                   │   ├── SessionManager.java            # Singleton para sesión actual
│                   │   ├── PasswordUtil.java              # Utilidad para contraseñas
│                   │   └── PermissionValidator.java       # Validador de permisos
│                   │
│                   ├── exception/                         # Excepciones personalizadas
│                   │   ├── SicaException.java
│                   │   ├── UsuarioNoEncontradoException.java
│                   │   ├── PermisoDenegadoException.java
│                   │   ├── PersonaBloqueadaException.java
│                   │   ├── VisitaNoEncontradaException.java
│                   │   ├── VisitaNoAprobadaException.java
│                   │   ├── CredencialesInvalidasException.java
│                   │   └── UsuarioInactivoException.java
│                   │
│                   └── util/                              # Utilidades generales
│                       ├── DateUtil.java
│                       ├── ValidatorUtil.java
│                       └── InputUtil.java
│
├── database/
│   ├── schema.sql                                         # Script de creación de BD
│   └── data.sql                                           # Script de datos iniciales
│
├── .gitignore
└── README.md
```

### Responsabilidades de Cada Capa

#### 📦 Model
Representa las entidades del dominio del sistema. Contiene:
- Clases POJO con atributos, getters y setters
- Enumeraciones para tipos constantes
- **No contiene** lógica de negocio ni acceso a datos

**Ejemplos:** Usuario, Rol, Permiso, Empresa, Persona, Visita, Incidente, BitacoraAuditoria

#### 👁️ View
Responsable de la interacción con el usuario. Contiene:
- Menús y formularios de consola
- Captura y validación básica de entrada del usuario
- Presentación de resultados
- **No contiene** lógica de negocio

**Ejemplos:** LoginView, MenuPrincipalView, PersonaFormView, CheckInView

#### 🎮 Controller
Coordina la comunicación entre View y Service. Contiene:
- Recepción de acciones del usuario desde la View
- Invocación de servicios correspondientes
- Manejo de excepciones
- Devolución de resultados a la View
- **No contiene** lógica de negocio compleja

**Ejemplos:** AuthController, UsuarioController, VisitaController

#### 💼 Service
Contiene toda la lógica de negocio. Responsable de:
- Validaciones de reglas de negocio
- Implementación de RBAC
- Orquestación de operaciones complejas
- Invocación de auditoría
- Coordinación entre múltiples repositorios
- **No accede directamente a la base de datos** (usa Repository)

**Ejemplos:** AuthService, VisitaService, AuthorizationService, AuditoriaService

#### 🗄️ Repository
Capa de acceso a datos. Responsable de:
- Operaciones CRUD en la base de datos
- Consultas SQL
- Mapeo de ResultSet a entidades
- **No contiene** lógica de negocio

**Ejemplos:** UsuarioRepository, PersonaRepository, VisitaRepository

---

## 🎯 Principios SOLID

El proyecto aplica rigurosamente los principios SOLID:

### S - Single Responsibility Principle (Principio de Responsabilidad Única)
Cada clase tiene una única responsabilidad:
- **Repository:** Solo acceso a datos
- **Service:** Solo lógica de negocio específica
- **Controller:** Solo coordinar View y Service
- **View:** Solo interacción con usuario

**Ejemplo:** `UsuarioRepository` solo maneja operaciones CRUD de usuarios en BD. `UsuarioService` maneja la lógica de negocio (validaciones, RBAC). `UsuarioController` coordina ambas capas.

### O - Open/Closed Principle (Principio Abierto/Cerrado)
Las clases están abiertas para extensión pero cerradas para modificación:
- Uso de interfaces en Repository para permitir cambiar implementación sin afectar servicios
- Strategy Pattern para diferentes validaciones sin modificar código existente
- Factory Pattern para crear objetos según contexto

**Ejemplo:** Si queremos cambiar de MySQL a PostgreSQL, solo modificamos la implementación del Repository, no los servicios.

### L - Liskov Substitution Principle (Principio de Sustitución de Liskov)
Las clases derivadas pueden sustituir a sus clases base:
- Jerarquía de excepciones: todas heredan de `SicaException`
- Interfaces de Repository pueden ser implementadas por diferentes proveedores

**Ejemplo:** Cualquier clase que herede de `SicaException` puede ser tratada como tal en los bloques catch.

### I - Interface Segregation Principle (Principio de Segregación de Interfaces)
Interfaces específicas en lugar de interfaces grandes:
- `UsuarioRepository`, `PersonaRepository`, `VisitaRepository` separados
- No existe un `GenericRepository` gigante con todos los métodos

**Ejemplo:** Un servicio que solo necesita usuarios no se ve forzado a depender de métodos de visitas.

### D - Dependency Inversion Principle (Principio de Inversión de Dependencias)
Las capas de alto nivel no dependen de implementaciones concretas:
- Los servicios dependen de interfaces de Repository
- Los controladores dependen de interfaces de Service

**Ejemplo:** `UsuarioService` depende de `IUsuarioRepository` (interfaz), no de `UsuarioRepositoryImpl` (implementación concreta).

---

## 🎨 Patrones de Diseño

El proyecto implementa mínimo **6 patrones de diseño**:

### 1. Repository Pattern
| Aspecto | Descripción |
|---------|-------------|
| **Ubicación** | Paquete `repository/` |
| **Problema que resuelve** | Separar la lógica de acceso a datos de la lógica de negocio |
| **Por qué se utiliza** | Permite cambiar la implementación de persistencia (ej: MySQL → PostgreSQL) sin afectar los servicios |
| **Implementación** | Cada entidad tiene su repositorio: `UsuarioRepository`, `PersonaRepository`, etc. |

### 2. Singleton Pattern
| Aspecto | Descripción |
|---------|-------------|
| **Ubicación** | `DatabaseConnection.java` y `SessionManager.java` |
| **Problema que resuelve** | Garantizar una única instancia de recursos críticos |
| **Por qué se utiliza** | Evita múltiples conexiones a BD (costoso) y conflictos de sesión (solo un usuario activo) |
| **Implementación** | Constructor privado + método `getInstance()` con inicialización lazy o eager |

### 3. Factory Pattern
| Aspecto | Descripción |
|---------|-------------|
| **Ubicación** | `service/factory/` o dentro de servicios específicos |
| **Problema que resuelve** | Centralizar y simplificar la creación de objetos complejos |
| **Por qué se utiliza** | Crear diferentes tipos de reportes o estrategias según el contexto |
| **Implementación** | `ReporteFactory.crearReporte(TipoReporte tipo)` |

### 4. Strategy Pattern
| Aspecto | Descripción |
|---------|-------------|
| **Ubicación** | `service/validation/` o dentro de servicios |
| **Problema que resuelve** | Aplicar diferentes algoritmos de validación según el tipo de operación |
| **Por qué se utiliza** | Permite extender validaciones sin modificar código existente (Open/Closed) |
| **Implementación** | `ValidacionStrategy` con implementaciones: `ValidacionTrabajador`, `ValidacionInvitado` |

### 5. Observer Pattern
| Aspecto | Descripción |
|---------|-------------|
| **Ubicación** | `service/notification/` o `observer/` |
| **Problema que resuelve** | Notificar cambios de estado de visitas pendientes (concurrencia) |
| **Por qué se utiliza** | Permite que el Guarda se actualice automáticamente cuando el Funcionario aprueba/rechaza |
| **Implementación** | `VisitaObserver` que notifica cambios a observadores suscritos |

### 6. Facade Pattern
| Aspecto | Descripción |
|---------|-------------|
| **Ubicación** | Servicios complejos que coordinan múltiples operaciones |
| **Problema que resuelve** | Simplificar operaciones que involucran múltiples servicios |
| **Por qué se utiliza** | El flujo de "salida olvidada" requiere: cerrar visita anterior + crear nueva + auditar |
| **Implementación** | `VisitaService.procesarSalidaOlvidada()` encapsula toda la complejidad |

---

## 🔐 Sistema RBAC (Role-Based Access Control)

### Concepto
SICA implementa un sistema de autorización basado en **roles y permisos** almacenados en la base de datos.

### Flujo de Autorización
```
Usuario → Rol → Permisos
```

### Ejemplo de Roles y Permisos

#### Rol: Superusuario
```
- gestionar_usuarios
- gestionar_roles
- gestionar_permisos
- gestionar_empresas
- gestionar_personas
- registrar_visita
- registrar_check_in
- registrar_check_out
- registrar_incidente
- bloquear_persona
- generar_reporte
- consultar_bitacora
```

#### Rol: Guarda de Seguridad
```
- buscar_persona
- registrar_visita
- registrar_check_in
- registrar_check_out
```

#### Rol: Supervisor de Seguridad
```
- buscar_persona
- registrar_visita
- registrar_check_in
- registrar_check_out
- registrar_incidente
- bloquear_persona
- generar_reporte
- consultar_bitacora
```

#### Rol: Funcionario de Empresa
```
- registrar_invitado
- aprobar_visita
- rechazar_visita
- consultar_visitas
```

### Implementación
Los permisos **NO** se verifican mediante código como:
```java
// ❌ MAL - No hacer esto
if (usuario.getRol().equals("Administrador")) {
    // permitir operación
}
```

En su lugar, se verifica mediante:
```java
// ✅ BIEN - Verificar permiso desde BD
if (authorizationService.tienePermiso(usuario, "gestionar_usuarios")) {
    // permitir operación
} else {
    throw new PermisoDenegadoException("No tienes permiso para gestionar usuarios");
}
```

### Auditoría de Intentos
Cuando un usuario intenta realizar una operación sin permiso:
- ✅ La operación se detiene inmediatamente
- ✅ No se modifica la base de datos
- ✅ Se muestra un mensaje claro al usuario
- ✅ Se registra el intento en la bitácora de auditoría (opcional según criticidad)

---

## 📊 Módulo de Auditoría

### Propósito
Registrar todas las operaciones críticas realizadas en el sistema para:
- Rastrear quién hizo qué y cuándo
- Investigar incidentes de seguridad
- Cumplir con requisitos de auditoría
- Analizar patrones de uso

### Tabla: `bitacora_auditoria`
```sql
id                   BIGINT AUTO_INCREMENT PRIMARY KEY
usuario_id           INT (FK → usuarios)
fecha_hora           TIMESTAMP
accion_realizada     VARCHAR(255)
tabla_afectada       VARCHAR(100)
registro_id_afectado INT
detalles             TEXT
```

### Acciones Auditadas (Mínimo)
- `LOGIN_EXITOSO`
- `LOGIN_FALLIDO`
- `CREACION_USUARIO`
- `ACTUALIZACION_USUARIO`
- `ELIMINACION_USUARIO`
- `CREACION_PERSONA`
- `ACTUALIZACION_PERSONA`
- `ELIMINACION_PERSONA`
- `CREACION_EMPRESA`
- `ACTUALIZACION_EMPRESA`
- `ELIMINACION_EMPRESA`
- `CAMBIO_ESTADO_PERSONA`
- `CREACION_INCIDENTE`
- `CHECK_IN`
- `CHECK_OUT`
- `APROBACION_VISITA`
- `RECHAZO_VISITA`
- `REGULARIZACION_SALIDA_OLVIDADA`

### Implementación
La auditoría se registra principalmente desde la **capa Service** mediante `AuditoriaService`:

```java
// Ejemplo de uso
auditoriaService.registrar(
    usuarioActual.getId(),
    AccionAuditoria.CREACION_PERSONA,
    "personas",
    persona.getId(),
    "Persona creada: " + persona.getNombre()
);
```

**Ventaja:** Centralización del registro de auditoría, evitando código repetitivo en cada Controller.

---

## 🚪 Flujos Principales del Sistema

### 1. Login
```
1. Usuario ingresa email y contraseña
2. Sistema busca usuario en BD
3. Sistema verifica que existe
4. Sistema verifica que está activo (esta_activo = TRUE)
5. Sistema verifica contraseña
6. Sistema obtiene rol del usuario
7. Sistema obtiene permisos asociados al rol
8. Sistema crea sesión (SessionManager)
9. Sistema registra LOGIN_EXITOSO en auditoría
10. Sistema muestra menú según permisos
```

**En caso de error:**
- Usuario no existe → `UsuarioNoEncontradoException`
- Usuario inactivo → `UsuarioInactivoException`
- Contraseña incorrecta → `CredencialesInvalidasException`
- Registrar `LOGIN_FALLIDO` en auditoría

### 2. Invitado Pre-Registrado
```
1. Funcionario de Empresa registra invitado previamente
   - Datos personales (nombre, documento, foto)
   - Empresa que visita
   - Fecha y hora esperada
   - Estado inicial: "Aprobado"
2. Invitado llega al complejo
3. Guarda busca invitado por documento
4. Sistema muestra:
   - Nombre, documento, foto
   - Empresa y persona que visita
   - Estado de acceso (Activo / Con Prohibición de Ingreso)
   - Estado de visita (Aprobado)
5. Si estado_acceso = "Con Prohibición de Ingreso" → Denegar acceso
6. Si estado_visita = "Aprobado" → Guarda realiza check-in
7. Sistema registra:
   - Hora de entrada actual (fecha_entrada)
   - Cambio de estado_visita_id
8. Sistema registra CHECK_IN en auditoría
```

### 3. Invitado No Anunciado
```
1. Invitado llega sin registro previo
2. Guarda busca por documento
3. Si no existe como persona:
   a. Guarda registra nueva persona (tipo: Invitado)
4. Guarda crea visita con estado "Pendiente de Aprobación"
5. Sistema notifica al Funcionario de Empresa correspondiente
6. Funcionario puede:
   
   OPCIÓN A: Aprobar
   - Estado cambia a "Aprobado"
   - Sistema registra APROBACION_VISITA en auditoría
   - Guarda puede proceder con check-in
   
   OPCIÓN B: Rechazar
   - Estado cambia a "Rechazado"
   - Sistema registra RECHAZO_VISITA en auditoría
   - Acceso denegado
```

**Concurrencia:** El sistema debe reflejar el cambio de estado realizado por el Funcionario en la interfaz del Guarda (polling, refresh, u Observer Pattern).

### 4. Trabajador Sin Carnet
```
1. Trabajador llega sin documento/carnet
2. Guarda busca trabajador por nombre o documento
3. Sistema verifica que existe como tipo "Trabajador"
4. Guarda crea solicitud temporal:
   - Estado: "Pendiente de Aprobación por Olvido"
5. Funcionario de Empresa recibe solicitud
6. Funcionario puede:
   
   OPCIÓN A: Aprobar
   - Estado cambia a "Aprobado"
   - Se permite ingreso puntual
   - Sistema registra auditoría
   
   OPCIÓN B: Rechazar
   - Acceso denegado
```

**Importante:** La aprobación NO modifica permanentemente el estado del trabajador, es solo para ese ingreso específico.

### 5. Salida Olvidada (Regularización)
```
Escenario:
- Visita #100: estado "Dentro", fecha_entrada: 2026-09-02 08:00, fecha_salida: NULL
- Persona abandonó el complejo sin registrar salida
- Persona intenta ingresar nuevamente

Flujo:
1. Guarda busca persona por documento
2. Sistema detecta visita anterior abierta (fecha_salida = NULL)
3. Sistema NO bloquea a la persona
4. Sistema ejecuta:
   a. Cerrar visita anterior:
      - fecha_salida = fecha_entrada + 12 horas (estimación)
      - estado = "Cerrada por Sistema (Salida Olvidada)"
   b. Crear nueva visita
   c. Registrar REGULARIZACION_SALIDA_OLVIDADA en auditoría
5. Guarda procede con check-in normal
```

### 6. Bloqueo de Persona
```
1. Supervisor de Seguridad selecciona persona
2. Sistema verifica permiso: "bloquear_persona"
3. Si tiene permiso:
   a. Cambiar estado_acceso_id a "Con Prohibición de Ingreso"
   b. Registrar CAMBIO_ESTADO_PERSONA en auditoría
4. La persona ya no podrá ingresar hasta ser desbloqueada
```

### 7. Registro de Incidente
```
1. Usuario con permiso "registrar_incidente" abre formulario
2. Ingresa:
   - Visita relacionada (opcional)
   - Descripción del incidente
   - Fecha y hora
3. Sistema registra:
   - Incidente en tabla "incidentes"
   - reportado_por_id = usuario actual
4. Sistema registra CREACION_INCIDENTE en auditoría
```

### 8. Generación de Reportes
```
1. Usuario con permiso "generar_reporte" accede al módulo
2. Selecciona tipo de reporte:
   - Personas actualmente dentro
   - Historial de visitas
   - Incidentes
   - Auditoría
   - Personas bloqueadas
3. Sistema verifica permisos específicos si aplica
4. Sistema genera reporte usando Stream API y Lambdas
5. Sistema muestra resultados
```

---

## 🗄️ Base de Datos

### Diagrama Entidad-Relación

```
┌─────────────┐         ┌──────────────┐         ┌────────────┐
│   roles     │◄────────┤ rol_permisos │────────►│  permisos  │
└─────────────┘         └──────────────┘         └────────────┘
      ▲
      │
      │
┌─────────────┐
│  usuarios   │
└─────────────┘
      │
      └──────────────┬────────────────────┐
                     │                    │
            ┌────────▼────────┐   ┌───────▼────────┐
            │   incidentes    │   │ bitacora_      │
            └─────────────────┘   │ auditoria      │
                     ▲            └────────────────┘
                     │
            ┌────────┴────────┐
            │    visitas      │
            └─────────────────┘
                     ▲
                     │
            ┌────────┴────────┐         ┌──────────────────────┐
            │    personas     │────────►│ persona_estados_     │
            └─────────────────┘         │ acceso               │
                     │                  └──────────────────────┘
                     ▼
            ┌─────────────────┐
            │    empresas     │
            └─────────────────┘
```

### Modelo Relacional

**Ver archivo:** `database/schema.sql`

El modelo incluye:
- ✅ Tablas de seguridad: `usuarios`, `roles`, `permisos`, `rol_permisos`
- ✅ Tablas de dominio: `empresas`, `personas`, `visitas`, `incidentes`
- ✅ Tablas de estado: `persona_estados_acceso`, `visita_estados`
- ✅ Tabla de auditoría: `bitacora_auditoria`
- ✅ Relaciones con FOREIGN KEY
- ✅ Restricciones de integridad

### Estados del Sistema

#### Estados de Persona (`persona_estados_acceso`)
- `Activo`: Puede ingresar al complejo
- `Con Prohibición de Ingreso`: No puede ingresar

#### Estados de Visita (`visita_estados`)
- `Dentro`: Persona actualmente dentro del complejo
- `Fuera`: Persona salió del complejo
- `Pendiente de Aprobación`: Esperando aprobación de funcionario
- `Aprobado`: Visita pre-aprobada, puede hacer check-in
- `Rechazado`: Visita rechazada, no puede ingresar
- `Expirado`: Visita aprobada pero no se presentó en el tiempo esperado
- `Cerrada por Sistema (Salida Olvidada)`: Sistema cerró automáticamente por salida no registrada
- `Pendiente de Aprobación por Olvido`: Trabajador sin carnet esperando aprobación

---

## 📥 Instalación

### Prerrequisitos
- [x] Java JDK 11 o superior instalado
- [x] MySQL o MariaDB instalado y en ejecución
- [x] Git instalado
- [x] IDE (opcional): IntelliJ IDEA, Eclipse, NetBeans, o VS Code

### Paso 1: Clonar el Repositorio
```bash
git clone https://github.com/tu-usuario/sica.git
cd sica
```

### Paso 2: Configurar la Base de Datos

1. **Crear la base de datos:**
```sql
CREATE DATABASE sica CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **Ejecutar el script de estructura:**
```bash
mysql -u root -p sica < database/schema.sql
```

3. **Ejecutar el script de datos iniciales:**
```bash
mysql -u root -p sica < database/data.sql
```

### Paso 3: Configurar la Conexión a BD

Editar el archivo `src/main/java/com/sica/database/DatabaseConfig.java`:

```java
public class DatabaseConfig {
    public static final String URL = "jdbc:mysql://localhost:3306/sica";
    public static final String USER = "tu_usuario";
    public static final String PASSWORD = "tu_contraseña";
}
```

### Paso 4: Compilar el Proyecto

```bash
# Desde la raíz del proyecto
javac -d bin -sourcepath src/main/java src/main/java/com/sica/Main.java
```

O desde tu IDE favorito: `Build → Build Project`

### Paso 5: Ejecutar el Sistema

```bash
java -cp bin com.sica.Main
```

O desde tu IDE: `Run → Run 'Main'`

---

## 👤 Usuarios de Prueba

El archivo `database/data.sql` incluye los siguientes usuarios para pruebas:

| Rol | Email | Contraseña | Descripción |
|-----|-------|------------|-------------|
| Superusuario | superuser@sica.com | super123 | Acceso completo al sistema |
| Supervisor de Seguridad | supervisor@sica.com | supervisor123 | Gestión de seguridad e incidentes |
| Guarda de Seguridad | guarda@sica.com | guarda123 | Control de acceso diario |
| Funcionario de Empresa | funcionario@empresaA.com | funcionario123 | Gestión de invitados de Empresa A |

**⚠️ IMPORTANTE:** Estas contraseñas son solo para ambiente de desarrollo/pruebas. En un entorno real, las contraseñas deben estar hasheadas con algoritmos seguros (BCrypt, Argon2, etc.).

---

## 🎮 Guía de Uso

### 1. Iniciar Sesión
```
1. Ejecutar aplicación
2. Seleccionar opción "Iniciar Sesión"
3. Ingresar email y contraseña
4. Sistema muestra menú según rol
```

### 2. Registrar Invitado Pre-Registrado (Funcionario)
```
1. Login como Funcionario de Empresa
2. Seleccionar "Gestionar Invitados"
3. Seleccionar "Registrar Nuevo Invitado"
4. Ingresar datos:
   - Nombre
   - Documento de identidad
   - Empresa a visitar
   - Fecha y hora esperada
   - Foto (URL o ruta)
5. Sistema crea visita con estado "Aprobado"
```

### 3. Check-In de Invitado (Guarda)
```
1. Login como Guarda de Seguridad
2. Seleccionar "Control de Acceso"
3. Seleccionar "Registrar Entrada (Check-In)"
4. Ingresar documento del invitado
5. Sistema muestra información de la persona y visita
6. Si todo está correcto, confirmar check-in
7. Sistema registra entrada
```

### 4. Aprobar Invitado No Anunciado
```
GUARDA:
1. Seleccionar "Registrar Invitado No Anunciado"
2. Ingresar datos del invitado
3. Sistema crea visita "Pendiente de Aprobación"
4. Esperar respuesta del funcionario

FUNCIONARIO:
1. Seleccionar "Aprobar/Rechazar Visitas"
2. Ver lista de visitas pendientes
3. Seleccionar visita
4. Aprobar o Rechazar
5. Sistema actualiza estado

GUARDA:
1. Refrescar estado de visita
2. Si fue aprobada, proceder con check-in
```

### 5. Bloquear Persona
```
1. Login como Supervisor de Seguridad
2. Seleccionar "Gestionar Personas"
3. Buscar persona por documento
4. Seleccionar "Cambiar Estado de Acceso"
5. Cambiar a "Con Prohibición de Ingreso"
6. Confirmar
7. Sistema registra cambio en auditoría
```

### 6. Generar Reportes
```
1. Login con usuario que tenga permiso "generar_reporte"
2. Seleccionar "Reportes"
3. Seleccionar tipo de reporte:
   - Personas actualmente dentro
   - Historial de visitas (rango de fechas)
   - Incidentes
   - Auditoría
4. Ingresar parámetros si aplica
5. Sistema genera y muestra reporte
```

### 7. Generar Reporte de Ocupación por Hora
```
1. Login como Supervisor de Seguridad (o rol con permiso "generar_reporte")
2. Seleccionar "Reportes"
3. Seleccionar "⭐ Reporte de Ocupación por Hora"
4. Ingresar fecha a analizar (formato dd/MM/yyyy)
5. Sistema analiza las 24 horas del día especificado
6. Sistema muestra:
   - Tabla con ocupación por franja horaria (00:00 - 23:59)
   - Número de personas dentro en cada hora
   - Estadísticas de ocupación máxima
   - Identificación de horas pico
7. Resultado puede usarse para:
   - Optimizar asignación de personal de seguridad
   - Planificar horarios de limpieza
   - Identificar patrones de afluencia
   - Toma de decisiones operativas

Casos Especiales Manejados:
- ✅ Visitas que abarcan múltiples horas
- ✅ Visitas iniciadas antes del día analizado que continúan en él
- ✅ Visitas sin fecha de salida (aún dentro)
- ✅ Visitas que inician en el día pero finalizan después
```

---

## 🔄 Git Flow

Este proyecto sigue la estrategia **Git Flow** para control de versiones:

### Ramas Principales
- `main`: Código en producción (solo releases estables)
- `develop`: Rama de integración para desarrollo

### Ramas de Soporte
- `feature/*`: Nuevas funcionalidades
- `release/*`: Preparación de releases
- `hotfix/*`: Correcciones urgentes en producción

### Flujo de Trabajo

```bash
# 1. Crear rama de funcionalidad desde develop
git checkout develop
git pull origin develop
git checkout -b feature/nombre-funcionalidad

# 2. Desarrollar la funcionalidad con commits convencionales
git add .
git commit -m "feat: agregar validación de permisos RBAC"

# 3. Actualizar con cambios de develop
git checkout develop
git pull origin develop
git checkout feature/nombre-funcionalidad
git merge develop

# 4. Finalizar funcionalidad: merge a develop
git checkout develop
git merge --no-ff feature/nombre-funcionalidad
git push origin develop

# 5. Crear release
git checkout -b release/v1.0.0 develop
# Ajustes finales, actualizar versión, etc.
git checkout main
git merge --no-ff release/v1.0.0
git tag -a v1.0.0 -m "Release v1.0.0"
git checkout develop
git merge --no-ff release/v1.0.0
git push origin --all
git push origin --tags
```

### Conventional Commits

Todos los commits deben seguir el formato:

```
<tipo>: <descripción breve>

[cuerpo opcional]

[footer opcional]
```

#### Tipos de Commits
- `feat`: Nueva funcionalidad
- `fix`: Corrección de bug
- `docs`: Cambios en documentación
- `style`: Formato, no afecta funcionalidad
- `refactor`: Refactorización de código
- `test`: Agregar o modificar tests
- `chore`: Tareas de mantenimiento

#### Ejemplos
```bash
feat: implementar autenticación de usuarios
fix: corregir validación de visitas pendientes
refactor: separar lógica de negocio del controlador
docs: actualizar README con instrucciones de instalación
test: agregar pruebas para autorización RBAC
```

---

## 🧪 Pruebas

### Casos de Prueba Mínimos

#### Autenticación
- [x] Login con credenciales correctas
- [x] Login con contraseña incorrecta
- [x] Login con usuario inexistente
- [x] Login con usuario inactivo
- [x] Logout correcto

#### RBAC (Autorización)
- [x] Usuario con permiso puede ejecutar operación
- [x] Usuario sin permiso recibe `PermisoDenegadoException`
- [x] Verificar permisos desde base de datos, no hardcodeados

#### Gestión de Personas
- [x] Crear persona (Trabajador/Invitado)
- [x] Actualizar datos de persona
- [x] Eliminar persona
- [x] Bloquear persona (cambiar estado a "Con Prohibición de Ingreso")
- [x] Desbloquear persona

#### Gestión de Visitas
- [x] Crear visita pre-registrada con estado "Aprobado"
- [x] Crear visita no anunciada con estado "Pendiente de Aprobación"
- [x] Aprobar visita pendiente
- [x] Rechazar visita pendiente
- [x] Check-in exitoso
- [x] Check-in con persona bloqueada (debe fallar)
- [x] Check-out exitoso
- [x] Regularización de salida olvidada

#### Auditoría
- [x] Verificar registro de LOGIN_EXITOSO
- [x] Verificar registro de CREACION_PERSONA
- [x] Verificar registro de CHECK_IN
- [x] Verificar registro de CAMBIO_ESTADO_PERSONA
- [x] Verificar registro de REGULARIZACION_SALIDA_OLVIDADA

#### Reportes
- [x] Generar reporte de personas dentro
- [x] Generar historial de visitas por rango de fechas
- [x] Generar reporte de incidentes
- [x] Verificar que solo usuarios con permiso puedan generar reportes

---

## 🌟 Características Técnicas Destacadas

### Uso de Lambdas y Stream API
El proyecto utiliza características modernas de Java donde sea apropiado:

```java
// Ejemplo: Filtrar visitas de un día específico
List<Visita> visitasHoy = visitas.stream()
    .filter(v -> DateUtil.esHoy(v.getFechaEntrada()))
    .collect(Collectors.toList());

// Ejemplo: Contar personas dentro del complejo
long personasDentro = visitas.stream()
    .filter(v -> v.getEstado().getNombreEstado().equals("Dentro"))
    .count();

// Ejemplo: Agrupar visitas por empresa
Map<String, List<Visita>> visitasPorEmpresa = visitas.stream()
    .collect(Collectors.groupingBy(v -> v.getPersona().getEmpresa().getNombre()));
```

### Manejo de Transacciones
Operaciones críticas se ejecutan de forma atómica:

```java
public void procesarSalidaOlvidada(Persona persona) {
    Connection conn = null;
    try {
        conn = DatabaseConnection.getInstance().getConnection();
        conn.setAutoCommit(false); // Iniciar transacción
        
        // 1. Cerrar visita anterior
        cerrarVisitaAnterior(persona.getId());
        
        // 2. Crear nueva visita
        crearNuevaVisita(persona.getId());
        
        // 3. Registrar auditoría
        auditoriaService.registrar(...);
        
        conn.commit(); // Confirmar transacción
    } catch (Exception e) {
        if (conn != null) conn.rollback(); // Revertir en caso de error
        throw new SicaException("Error al procesar salida olvidada", e);
    } finally {
        if (conn != null) conn.setAutoCommit(true);
    }
}
```

### Manejo de Concurrencia
Para el escenario de visitas pendientes:

**Solución implementada:** Observer Pattern con polling

```java
// El guarda puede refrescar el estado de una visita
public void refrescarEstadoVisita(int visitaId) {
    Visita visitaActualizada = visitaRepository.findById(visitaId);
    // Notificar a la vista si cambió el estado
}
```

**Nota:** En un entorno real con interfaz web, se usaría WebSockets o Server-Sent Events para notificaciones en tiempo real.

---

## 📚 Recursos Adicionales

### Referencias de Patrones de Diseño
- [Refactoring.Guru - Design Patterns](https://refactoring.guru/design-patterns)
- Repository Pattern: [Martin Fowler - Repository](https://martinfowler.com/eaaCatalog/repository.html)

### Referencias de SOLID
- [SOLID Principles - Wikipedia](https://en.wikipedia.org/wiki/SOLID)
- [Uncle Bob - SOLID Principles](http://butunclebob.com/ArticleS.UncleBob.PrinciplesOfOod)

### Java Stream API y Lambdas
- [Oracle - Java Tutorials: Lambda Expressions](https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html)
- [Oracle - Java Tutorials: Stream API](https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html)

---

## 👥 Contribuciones

Este es un proyecto académico. Las contribuciones deben seguir:
1. Git Flow para ramas
2. Conventional Commits para mensajes
3. Principios SOLID
4. Patrones de diseño apropiados
5. Código limpio y documentado

---

## 📄 Licencia

Este proyecto es de uso académico.

---

## 👨‍💻 Autor

**[Tu Nombre]**
- GitHub: [@tu-usuario](https://github.com/tu-usuario)
- Email: tu.email@ejemplo.com

---

## 📋 Estado del Proyecto

**Versión:** 1.0.0 (En desarrollo)

### Módulos Completados
- [ ] Estructura base del proyecto
- [ ] Base de datos (schema.sql y data.sql)
- [ ] Modelo (entidades)
- [ ] Capa Repository
- [ ] Autenticación y RBAC
- [ ] Módulo de Empresas
- [ ] Módulo de Personas
- [ ] Módulo de Visitas
- [ ] Módulo de Incidentes
- [ ] Módulo de Reportes
- [ ] Módulo de Auditoría
- [ ] Interfaz de Usuario (Views)
- [ ] Pruebas completas
- [ ] Documentación final

---

## 🔮 Trabajo Futuro

### Posibles Mejoras (No prioritarias para v1.0)
- [ ] Interfaz gráfica (JavaFX o Swing)
- [ ] API REST para integración con otros sistemas
- [ ] Notificaciones en tiempo real (WebSockets)
- [ ] Dashboard con estadísticas
- [ ] Exportación de reportes a PDF/Excel
- [ ] Sistema de notificaciones por email
- [ ] Autenticación con tokens JWT
- [ ] Integración con lectores de código de barras/QR
- [ ] App móvil para guardas

---

## ❓ FAQ (Preguntas Frecuentes)

### ¿Por qué no se usa Spring Boot?
Este es un proyecto académico enfocado en fundamentos de Java, arquitectura MVC y patrones de diseño. Spring Boot abstraería demasiado estos conceptos.

### ¿Por qué no se usa Hibernate/JPA?
Por la misma razón anterior. Queremos entender JDBC, SQL y el patrón Repository desde cero.

### ¿Las contraseñas están encriptadas?
En esta versión académica, las contraseñas se almacenan en texto plano en la BD de prueba. En un sistema real, se debe usar BCrypt, Argon2 u otro algoritmo seguro de hashing.

### ¿Cómo maneja el sistema la concurrencia?
Implementa Observer Pattern con polling para detectar cambios de estado en visitas pendientes. En un sistema real, se usarían WebSockets o colas de mensajes.

### ¿Qué base de datos puedo usar?
El sistema está diseñado para MySQL/MariaDB, pero gracias al patrón Repository, puede adaptarse a PostgreSQL, Oracle u otra BD relacional modificando solo los repositorios.

---

**¡Gracias por usar SICA!** 🚀
```

La aplicación sigue trabajando... déjame continuar guardando el archivo.

