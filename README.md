# Packet Sniffer

A lightweight Fabric mod that captures all C2S/S2C network packets to disk with async I/O. No Fabric API required.

## Requirements

- Minecraft 1.21.11+
- Java 21+

## Building

```bash
./gradlew build
```

Drop the jar from `build/libs/` into `.minecraft/mods/`.

## How It Works

Mixin injects into `Connection.send` (C2S) and `Connection.channelRead0` (S2C). A single-thread executor handles all file I/O off the network thread. Packet fields are serialized via reflection.

Two files are generated per session in `.minecraft/packet_logs/`:

- `*_list.log` -- one line per packet: index, timestamp, direction, class name
- `*_content.log` -- full field dump of each packet

## Limitations

- Packets that fail during Netty decoding (before channelRead0) are not captured.
- No log rotation. High throughput will produce large files.

## License

MIT