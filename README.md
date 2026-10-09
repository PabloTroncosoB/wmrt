# Dispatch Window Reservation API

API REST para la reserva de ventanas de despacho (dispatch windows) para órdenes de retail. Permite a los clientes reservar franjas horarias de entrega en zonas geográficas de la Región Metropolitana.

---

## Tecnologías

| Componente      | Versión    |
|-----------------|------------|
| Spring Boot     | 3.2.5      |
| Java            | 21         |
| SQLite          | 3.45.3.0   |
| Maven           | 3.8+       |
| Jackson         | (managed)  |

---

## Requisitos

- **Java 21** instalado y en el `PATH`
- **Maven 3.8+** instalado y en el `PATH`

---

## Configuración de entorno

El proyecto incluye un archivo `.env.example` con todas las variables disponibles. Copia y ajusta los valores:

```bash
cp .env.example .env
```

> **⚠️ Importante:** Spring Boot **no carga `.env` automáticamente**. Las variables deben estar presentes en el entorno del proceso antes de arrancar. El archivo `.env` es solo una referencia; los valores reales se pasan como variables de entorno del sistema o al ejecutar el proceso.
>
> `application.properties` usa la sintaxis `${VARIABLE:default}`, por lo que si no se exporta ninguna variable el servicio **arranca igual** usando los valores por defecto definidos ahí.

### Cómo exportar las variables antes de arrancar

**Linux / macOS:**
```bash
export $(grep -v '^#' .env | xargs)
java -jar target/dispatch-1.0.0.jar
```

**PowerShell (Windows):**
```powershell
Get-Content .env | Where-Object { $_ -match '^\w' } | ForEach-Object {
    $k, $v = $_ -split '=', 2
    [System.Environment]::SetEnvironmentVariable($k, $v)
}
java -jar target/dispatch-1.0.0.jar
```

---

## Levantar el servicio

```bash
# Acceder al directorio del proyecto
cd C:\Users\pab\code\wmrt

# Compilar el proyecto
mvn clean package -DskipTests

# Ejecutar el JAR
java -jar target/dispatch-1.0.0.jar

# O directamente con Maven (modo desarrollo)
mvn spring-boot:run
```

> **Nota:** La base de datos SQLite se crea automáticamente en `dispatch.db` en el directorio raíz del proyecto al iniciar la aplicación. No se requiere ninguna configuración adicional de base de datos. Los bloques de tiempo para los próximos 14 días hábiles también se generan automáticamente al arrancar.

---

## Endpoints disponibles

| Método   | Path                                              | Descripción                                          |
|----------|---------------------------------------------------|------------------------------------------------------|
| `GET`    | `/api/v1/zones`                                   | Lista todas las zonas de despacho                    |
| `GET`    | `/api/v1/zones/{id}`                              | Obtiene una zona por su ID                           |
| `GET`    | `/api/v1/availability/zones/{zoneId}/dates`       | Fechas disponibles para una zona (parámetros: `from`, `to`) |
| `GET`    | `/api/v1/availability/zones/{zoneId}/blocks`      | Bloques de tiempo para una zona y fecha (`date`)     |
| `GET`    | `/api/v1/availability/blocks/{blockId}`           | Detalle de un bloque de tiempo por ID                |
| `POST`   | `/api/v1/reservations`                            | Crea una nueva reserva (body JSON)                   |
| `GET`    | `/api/v1/reservations`                            | Lista todas las reservas                             |
| `GET`    | `/api/v1/reservations/{id}`                       | Obtiene una reserva por ID                           |
| `GET`    | `/api/v1/reservations/order/{orderId}`            | Obtiene una reserva por `orderId`                    |
| `DELETE` | `/api/v1/reservations/{id}`                       | Cancela una reserva (retorna 200 con reserva actualizada) |

---

## Ejemplos de uso (curl)

### Listar todas las zonas

```bash
curl http://localhost:8080/api/v1/zones
```

### Ver fechas disponibles para una zona

```bash
# Zona 2 (Centro), próximos 30 días (default)
curl "http://localhost:8080/api/v1/availability/zones/2/dates"

# Con rango explícito
curl "http://localhost:8080/api/v1/availability/zones/2/dates?from=2026-10-10&to=2026-10-24"
```

### Ver bloques de tiempo disponibles para una zona y fecha

```bash
curl "http://localhost:8080/api/v1/availability/zones/2/blocks?date=2026-10-13"
```

**Respuesta de ejemplo:**
```json
[
  {
    "id": 5,
    "date": "2026-10-13",
    "startTime": "09:00",
    "endTime": "12:00",
    "cost": 5990.0,
    "availableSlots": 10,
    "zoneId": 2,
    "zoneName": "Centro",
    "full": false
  }
]
```

### Crear una reserva

```bash
curl -X POST http://localhost:8080/api/v1/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-001",
    "timeBlockId": 5,
    "customerName": "Juan Pérez",
    "address": "Av. Providencia 1234, Santiago"
  }'
```

**Respuesta:** `201 Created`
```json
{
  "id": 1,
  "orderId": "ORD-001",
  "timeBlockId": 5,
  "customerName": "Juan Pérez",
  "address": "Av. Providencia 1234, Santiago",
  "status": "CONFIRMED",
  "createdAt": "2026-10-09T17:30:00.123",
  "timeBlock": { ... }
}
```

### Obtener una reserva por ID

```bash
curl http://localhost:8080/api/v1/reservations/1
```

### Obtener una reserva por orderId

```bash
curl http://localhost:8080/api/v1/reservations/order/ORD-001
```

### Cancelar una reserva

```bash
curl -X DELETE http://localhost:8080/api/v1/reservations/1
```

**Respuesta:** `200 OK` con el cuerpo de la reserva actualizado (`status: "CANCELLED"`).

---

## Flujo típico

1. **Listar zonas** → `GET /api/v1/zones` para conocer las zonas disponibles y sus IDs.
2. **Consultar fechas disponibles** → `GET /api/v1/availability/zones/{zoneId}/dates` para ver qué días tienen capacidad.
3. **Consultar bloques del día** → `GET /api/v1/availability/zones/{zoneId}/blocks?date=YYYY-MM-DD` para ver las franjas horarias con slots disponibles.
4. **Crear la reserva** → `POST /api/v1/reservations` con el ID del bloque elegido y los datos del cliente.
5. **Verificar la reserva** → `GET /api/v1/reservations/{id}` o `GET /api/v1/reservations/order/{orderId}`.
6. **Cancelar si es necesario** → `DELETE /api/v1/reservations/{id}` libera el slot automáticamente.

---

## Estructura del proyecto

```
wmrt/
├── pom.xml
├── .env                            ← Variables de entorno locales (ignorado por git)
├── .env.example                    ← Plantilla de variables (incluido en git)
├── dispatch.db                     ← Creado automáticamente al arrancar
├── README.md
├── personal.md
└── src/
    └── main/
        ├── java/com/wmrt/dispatch/
        │   ├── DispatchApplication.java
        │   ├── config/
        │   │   ├── DatabaseConfig.java  ← WAL mode + foreign keys
        │   │   └── DataSeeder.java      ← Seed de bloques de tiempo
        │   ├── model/
        │   │   ├── Zone.java
        │   │   ├── TimeBlock.java
        │   │   ├── Reservation.java
        │   │   └── ReservationStatus.java
        │   ├── dto/
        │   │   ├── AvailableDateResponse.java
        │   │   ├── TimeBlockResponse.java
        │   │   ├── ReservationRequest.java
        │   │   ├── ReservationResponse.java
        │   │   └── ErrorResponse.java
        │   ├── repository/
        │   │   ├── ZoneRepository.java
        │   │   ├── TimeBlockRepository.java
        │   │   └── ReservationRepository.java
        │   ├── service/
        │   │   ├── ZoneService.java
        │   │   ├── AvailabilityService.java
        │   │   └── ReservationService.java
        │   ├── controller/
        │   │   ├── ZoneController.java
        │   │   ├── AvailabilityController.java
        │   │   └── ReservationController.java
        │   └── exception/
        │       ├── GlobalExceptionHandler.java
        │       ├── ZoneNotFoundException.java
        │       ├── TimeBlockNotFoundException.java
        │       ├── NoCapacityException.java
        │       └── ReservationNotFoundException.java
        └── resources/
            ├── application.properties
            └── schema.sql
```
