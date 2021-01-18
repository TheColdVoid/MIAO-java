package cn.voidnet.miao;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;

@Data
@Builder
class ParameterInfo {
    static HashMap<String, String> javaTypeNameToJsTypeNameTable =
            new HashMap<>();

    static {
        javaTypeNameToJsTypeNameTable.put("java.lang.String", "string");
        javaTypeNameToJsTypeNameTable.put("java.lang.Character", "string");
        javaTypeNameToJsTypeNameTable.put("char", "string");
        javaTypeNameToJsTypeNameTable.put("java.lang.Boolean", "bool");
        javaTypeNameToJsTypeNameTable.put("boolean", "bool");
        javaTypeNameToJsTypeNameTable.put("Object", "object");
    }

    String name;
    String typeName;
    String displayName;

    public String getDisplayName() {
        if (Util.isEmpty(displayName))
            return null;
        return this.displayName;
    }

    public String getTypeName() {
        return javaTypeNameToJsTypeName(this.typeName);
    }

    private String javaTypeNameToJsTypeName(String javaTypeName) {
        if (javaTypeNameToJsTypeNameTable.containsKey(javaTypeName))
            return javaTypeNameToJsTypeNameTable.get(javaTypeName);
        else
            //TODO:Add support for more detailed numeric types, such as support for more detailed display of real numbers and integers, etc.
            return "number";
    }

}
