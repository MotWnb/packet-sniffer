package io.github.motwnb.packetsniffer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class PacketSerializer {

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
                sb.append("  ").append(NameResolver.mapFieldName(clazz, field.getName())).append(": ").append(format(val)).append('\n');
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
                return "byte[" + b.length + "] " + Base64.getEncoder().encodeToString(b);
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
            case Record r -> {
                var components = r.getClass().getRecordComponents();
                StringBuilder rsb = new StringBuilder();
                rsb.append(NameResolver.remapString(r.getClass().getSimpleName())).append('[');
                for (int i = 0; i < components.length; i++) {
                    if (i > 0) rsb.append(", ");
                    rsb.append(NameResolver.mapFieldName(r.getClass(), components[i].getName()));
                    rsb.append('=');
                    try {
                        rsb.append(format(components[i].getAccessor().invoke(r)));
                    } catch (Exception e) {
                        rsb.append("<error>");
                    }
                }
                rsb.append(']');
                return rsb.toString();
            }
            default -> {
            }
        }

        return NameResolver.remapString(val.toString());
    }
}
