package io.github.motwnb.packetsniffer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class PacketSerializer {

    private static final int MAX_BYTES_DISPLAY = 64;

    public static String serialize(Object packet) {
        StringBuilder sb = new StringBuilder();
        try {
            appendFields(packet, packet.getClass(), sb);
        } catch (Exception e) {
            sb.append("  <serialization error: ").append(e.getMessage()).append(">\n");
        }
        return sb.toString();
    }

    private static void appendFields(Object obj, Class<?> clazz, StringBuilder sb) {
        if (clazz == null || clazz == Object.class) return;

        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);
            try {
                Object val = field.get(obj);
                sb.append("  ").append(field.getName()).append(": ").append(format(val)).append('\n');
            } catch (Exception e) {
                sb.append("  ").append(field.getName()).append(": <access error>\n");
            }
        }

        Class<?> parent = clazz.getSuperclass();
        if (parent != null && parent != Object.class) {
            appendFields(obj, parent, sb);
        }
    }

    private static String format(Object val) {
        switch (val) {
            case null -> {
                return "null";
            }
            case byte[] b -> {
                String hex = toHex(b, Math.min(b.length, MAX_BYTES_DISPLAY));
                return "byte[" + b.length + "] " + hex + (b.length > MAX_BYTES_DISPLAY ? "..." : "");
            }
            case int[] a -> {
                return "int[" + a.length + "]";
            }
            case long[] a -> {
                return "long[" + a.length + "]";
            }
            case float[] a -> {
                return "float[" + a.length + "]";
            }
            case double[] a -> {
                return "double[" + a.length + "]";
            }
            case Collection<?> c -> {
                return c.getClass().getSimpleName() + "(size=" + c.size() + ")";
            }
            case Map<?, ?> m -> {
                return "Map(size=" + m.size() + ")";
            }
            case Optional<?> o -> {
                return o.map(v -> "Optional[" + format(v) + "]").orElse("Optional.empty");
            }
            case Enum<?> e -> {
                return e.name();
            }
            default -> {
            }
        }

        return val.toString();
    }

    private static String toHex(byte[] bytes, int limit) {
        StringBuilder sb = new StringBuilder(limit * 2);
        for (int i = 0; i < limit; i++) {
            sb.append(String.format("%02x", bytes[i]));
        }
        return sb.toString();
    }
}
