package cn.voidnet.miao.annotation;


import cn.voidnet.miao.visualization.VisualizationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebDemo {
    String value() default "";

    String description() default "";

    /**
     * @return What visualization to use. If Table visualization is used, then the return value must be List&lt;T&gt;, where T can be Map (if there is a possibility of empty results it is recommended to specify the table header by setting the tableHeader), or it can be a user-defined class (Getter with attributes is required), or it can be a basic data type
     */
    VisualizationType visualization() default VisualizationType.NONE;

    String[] tableHeaders() default {};


    /**
     * @return What type of object each row of the table is used to infer the table header. Because of the type erasure mechanism, we cannot infer table headers by the return value type.
     */
    Class tableRowType() default Object.class;
}
