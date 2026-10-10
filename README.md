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

## Seguridad (API Key Authentication)

La API cuenta con una capa de seguridad basada en el header HTTP `X-API-KEY`:

- **Header requerido:** `X-API-KEY: wmrt-secret-api-key-2026`
- **Rutas protegidas:** Todas las rutas bajo `/api/**`
- **Respuesta ante error de autenticación:** `401 Unauthorized` con formato JSON estándar:
  ```json
  {
    "timestamp": "2026-10-09T23:30:00Z",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid or missing API Key. Please provide a valid header: X-API-KEY",
    "path": "/api/v1/zones"
  }
  ```
- **Deshabilitar seguridad para pruebas locales:** Puedes deshabilitarla configurando `SECURITY_API_KEY_ENABLED=false` en el `.env` o en `application.properties`.

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

> Todos los endpoints requieren el header `X-API-KEY: wmrt-secret-api-key-2026`.

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

### 1. Intento sin API Key (retorna 401 Unauthorized)

```bash
curl -i http://localhost:8080/api/v1/zones
```

### 2. Listar todas las zonas (con API Key)

```bash
curl -H "X-API-KEY: wmrt-secret-api-key-2026" http://localhost:8080/api/v1/zones
```

### 3. Ver fechas disponibles para una zona

```bash
# Zona 2 (Centro), próximos 30 días (default)
curl -H "X-API-KEY: wmrt-secret-api-key-2026" \
  "http://localhost:8080/api/v1/availability/zones/2/dates"

# Con rango explícito
curl -H "X-API-KEY: wmrt-secret-api-key-2026" \
  "http://localhost:8080/api/v1/availability/zones/2/dates?from=2026-10-10&to=2026-10-24"
```

### 4. Ver bloques de tiempo disponibles para una zona y fecha

```bash
curl -H "X-API-KEY: wmrt-secret-api-key-2026" \
  "http://localhost:8080/api/v1/availability/zones/2/blocks?date=2026-10-13"
```

### 5. Crear una reserva

```bash
curl -X POST http://localhost:8080/api/v1/reservations \
  -H "X-API-KEY: wmrt-secret-api-key-2026" \
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

### 6. Obtener una reserva por ID

```bash
curl -H "X-API-KEY: wmrt-secret-api-key-2026" \
  http://localhost:8080/api/v1/reservations/1
```

### 7. Cancelar una reserva

```bash
curl -X DELETE -H "X-API-KEY: wmrt-secret-api-key-2026" \
  http://localhost:8080/api/v1/reservations/1
```

---

## Flujo típico

1. **Listar zonas** → `GET /api/v1/zones` para conocer las zonas disponibles y sus IDs.
2. **Consultar fechas disponibles** → `GET /api/v1/availability/zones/{zoneId}/dates` para ver qué días tienen capacidad.
3. **Consultar bloques del día** → `GET /api/v1/availability/zones/{zoneId}/blocks?date=YYYY-MM-DD` para ver las franjas horarias con slots disponibles.
4. **Crear la reserva** → `POST /api/v1/reservations` con el ID del bloque elegido y los datos del cliente.
5. **Verificar la reserva** → `GET /api/v1/reservations/{id}` o `GET /api/v1/reservations/order/{orderId}`.
6. **Cancelar si es necesario** → `DELETE /api/v1/reservations/{id}` libera el slot automáticamente.

---

## Importar en Postman

1. Abre Postman
2. Clic en **Import**
3. Selecciona el archivo `dispatch-api.postman_collection.json` de la raíz del proyecto
4. La colección ya incluye la autenticación con `X-API-KEY` configurada a nivel global mediante la variable `{{apiKey}}`.
