package cn.voidnet.miao.visualization;

import lombok.Data;

import java.util.List;

@Data
public class TableVisualization implements Visualization {
    public List<String> headers;
    public Boolean isBasicList = false;
    public Boolean isCustomClass = false;


    @Override
    public VisualizationType getVisualizationTypeEnum() {
        return VisualizationType.TABLE;
    }

    @Override
    public String getType() {
        return "table";
    }
}
