package cn.voidnet.miao;

import cn.voidnet.miao.visualization.Visualization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Builder
@Data
public class MethodInfo {
    int hashcode;
    String name;
    String description;

    String displayName;
    @Builder.Default
    List<ParameterInfo> parameterInfos = new ArrayList<>();

    @JsonIgnore
    Method method;
    Visualization visualization;


    public String getDisplayName() {
        if (displayName.isEmpty())
            return null;
        return displayName;
    }

    public String getDescription() {
        if (description.isEmpty())
            return null;
        return description;
    }
}
