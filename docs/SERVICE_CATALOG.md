# Nexora service catalog (code map)

This document maps the **logical microservice** from the Nexora vision to the **Java package** in this Spring Boot application. The layout supports splitting each area into its own deployable later without renaming concepts.

| Logical service       | Package / module prefix | REST prefix (typical) |
|----------------------|-------------------------|------------------------|
| Auth                 | `org.example.nexora.auth` | `/api/v1/auth` |
| User & profile       | `org.example.nexora.user` | `/api/...` (see controllers) |
| Social               | `org.example.nexora.social` | `/api/...` |
| Messaging            | `org.example.nexora.messaging` | `/api/...` |
| Marketplace          | `org.example.nexora.marketplace` | `/api/...` |
| Food                 | `org.example.nexora.food` | `/api/...` |
| Ride                 | `org.example.nexora.ride` | `/api/...` |
| Grocery              | `org.example.nexora.grocery` | `/api/...` |
| Video                | `org.example.nexora.video` | `/api/...` |
| **Games**            | `org.example.nexora.game` | `/api/games` |
| **Media / editing**  | `org.example.nexora.media` | `/api/media` |
| Payments             | `org.example.nexora.payment` | `/api/payments` |
| Wallet               | `org.example.nexora.wallet` | `/wallet`, withdraw APIs |
| Transactions (P2P ledger) | `org.example.nexora.transaction` | `/transactions` |
| Advertising          | `org.example.nexora.advertising` | `/api/...` |
| AI                   | `org.example.nexora.ai` | `/api/...` |
| Admin                | `org.example.nexora.admin` | `/api/...` |
| Security (JWT, fraud)| `org.example.nexora.security` | filters / internal |
| Cross-cutting        | `org.example.nexora.common`, `config` | exceptions, OpenAPI, Kafka, Redis |

## Kafka topics (naming)

Event streams use the `nexora.<domain>.events` pattern where applicable, for example:

- `nexora.user.events`
- `nexora.post.events`
- `nexora.game.events`
- `nexora.media.events`

Constants live in `KafkaConfig` for auto-created topics in development.

## Related docs

- [NEXORA_ECOSYSTEM.md](./NEXORA_ECOSYSTEM.md) — product and architecture narrative.
- [ARCHITECTURE_GATEWAY.md](./ARCHITECTURE_GATEWAY.md) — API gateway and split strategy.
