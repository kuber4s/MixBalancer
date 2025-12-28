# 🎮 MixBalancer

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-green?style=for-the-badge&logo=springboot)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

**Team balancing REST API for Overwatch 2 custom games**

***P.S.: I extend my sincere gratitude to my valued friends Ilya Makarov (aka Txao) and Dmitry Loginov for allowing me to test the balancer on their Discord servers***

[Features](#-features) •
[Quick Start](#-quick-start) •
[API](#-api-endpoints) •
[Algorithm](#-balance-algorithm) •
[Integration](#-integration-examples)

</div>

---

## ✨ Features

- 🎯 **Smart Balancing** — Advanced algorithm prioritizing tank SR similarity
- 🗺️ **Map Selection** — Random map from all 28 Overwatch 2 maps
- 🔺 **Priority Queue** — Players who skipped games get priority next match
- 📊 **Balance Metrics** — Detailed quality assessment (Excellent/Good/Fair/Poor)
- 🔌 **REST API** — Easy integration with Discord, Web UI
- 📚 **OpenAPI/Swagger** — Interactive API documentation
- 💾 **Flexible Storage** — In-memory (default) or PostgreSQL

---

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Maven 3.8+

### Run

```bash
# Clone
git clone https://github.com/kuber4s/MixBalancer.git
cd MixBalancer

# Build & Run
mvn spring-boot:run
```

### Access

| URL | Description |
|-----|-------------|
| http://localhost:8080/api/v1 | REST API |
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8080/api/docs | OpenAPI JSON |

---

## 📖 API Endpoints

### Players

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/players` | Register or update player |
| `GET` | `/api/v1/players/{id}` | Get player by ID |
| `GET` | `/api/v1/players` | Get all players |
| `DELETE` | `/api/v1/players/{id}` | Delete player |

### Lobbies

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/lobbies/{id}` | Get lobby status |
| `POST` | `/api/v1/lobbies/{id}/join` | Join lobby |
| `POST` | `/api/v1/lobbies/{id}/leave` | Leave lobby |
| `GET` | `/api/v1/lobbies/{id}/queue` | Get queue status |

### Balancer

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/balance/lobby/{id}` | Balance teams from lobby |
| `POST` | `/api/v1/balance/direct` | Balance from player list |
| `GET` | `/api/v1/balance/maps` | Get all maps |
| `GET` | `/api/v1/balance/maps/{mode}` | Get maps by mode |
| `GET` | `/api/v1/balance/maps/random` | Get random map |

---

## 💡 Usage Examples

### Register a Player

```bash
curl -X POST http://localhost:8080/api/v1/players \
  -H "Content-Type: application/json" \
  -d '{
    "id": "discord-123456",
    "name": "NGINX",
    "tankSR": 4200,
    "dpsSR": 0,
    "supportSR": 3800
  }'
```

### Balance Teams (Direct API)

```bash
curl -X POST http://localhost:8080/api/v1/balance/direct \
  -H "Content-Type: application/json" \
  -d '{
    "players": [
      {"id": "1", "name": "NGINX", "tankSR": 0, "dpsSR": 3700, "supportSR": 0},
      {"id": "2", "name": "yreen", "tankSR": 0, "dpsSR": 0, "supportSR": 4000},
      {"id": "3", "name": "Txao", "tankSR": 4700, "dpsSR": 0, "supportSR": 4500},
      {"id": "4", "name": "KSAA", "tankSR": 4700, "dpsSR": 0, "supportSR": 0},
      {"id": "5", "name": "GetMads", "tankSR": 3500, "dpsSR": 0, "supportSR": 3700},
      {"id": "6", "name": "Pr1de", "tankSR": 0, "dpsSR": 0, "supportSR": 3800},
      {"id": "7", "name": "ScorpixSHOW", "tankSR": 0, "dpsSR": 3500, "supportSR": 0},
      {"id": "8", "name": "Xapu3ma", "tankSR": 0, "dpsSR": 3700, "supportSR": 3500},
      {"id": "9", "name": "Nanika", "tankSR": 0, "dpsSR": 0, "supportSR": 4000},
      {"id": "10", "name": "Sacr1ficed", "tankSR": 0, "dpsSR": 3850, "supportSR": 0}
    ]
  }'
```

### Response Example

```json
{
  "match": {
    "balanceQuality": "EXCELLENT",
    "createdAt": "2025-12-28T20:58:14.742587300Z",
    "dpsSRDiff": 25,
    "id": "88BE43CD",
    "lobbyId": null,
    "mapMode": "Flashpoint",
    "mapName": "Aatlis",
    "queue": [],
    "srDifference": 30,
    "supportSRDiff": 50,
    "tankSRDiff": 0,
    "team1": {
      "emoji": "🔴",
      "name": "🔴 Red Team",
      "averageSR": 3950,
      "dps": [
        {
          "playerId": "10",
          "playerName": "Sacr1ficed",
          "role": "dps",
          "sr": 3850
        },
        {
          "playerId": "7",
          "playerName": "ScorpixSHOW",
          "role": "dps",
          "sr": 3500
        }
      ],
      "support": [
        {
          "playerId": "2",
          "playerName": "Nanika",
          "role": "support",
          "sr": 4000
        },
        {
          "playerId": "5",
          "playerName": "GetMads",
          "role": "support",
          "sr": 3700
        }
      ],
      "tank": {
        "playerId": "3",
        "playerName": "Txao",
        "role": "tank",
        "sr": 4700
      }
    },
    "team2": {
      "emoji": "🔵",
      "name": "🔵 Blue Team",
      "averageSR": 3980,
      "dps": [
        {
          "playerId": "1",
          "playerName": "NGINX",
          "role": "dps",
          "sr": 3700
        },
        {
          "playerId": "8",
          "playerName": "Xapu3ma",
          "role": "dps",
          "sr": 3700
        }
      ],
      "support": [
        {
          "playerId": "6",
          "playerName": "Pr1de",
          "role": "support",
          "sr": 3800
        },
        {
          "playerId": "9",
          "playerName": "yreen",
          "role": "support",
          "sr": 4000
        }
      ],
      "tank": {
        "playerId": "4",
        "playerName": "KSAA",
        "role": "tank",
        "sr": 4700
      }
    }
  },
  "success": true
}
```

---

## 🔄 Flex Players

Players can have SR for multiple roles. The algorithm intelligently uses this flexibility:

### Example: Flex Player

```json
{
  "id": "1",
  "name": "NGINX",
  "tankSR": 4200,
  "dpsSR": 3700,
  "supportSR": 3500
}
```

This player:
- **Primary role**: Tank (highest SR = 4200)
- **Can also play**: DPS (3700) and Support (3500)

### How It Works

1. **Validation** — Checks if enough players exist for each role (counts all who `canPlay`)
2. **Selection** — Prefers players by primary role, then fills gaps with flex
3. **Assignment** — May assign flex player to ANY of their roles for better balance

### Practical Benefits

- **More matches possible** — If you have 3 tanks but only 3 supports, a tank who can support enables the match
- **Better balance** — Algorithm can choose which role gives the best SR balance
- **Queue flexibility** — Flex players are more likely to get into matches

---

## 🧠 Balance Algorithm

The **FairBalanceStrategy [path: com/overwatch/balancer/core/impl/FairBalanceStrategy.java]** uses a multi-phase approach:

### Phase 1: Tank Pairing (Critical)
IMHO tanks are the most impactful role with only 1 per team. The algorithm finds the two tanks with the closest SR to minimize tank difference.

### Phase 2: DPS Distribution
DPS players are assigned to balance overall team SR, with higher SR DPS going to the team with lower tank SR.

### Phase 3: Support Compensation
Supports are distributed to compensate for any remaining SR imbalance between teams.

### Phase 4: Optimization
The algorithm runs 3000 (default) iterations to find the optimal balance.

### Quality Metrics

| Quality | Overall SR Diff | Tank SR Diff |
|---------|-----------------|--------------|
| 🟢 **EXCELLENT** | ≤30 | ≤200 |
| 🟡 **GOOD** | ≤75 | ≤350 |
| 🟠 **FAIR** | ≤150 | ≤500 |
| 🔴 **POOR** | >150 | >500 |

---

## 🗺️ Available Maps

### Control (7)
Antarctic Peninsula, Busan, Ilios, Lijiang Tower, Nepal, Oasis, Samoa

### Escort (8)
Circuit Royal, Dorado, Havana, Junkertown, Rialto, Route 66, Shambali Monastery, Watchpoint: Gibraltar

### Flashpoint (3)
New Junk City, Suravasa, Aatlis

### Hybrid (7)
Blizzard World, Eichenwalde, Hollywood, King's Row, Midtown, Numbani, Paraíso

### Push (4)
Colosseo, Esperança, New Queen Street, Runasapi

---

## 📊 Rank System

| Rank | SR Range | Short |
|------|----------|-------|
| Bronze | 1000-1499 | B |
| Silver | 1500-1999 | S |
| Gold | 2000-2499 | G |
| Platinum | 2500-2999 | P |
| Diamond | 3000-3499 | D |
| Master | 3500-3999 | M |
| Grandmaster | 4000-4499 | GM |
| Champion | 4500-5000 | C |

> **Note:** SR = 0 means the player doesn't play this role.

---

## 🔌 Integration Examples

### Discord Bot (JavaScript)

```javascript
const BALANCER_URL = 'http://localhost:8080/api/v1';

async function registerPlayer(discordId, name, tankSR, dpsSR, supportSR) {
    const res = await fetch(`${BALANCER_URL}/players`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ id: discordId, name, tankSR, dpsSR, supportSR })
    });
    return res.json();
}

async function balanceMatch(players) {
    const res = await fetch(`${BALANCER_URL}/balance/direct`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ players })
    });
    return res.json();
}
```

### Discord Bot (Python)

```python
import aiohttp

BALANCER_URL = 'http://localhost:8080/api/v1'

async def register_player(discord_id: str, name: str, tank_sr: int, dps_sr: int, support_sr: int):
    async with aiohttp.ClientSession() as session:
        async with session.post(f'{BALANCER_URL}/players', json={
            'id': discord_id,
            'name': name,
            'tankSR': tank_sr,
            'dpsSR': dps_sr,
            'supportSR': support_sr
        }) as resp:
            return await resp.json()

async def balance_match(players: list):
    async with aiohttp.ClientSession() as session:
        async with session.post(f'{BALANCER_URL}/balance/direct', json={
            'players': players
        }) as resp:
            return await resp.json()
```

---

## ⚙️ Configuration

### Application Properties

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8080 | Server port |
| `spring.profiles.active` | memory | Profile: `memory` or `postgres` |
| `balancer.max-iterations` | 3000 | Balance algorithm iterations |

### PostgreSQL Profile

```bash
# Run with PostgreSQL
mvn spring-boot:run -Dspring.profiles.active=postgres

# Environment variables
export DATABASE_URL=jdbc:postgresql://localhost:5432/ow2balancer
export DATABASE_USER=postgres
export DATABASE_PASSWORD=yourpassword
```

---

## 📁 Project Structure

```
src/main/java/com/overwatch/balancer/
├── api/
│   ├── controller/                   # REST controllers
│   └── dto/                          # Request/Response DTOs
├── config/                           # Spring configuration
│   └── db/
├── core/
│   └── impl/                         # Balancer algorithm core
├── domain/                           # Domains and models
│   ├── enumeration/
│   └── model/
├── exception/                        # Business exceptions
├── repository/
│   └── impl/                         # Repository implementations
├── service/                          # Business logic
├── BalancerApplication.java
└── GlobalExceptionHandler.java
```

---

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=FairBalanceStrategyTest
```

---

## 🤝 Contributing

**New ideas and bug fixes are more than welcome!**

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/ultra-mega-feature`)
3. Commit your changes (`git commit -m 'Add super-duper feature'`)
4. Push to the branch (`git push origin feature/ultra-mega-feature`)
5. Open a Pull Request

## Feel free to open an issue if you have questions, suggestions, or found a bug ;) !

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<div align="center">

Made with ❤️ for the Overwatch 2 community

**⭐ Star this repo if you find it useful!**

</div>