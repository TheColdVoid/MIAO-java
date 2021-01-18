package cn.voidnet.miao.visualization;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Visualization {
    String getType();

    @JsonIgnore
    VisualizationType getVisualizationTypeEnum();
}
