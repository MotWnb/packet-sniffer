package io.github.motwnb.packetsniffer;

import net.minecraft.network.protocol.Packet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class PacketLogger {

    private static final DateTimeFormatter SESSION_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final AtomicLong COUNTER = new AtomicLong(0);

    private static ExecutorService executor;
    private static BufferedWriter listWriter;
    private static BufferedWriter contentWriter;
    private static volatile boolean active = false;

    public static void init(Path gameDir) {
        try {
            Path logDir = gameDir.resolve("packet_logs");
            Files.createDirectories(logDir);
            String session = LocalDateTime.now().format(SESSION_FMT);
            listWriter = Files.newBufferedWriter(logDir.resolve(session + "_list.log"));
            contentWriter = Files.newBufferedWriter(logDir.resolve(session + "_content.log"));
            executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "PacketSniffer-IO");
                t.setDaemon(true);
                return t;
            });
            active = true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize PacketLogger", e);
        }
    }

    public static void log(Packet<?> packet, String direction) {
        if (!active) return;

        long id = COUNTER.incrementAndGet();
        long timestamp = System.currentTimeMillis();
        String packetId = packet.type().id().toString();
        String className = NameResolver.mapSimpleClassName(packet.getClass());
        String fields = PacketSerializer.serialize(packet);

        executor.submit(() -> {
            try {
                String time = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()
                ).format(TIME_FMT);

                listWriter.write(String.format("#%d [%s] [%s] %s (%s)%n", id, time, direction, packetId, className));
                listWriter.flush();

                contentWriter.write(String.format("=== #%d [%s] [%s] %s (%s) ===%n", id, time, direction, packetId, className));
                contentWriter.write(fields);
                contentWriter.newLine();
                contentWriter.flush();
            } catch (IOException ignored) {
            }
        });
    }

    public static void shutdown() {
        active = false;
        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
        }
        closeQuietly(listWriter);
        closeQuietly(contentWriter);
    }

    private static void closeQuietly(BufferedWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ignored) {
            }
        }
    }
}
