# Nexora service catalog (28-system mapping)

This document maps Nexora's **28 core systems** to:

- current backend package(s) in this repository
- implementation status
- next-step gap notes for production readiness

> Legend: **Implemented** = already present in codebase, **Partial** = foundation exists but not full product scope, **Planned** = not yet implemented in this repo.

| # | System | Current package/module mapping | Status | Gap / next steps |
|---|--------|--------------------------------|--------|------------------|
| 1 | User Accounts & Authentication | `org.example.nexora.auth`, `org.example.nexora.security`, `org.example.nexora.user` | Partial | Add OTP flows, multi-device session controls, social login adapters |
| 2 | User Profiles | `org.example.nexora.user` (`User`, `UserProfile`, `UserController`) | Partial | Portfolio blocks, pinned posts, verified-badge workflow |
| 3 | Follow System | `org.example.nexora.user` (`UserFollow`, `UserFollowRepository`) | Partial | Suggested users + growth analytics service |
| 4 | Content Feed | `org.example.nexora.social` (`PostService`, `PostController`) | Partial | Ranking pipeline, trending index, infinite-scroll cursor API hardening |
| 5 | Post Creation System | `org.example.nexora.social`, `org.example.nexora.media` | Partial | Drafts, scheduled publishing, tagging/mentions |
| 6 | Built-in Editing System | `org.example.nexora.media` (`EditingJob`, `MediaService`) | Partial | Timeline editor, layers, reusable presets/templates |
| 7 | AI Editing & Assistance | `org.example.nexora.ai`, `org.example.nexora.media` | Partial | AI suggestions, thumbnail generation, style-transfer presets |
| 8 | Engagement System | `org.example.nexora.social` (`Like`, `Comment`) | Partial | Share model + analytics + reaction-type taxonomy |
| 9 | Messaging System | `org.example.nexora.messaging`, `config/WebSocketConfig` | Partial | Group chats, richer read receipts, voice-note support |
| 10 | Video Calling System | `config/WebSocketConfig` (signaling foundation only) | Planned | Add WebRTC signaling APIs, TURN/STUN integration, call state persistence |
| 11 | Phone Number Integration | `auth` (extensible), `user` | Planned | Phone verification, OTP login, contact sync, SMS provider integration |
| 12 | Voice Features | `messaging` (extensible) | Planned | Voice notes, speech-to-text, voice commands |
| 13 | Language Translation System | `ai` (extensible) | Planned | Chat/caption translation service and real-time translation pipe |
| 14 | Currency Conversion System | `payment`, `wallet`, `transaction` | Planned | FX-rate provider client, rate snapshots, display conversion API |
| 15 | Marketplace System | `org.example.nexora.marketplace` | Partial | Review/rating model, featured listings, stronger order lifecycle |
| 16 | Digital Product System | `marketplace`, `media` (extensible) | Planned | Secure download links, licensing, versioned digital assets |
| 17 | Service Selling System | `marketplace` (extensible) | Planned | Service catalog entities, delivery milestones, custom offers |
| 18 | Earnings & Wallet System | `org.example.nexora.wallet`, `org.example.nexora.transaction`, `org.example.nexora.video.Earning` | Partial | Multi-currency balances, payout orchestration, statement exports |
| 19 | Payment Integration | `org.example.nexora.payment` | Partial | Refund APIs, subscription billing, webhook idempotency hardening |
| 20 | Creator Dashboard | `video` + analytics-ready event streams | Partial | Dedicated analytics endpoints/UI aggregates |
| 21 | Notification System | `org.example.nexora.notification` | Partial | Push channels, preference matrix, digest strategies |
| 22 | Search & Discovery | `marketplace` (search-like patterns), `social` (discoverable content) | Partial | Unified search index, filters, recommendations/trending APIs |
| 23 | Real-Time Engine | `config/WebSocketConfig`, `messaging`, `notification` | Partial | Presence service, event-stream fanout, backpressure controls |
| 24 | Recommendation System | `ai`, `social` (extensible) | Planned | Candidate generation + ranking + feedback loop |
| 25 | Media Storage System | `media`, `video` | Partial | Object-storage adapters (S3/GCS), CDN, async compression pipelines |
| 26 | Dockerized Infrastructure | `Dockerfile`, `compose.yaml`, `k8s/` | Implemented | Add production-grade health probes, autoscaling policy, secret management |
| 27 | Cross-Platform Support | `frontend/`, `mobile/` | Partial | Shared API contract/versioning and auth/session parity |
| 28 | Global Communication Layer | `messaging`, planned `video calling`, planned `translation` | Planned | Unified conversation abstraction across chat/calls/translation |

## Existing package index (from current backend)

- `org.example.nexora.auth`
- `org.example.nexora.user`
- `org.example.nexora.social`
- `org.example.nexora.messaging`
- `org.example.nexora.marketplace`
- `org.example.nexora.media`
- `org.example.nexora.video`
- `org.example.nexora.payment`
- `org.example.nexora.wallet`
- `org.example.nexora.transaction`
- `org.example.nexora.notification`
- `org.example.nexora.ai`
- `org.example.nexora.security`
- `org.example.nexora.config`

## Deployment-oriented service boundaries (recommended)

1. Identity Service (`auth`, `user`, phone verification adapters)
2. Social Graph Service (`follow`, `profiles`, relationship recommendations)
3. Content Service (`posts`, feed ranking, engagement)
4. Realtime Comms Service (`messaging`, notifications, presence)
5. Media/AI Service (`media`, AI editing, transformation)
6. Commerce Service (`marketplace`, digital products, services`)
7. Finance Service (`payment`, `wallet`, `transaction`, payouts)
8. Discovery Service (search + recommendation)

## Related docs

- [`NEXORA_PRODUCT_SPEC.md`](./NEXORA_PRODUCT_SPEC.md) — complete product feature spec for all 28 systems.
- [`DEPLOYMENT.md`](./DEPLOYMENT.md) — deployment guide.
- [`ARCHITECTURE_GATEWAY.md`](./ARCHITECTURE_GATEWAY.md) — gateway and split strategy.
