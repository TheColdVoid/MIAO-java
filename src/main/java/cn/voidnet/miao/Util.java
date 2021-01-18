package cn.voidnet.miao;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Util {
    static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    static boolean isBasicType(Class<?> clazz) {
        return clazz.isPrimitive() || isWrapClass(clazz) || clazz.equals(String.class);
    }

    static boolean isWrapClass(Class clz) {
        try {
            return ((Class) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

    static List<String> getAllPropertyName(Class clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(it -> it.getName().matches("^(?:get|is)[A-Z].*"))
                .map(it -> {
                            Matcher matcher = Pattern.compile("^(?:get|is)([A-Z].*)")
                                    .matcher(it.getName());
                            matcher.find();
                            return matcher.group(1);
                        }
                )
                .map(it -> it.substring(0, 1).toLowerCase() + it.substring(1))
                .collect(Collectors.toList());

    }
}
