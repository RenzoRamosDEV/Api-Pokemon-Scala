# PokeAPI Scala Wrapper

API REST construida en **Scala 3** que actúa como wrapper sobre [PokeAPI](https://pokeapi.co). Expone los recursos de Pokémon con un formato de respuesta uniforme, caché en memoria y documentación Swagger integrada.

---

## ¿Qué hace?

- Consume la API pública de PokeAPI y reexpone sus recursos con un envelope de respuesta estándar
- Cachea los Pokémon más consultados en memoria para reducir latencia y llamadas externas
- Ofrece un endpoint enriquecido `/pokemon/{id}/full` que combina Pokémon + Especie + Cadena evolutiva en una sola llamada
- Valida parámetros de paginación y traduce errores de PokeAPI a códigos HTTP semánticos
- Documenta toda la API automáticamente en Swagger UI

---

## Stack tecnológico

| Capa | Librería | Versión |
|---|---|---|
| Servidor HTTP | http4s Ember | 0.23.27 |
| Cliente HTTP | http4s Ember Client | 0.23.27 |
| Efectos | Cats Effect | 3.5.4 |
| JSON | Circe | 0.14.10 |
| Configuración | PureConfig | 0.17.7 |
| Caché | Scaffeine (Caffeine) | 5.3.0 |
| Logging | log4cats + Logback | 2.6.0 / 1.5.13 |
| Tests | MUnit + munit-cats-effect | 1.0.3 / 2.0.0 |
| Lenguaje | Scala | 3.4.3 |
| JVM | Java | 17 |

---

## Arquitectura

El proyecto sigue **Clean Architecture** en cuatro capas:

```
src/main/scala/com/pokeapp/
│
├── domain/
│   ├── model/          → Modelos de dominio (Pokemon, Ability, Berry, Move, etc.)
│   ├── port/           → Interfaces de repositorio (PokemonRepository, ResourceRepository)
│   └── error/          → Errores de dominio (NotFound, RateLimitExceeded, ParseError, ExternalApiError)
│
├── application/
│   └── {recurso}/      → Casos de uso: GetPokemonUseCase, ListBerryUseCase, GetPokemonFullUseCase, etc.
│
├── infrastructure/
│   ├── http/client/    → PokeApiClient (tipado) y GenericPokeApiClient (passthrough JSON)
│   ├── http/codec/     → Decoders Circe para mapear snake_case de PokeAPI a camelCase del dominio
│   ├── adapter/        → Implementaciones HTTP de los puertos de repositorio
│   └── cache/          → CaffeineCache y CachedPokemonRepository (patrón decorador)
│
├── api/
│   ├── routes/         → Rutas HTTP (PokemonRoutes, TypedResourceRoutes, GenericResourceRoutes, etc.)
│   ├── middleware/     → RequestLogger y ErrorHandler
│   ├── dto/            → Envelopes de respuesta (ApiResponse, ApiError) y encoders de dominio
│   └── ResourceRegistry.scala → Lista blanca de los 56 recursos válidos de PokeAPI
│
├── config/
│   └── AppConfig.scala → Configuración tipada cargada desde application.conf vía PureConfig
│
└── Main.scala          → Punto de entrada; compone toda la aplicación como un Resource de Cats Effect
```

### Decisiones de diseño destacadas

- **Solo Pokémon tiene caché** — es el recurso más consultado; el resto va directo a PokeAPI
- **Claves de caché con prefijo** — `id:25` y `name:pikachu` conviven sin colisionar en el mismo caché
- **No se cachean errores** — si PokeAPI falla, el siguiente intento vuelve a consultar la fuente
- **ResourceRegistry como lista blanca** — evita que GenericResourceRoutes actúe como proxy abierto
- **Orden de rutas importa** — rutas específicas se montan antes que las genéricas en el stack de http4s
- **Stack de middleware de adentro hacia afuera** — `RequestLogger → ErrorHandler → CORS`

---

## Endpoints

### Pokémon

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/v1/pokemon` | Lista paginada de Pokémon |
| `GET` | `/api/v1/pokemon/{id\|name}` | Detalle de un Pokémon |
| `GET` | `/api/v1/pokemon/{id\|name}/full` | Pokémon + Especie + Cadena evolutiva |

### Recursos tipados

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/v1/ability/{id\|name}` | Detalle de una habilidad |
| `GET` | `/api/v1/berry/{id\|name}` | Detalle de una baya |
| `GET` | `/api/v1/move/{id\|name}` | Detalle de un movimiento |
| `GET` | `/api/v1/type/{id\|name}` | Detalle de un tipo |
| `GET` | `/api/v1/item/{id\|name}` | Detalle de un item |
| `GET` | `/api/v1/nature/{id\|name}` | Detalle de una naturaleza |
| `GET` | `/api/v1/evolution-chain/{id}` | Cadena evolutiva completa |
| `GET` | `/api/v1/location/{id\|name}` | Detalle de una localización |

> Todos los recursos tipados también exponen `GET /api/v1/{recurso}?limit=20&offset=0` para listado paginado.

### Passthrough genérico

Cualquier recurso incluido en `ResourceRegistry` (berry-firmness, egg-group, generation, region, etc.) puede consultarse con:

```
GET /api/v1/{recurso}
GET /api/v1/{recurso}/{id|name}
```

### Utilidades

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/health` | Health check — devuelve `{ "status": "UP" }` |
| `GET` | `/swagger/swagger.json` | Spec OpenAPI 3.0 |
| `GET` | `/swagger-ui` | Interfaz Swagger UI |

---

## Formato de respuesta

### Respuesta exitosa

```json
{
  "data": { ... },
  "meta": {
    "source": "live",
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

### Respuesta de error

```json
{
  "error": {
    "code": "NOT_FOUND",
    "message": "Resource not found at ...",
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

**Códigos de error posibles:** `NOT_FOUND` · `BAD_REQUEST` · `RATE_LIMIT_EXCEEDED` · `EXTERNAL_API_ERROR` · `INTERNAL_ERROR`

---

## Paginación

Los endpoints de listado aceptan los query params `limit` y `offset`:

```
GET /api/v1/pokemon?limit=20&offset=0
```

- `limit`: entre 1 y 100 (por defecto 20)
- `offset`: mayor o igual a 0 (por defecto 0)

---

## Configuración

El archivo `src/main/resources/application.conf` controla todos los parámetros. Cada valor puede sobreescribirse con variables de entorno:

```hocon
server {
  host = "0.0.0.0"      # variable: SERVER_HOST
  port = 8080            # variable: SERVER_PORT
}

pokeapi {
  base-url        = "https://pokeapi.co/api/v2"
  timeout-seconds = 10
  max-retries     = 3
}

cache {
  max-size    = 1000   # entradas máximas antes de desalojar (LRU)
  ttl-minutes = 60     # tiempo de vida de cada entrada
}
```

---

## Cómo ejecutar

### Requisitos

- Java 17+
- sbt 1.x

### Desarrollo local

```bash
sbt run
```

El servidor arranca en `http://localhost:8080`.

### Tests

```bash
sbt test
```

65 tests · 0 fallos

### Docker

```bash
# Generar el fat JAR
sbt assembly

# Construir y levantar con Docker Compose
docker compose -f docker/docker-compose.yml up --build
```

---

## Variables de entorno

| Variable | Por defecto | Descripción |
|----------|-------------|-------------|
| `SERVER_HOST` | `0.0.0.0` | Host de escucha del servidor |
| `SERVER_PORT` | `8080` | Puerto de escucha del servidor |
