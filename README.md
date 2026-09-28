# SniffNet ⚡

> Lightweight, non-root Android HTTP/HTTPS traffic inspector powered by `VpnService`. Real-time packet parsing, HTTP protocol dissection, and reactive UI built with pure Java and RxJava3.

![Android](https://img.shields.io/badge/Android-34%2B-3DDC84?style=flat-square&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=java&logoColor=white)
![RxJava](https://img.shields.io/badge/RxJava-3.x-B7178C?style=flat-square&logo=reactiveX&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)

## Overview

**SniffNet** is a high-performance network analysis tool for Android designed to capture, parse, and inspect HTTP/HTTPS traffic in real time. Operating at Layer 3/4 without requiring superuser (root) privileges, SniffNet leverages Android's native `VpnService` architecture to reconstruct TCP streams and analyze application-level payloads on the fly.

Built entirely in Java 17 with an asynchronous, reactive architecture, SniffNet provides developers and security researchers with a clean, low-overhead interface for debugging mobile network activity.

## Key Features

- **Rootless Packet Interception**: Captures system-wide IPv4 TCP traffic through a local tun-interface (`VpnService`).
- **Real-Time Protocol Dissection**: Custom binary parser for extracting HTTP request lines (`GET`, `POST`, `PUT`, `DELETE`, `PATCH`), headers, and URI parameters.
- **Reactive UI Streaming**: Zero-lag UI updates powered by RxJava3 `PublishSubject` and `DiffUtil`-backed `RecyclerView`.
- **Low Memory Overhead**: Direct `ByteBuffer` manipulation to parse raw IP/TCP packet structures without heavy object allocation.
- **Custom Application Bypassing**: Self-exclusion mechanism to prevent recursive loopback tunneling.

## System Architecture

```text
┌─────────────────────────┐
│ Target Application      │
└────────────┬────────────┘
             │ (Outbound TCP Traffic)
             ▼
┌─────────────────────────┐
│ HttpSnifferVpnService   │ ◄── Intercepts L3 IP Packets via TUN Interface
└────────────┬────────────┘
             ▼
┌─────────────────────────┐
│ IpPacketParser          │ ◄── Parses IPv4 Header & TCP State
└────────────┬────────────┘
             ▼
┌─────────────────────────┐
│ HttpProtocolParser      │ ◄── Extracts HTTP Methods, Host & Headers
└────────────┬────────────┘
             ▼
┌─────────────────────────┐
│ PacketRepository        │ ◄── RxJava3 PublishSubject Stream
└────────────┬────────────┘
             ▼
┌─────────────────────────┐
│ MainActivity (UI)       │ ◄── Real-Time Reactive List Updates
└─────────────────────────┘
```

## Tech Stack & Dependencies

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Language** | Java 17 | Core application logic & data processing |
| **Platform SDK** | Android 14 (API 34) | Min SDK 24 (Android 7.0+) |
| **Networking** | `android.net.VpnService` | Native Android packet capture interface |
| **Reactivity** | RxJava3 & RxAndroid | Thread-safe event bus & stream management |
| **UI Components** | Material Components 3 | `RecyclerView`, `ViewBinding`, `MaterialCardView` |
| **Build System** | Gradle (Kotlin DSL) | Android Gradle Plugin 8.2+ |

## Getting Started

### Prerequisites

- Android Studio Jellyfish (2023.3.1) or higher
- JDK 17
- Android device or emulator running API 24+

### Building from Source

```bash
# Clone the repository
git clone https://github.com/stackvoided/SniffNet.git
cd SniffNet

# Build Debug APK
./gradlew assembleDebug
```

The compiled APK will be located at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Project Structure

```text
app/src/main/java/com/example/httpsniffer/
├── model/
│   └── HttpRequestPacket.java       # Immutable POJO representing intercepted requests
├── net/
│   ├── IpPacketParser.java           # IPv4 & TCP binary header extraction
│   └── HttpProtocolParser.java       # L7 HTTP request dissector
├── repository/
│   └── PacketRepository.java         # Central RxJava event bus singleton
├── service/
│   └── HttpSnifferVpnService.java    # Foreground VpnService background worker
└── ui/
    ├── MainActivity.java             # Main activity & VPN lifecycle controller
    └── PacketAdapter.java             # DiffUtil-optimized RecyclerView adapter
```

## How It Works

1. **TUN Interface Setup**: Upon user activation, `HttpSnifferVpnService` requests VPN permissions and instantiates a Virtual Network Interface configured with local route `0.0.0.0/0`.
2. **Buffer Stream Reader**: The service opens a `FileInputStream` on the VPN interface file descriptor, reading raw byte buffers into direct memory (`ByteBuffer`).
3. **L3/L4 Parsing**: `IpPacketParser` validates IPv4 headers, filters for TCP protocol (`0x06`), extracts IP endpoints, and determines payload offsets.
4. **L7 Protocol Identification**: `HttpProtocolParser` checks payload prefixes against standard HTTP verb byte patterns (`GET`, `POST`, etc.) and constructs `HttpRequestPacket` instances.
5. **UI Rendering**: Extracted packets are emitted through `PacketRepository.getPacketStream()`, processed on a background thread, and rendered safely on the main Android thread.

## Security & Privacy Note

SniffNet operates strictly on-device. Intercepted packet metadata is processed exclusively in transient RAM and is never transmitted to external servers or remote endpoints.

## License

This project is licensed under the MIT License - see the `LICENSE` file for details.
