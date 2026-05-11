# Pokédex Retro — Scala Full Stack

Pokédex interactiva con estética retro de consola clásica de los 90. Construida íntegramente en Scala, con un backend funcional y un frontend de servidor renderizado, sin frameworks de JavaScript.

---

## Estructura del proyecto

```
pokeapi-scala/
├── backend/    # API REST en http4s + Cats Effect
└── frontend/   # Interfaz web en Play Framework + Twirl
```

---

## Backend

### Tecnologías

| Librería | Versión | Rol |
|---|---|---|
| **Scala 3** | 3.4.3 | Lenguaje |
| **http4s** | 0.23.27 | Servidor HTTP + cliente HTTP |
| **Cats Effect** | 3.5.4 | Runtime funcional / IO |
| **Circe** | 0.14.10 | Serialización / deserialización JSON |
| **PureConfig** | 0.17.7 | Configuración tipada |
| **Scaffeine** | 5.3.0 | Caché en memoria (Caffeine wrapper) |
| **Log4Cats** | 2.6.0 | Logging funcional |
| **Logback** | 1.5.13 | Backend de logging |
| **Prometheus** | 0.24.0 | Métricas HTTP |
| **MUnit + MUnit CE** | 1.0.3 / 2.0.0 | Testing |

### Arquitectura

El backend sigue una arquitectura en capas inspirada en **Hexagonal Architecture**:

```
com.pokeapp/
├── domain/
│   ├── model/          # Entidades de dominio (Pokemon, Move, Nature, Item, Berry, Type, Ability)
│   └── repository/     # Interfaces de repositorio (traits)
├── application/        # Casos de uso / servicios
├── infrastructure/
│   ├── http/
│   │   ├── routes/     # Definición de rutas HTTP (http4s DSL)
│   │   └── codec/      # Encoders/Decoders Circe por entidad
│   └── config/         # Configuración con PureConfig
├── api/                # Cliente HTTP hacia PokéAPI pública
└── Main.scala          # Punto de entrada con IOApp
```

### Endpoints disponibles

```
GET /api/v1/pokemon           ?limit=&offset=
GET /api/v1/pokemon/:name
GET /api/v1/move              ?limit=&offset=
GET /api/v1/move/:name
GET /api/v1/nature            ?limit=&offset=
GET /api/v1/nature/:name
GET /api/v1/item              ?limit=&offset=
GET /api/v1/item/:name
GET /api/v1/berry             ?limit=&offset=
GET /api/v1/berry/:name
GET /api/v1/type              ?limit=&offset=
GET /api/v1/type/:name
GET /api/v1/ability           ?limit=&offset=
GET /api/v1/ability/:name
```

Todas las respuestas tienen el envelope:
```json
{
  "data": { ... },
  "meta": {
    "source": "pokeapi | cache",
    "timestamp": "2025-05-11T21:00:00Z"
  }
}
```

### Caché

El backend cachea las respuestas de PokéAPI en memoria con **Scaffeine** para reducir latencia y evitar rate limiting. El campo `meta.source` indica si la respuesta viene de caché o de la API externa.

### Levantar el backend

```bash
cd backend
sbt run
# Escucha en http://localhost:8080
```

---

## Frontend

### Tecnologías

| Librería / Tool | Versión | Rol |
|---|---|---|
| **Scala 3** | 3.4.3 | Lenguaje |
| **Play Framework** | 3.0.6 | Framework MVC + servidor web |
| **Twirl** | (incluido en Play) | Motor de plantillas HTML |
| **Play JSON** | 3.0.4 | Deserialización de respuestas del backend |
| **Play WS** | (incluido en Play) | Cliente HTTP para consumir el backend |
| **Guice** | (incluido en Play) | Inyección de dependencias |
| **Press Start 2P** | Google Fonts | Tipografía retro para títulos |
| **VT323** | Google Fonts | Tipografía retro para texto |

> **Sin JavaScript. Sin React. Sin Vue. Sin TypeScript. Sin framework CSS.**  
> Todo el comportamiento interactivo (menú hamburguesa, acordeón, tooltips) se resuelve con **CSS puro** mediante el checkbox hack y la pseudo-clase `:target`.

### Arquitectura

```
frontend/app/
├── controllers/        # Un controller por sección (Play MVC)
│   ├── PokedexController.scala
│   ├── MovesController.scala
│   ├── NaturesController.scala
│   ├── ItemsController.scala
│   ├── BerriesController.scala
│   ├── TypesController.scala
│   └── AbilitiesController.scala
├── models/
│   ├── PokemonModels.scala   # Reads[T] para Pokemon, Sprites, PaginatedResponse
│   └── GameModels.scala      # Reads[T] para Move, Nature, Item, Berry, GameType, Ability
└── views/              # Plantillas Twirl (.scala.html)
    ├── main.scala.html         # Layout base con nav y footer
    ├── index.scala.html        # Pokédex (grid 6×3)
    ├── moves.scala.html        # Movimientos (grid 4×4)
    ├── natures.scala.html      # Naturalezas (acordeón por stat)
    ├── items.scala.html        # Objetos (tabla Poké Mart)
    ├── berries.scala.html      # Bayas (grid 5×2)
    ├── types.scala.html        # Tipos (matriz de efectividad 18×18)
    └── abilities.scala.html    # Habilidades (lista con paginación)
```

### Secciones

| Ruta | Sección | Diseño |
|---|---|---|
| `/` | Pokédex | Grid 6 columnas × 3 filas, cards con sprite y tipos |
| `/movimientos` | Movimientos | Grid 4×4, cards con stats (POW/ACC/PP/PRI) y efecto |
| `/naturalezas` | Naturalezas | Acordeón CSS por stat: sube / baja |
| `/objetos` | Objetos | Tabla estilo Poké Mart con sprite, categoría y precio |
| `/bayas` | Bayas | Grid 5×2 con stats de cultivo y sabores |
| `/tipos` | Tipos | Matriz de efectividad 18×18 con sticky headers |
| `/habilidades` | Habilidades | Lista horizontal con nombre y descripción |

### Levantar el frontend

```bash
cd frontend
sbt run
# Escucha en http://localhost:9000
# Requiere que el backend esté corriendo en http://localhost:8080
```

---

## Flujo de datos

```
Navegador → Play (frontend :9000) → http4s (backend :8080) → PokéAPI / Caché
```

1. El usuario accede a una ruta del frontend
2. El controller de Play hace una petición HTTP al backend con `WSClient`
3. El backend consulta PokéAPI (o sirve desde caché)
4. La respuesta JSON se deserializa con `Play JSON Reads[T]`
5. Twirl renderiza el HTML y lo sirve al navegador

---

## Convenciones del proyecto

- **Commits**: Conventional Commits en español (`feat`, `fix`, `style`, `refactor`, `docs`)
- **Scala**: Scala 3 con sintaxis de indentación (sin llaves)
- **CSS**: Variables CSS custom properties, diseño mobile-first
- **Fuentes**: Press Start 2P para headers, VT323 para texto de cuerpo

---

## Créditos

- Datos: [PokéAPI](https://pokeapi.co/)
- Desarrollado por **Renzo Ramos**
