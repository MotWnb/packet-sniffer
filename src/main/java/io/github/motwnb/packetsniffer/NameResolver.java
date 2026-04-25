package io.github.motwnb.packetsniffer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameResolver {

    private static final ConcurrentHashMap<String, String> CLASS_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> FIELD_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> SIMPLE_CLASS_MAP = new ConcurrentHashMap<>();
    private static final Pattern INTERMEDIARY_PATTERN = Pattern.compile("\\bclass_\\d+\\b");
    private static volatile boolean loaded = false;

    public static void init() {
        try (InputStream is = NameResolver.class.getClassLoader()
                .getResourceAsStream("packetsniffer/mappings.txt")) {
            if (is == null) return;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    String[] parts = line.split("\t");
                    if (parts.length == 3 && "C".equals(parts[0])) {
                        CLASS_MAP.put(parts[1], parts[2]);
                        String intSimple = parts[1].substring(parts[1].lastIndexOf('.') + 1);
                        String namedSimple = parts[2].substring(parts[2].lastIndexOf('.') + 1);
                        SIMPLE_CLASS_MAP.put(intSimple, namedSimple);
                        if (intSimple.contains("$")) {
                            String innerInt = intSimple.substring(intSimple.lastIndexOf('$') + 1);
                            String innerNamed = namedSimple.contains("$")
                                    ? namedSimple.substring(namedSimple.lastIndexOf('$') + 1)
                                    : namedSimple;
                            SIMPLE_CLASS_MAP.putIfAbsent(innerInt, innerNamed);
                        }
                    } else if (parts.length == 4 && "F".equals(parts[0])) {
                        FIELD_MAP.put(parts[1] + "#" + parts[2], parts[3]);
                    }
                }
            }
            loaded = true;
        } catch (Exception ignored) {
        }
    }

    public static String mapClassName(Class<?> clazz) {
        if (!loaded) return clazz.getName();
        return CLASS_MAP.getOrDefault(clazz.getName(), clazz.getName());
    }

    public static String mapSimpleClassName(Class<?> clazz) {
        String full = mapClassName(clazz);
        int dot = full.lastIndexOf('.');
        return dot >= 0 ? full.substring(dot + 1) : full;
    }

    public static String mapFieldName(Class<?> owner, String fieldName) {
        if (!loaded) return fieldName;
        return FIELD_MAP.getOrDefault(owner.getName() + "#" + fieldName, fieldName);
    }

    public static String remapString(String s) {
        if (!loaded) return s;
        Matcher m = INTERMEDIARY_PATTERN.matcher(s);
        if (!m.find()) return s;
        m.reset();
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(
                    SIMPLE_CLASS_MAP.getOrDefault(m.group(), m.group())
            ));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
