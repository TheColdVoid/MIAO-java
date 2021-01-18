package cn.voidnet.miao;

import cn.voidnet.miao.annotation.Parameter;
import cn.voidnet.miao.annotation.WebDemo;
import cn.voidnet.miao.visualization.TableVisualization;
import cn.voidnet.miao.visualization.Visualization;
import cn.voidnet.miao.visualization.VisualizationType;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import lombok.extern.slf4j.Slf4j;
import org.reflections8.Reflections;
import org.reflections8.scanners.MethodAnnotationsScanner;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static io.javalin.apibuilder.ApiBuilder.*;

;

@Slf4j
public class MIAO {
    HashMap<Integer, MethodInfo> methodMap = new HashMap<>();
    Javalin app = null;
    MetaInfo metaInfo = new MetaInfo();

    public MIAO(int port, String packagePrefix, String title) {
        log.info("Starting MIAO...");
        log.info("Scanning methods in package " + packagePrefix);
        //TODO:Make this option changeable via parameters
        String host = "0.0.0.0";

        metaInfo.title = Optional.ofNullable(title).orElse("MIAO");

        //begin method signatures scanning
        Reflections reflections = new Reflections(packagePrefix, new MethodAnnotationsScanner());

        Set<Method> annotated = reflections.getMethodsAnnotatedWith(WebDemo.class);

        //TODO:need to refactor
        annotated
                .stream()
                .filter(this::checkIsMethodAvailable)
                .forEach(method ->
                        methodMap.put(method.hashCode(), MethodInfo
                                .builder()
                                .method(method)
                                .name(method.getName())
                                .hashcode(method.hashCode())
                                .displayName(
                                        method
                                                .getAnnotation(WebDemo.class)
                                                .value()
                                )
                                .description(
                                        method
                                                .getAnnotation(WebDemo.class)
                                                .description()
                                )
                                .parameterInfos(
                                        Arrays.stream(method
                                                .getParameters()
                                        )
                                                .map(parameter -> ParameterInfo
                                                        .builder()
                                                        .typeName(parameter.getType().getName())
                                                        .name(parameter.getName())
                                                        .displayName(
                                                                Optional.ofNullable(
                                                                        parameter
                                                                                .getAnnotation(Parameter.class)
                                                                )
                                                                        .map(it -> it.value())
                                                                        .orElse(null)
                                                        )
                                                        .build()
                                                )
                                                .collect(Collectors.toList())
                                )
                                .visualization(
                                        getVisualization(method)
                                )
                                .build()
                        )
                );
        log.info("Generating scheme completed");
        this.app = Javalin.create(config -> {
            config.defaultContentType = "application/json";
            config.enableCorsForAllOrigins();
            config.addStaticFiles("/static");
            config.showJavalinBanner = false;
        }).routes(() -> {
            path("call", () -> {
                post(":method-hash", this::handleMethodCall);
            });
            path("scheme", () -> {
                get("", this::handleFetchScheme);
            });
            path("meta-info", () -> {
                get("", this::handleFetchMetaInfo);
            });
        }).start(host, port);
        log.info("\n\tThe MIAO server is started,\n" +
                "\tplease enter this URL in your browser to access this application:\n"
                + "\t" + getHostName(host, port)
        );
        openBrowser(getHostName(host, port));
    }

    public static MIAO start(int port) {
        return new MIAO(port, "", null);
    }

    public static MIAO start(String title, int port, String packagePrefix) {
        return new MIAO(port, packagePrefix, title);
    }

    public static MIAO start(String title, int port) {
        return new MIAO(port, "", title);
    }

    public static MIAO start(String title) {
        return new MIAO(2333, "", title);
    }

    public static MIAO start() {
        return new MIAO(2333, "", null);
    }

    private Visualization getVisualization(Method method) {
        VisualizationType type = method.getAnnotation(WebDemo.class).visualization();
        switch (type) {
            case TABLE:
                return getTableVisualization(method);
            case NONE:
            default:
                return null;
        }
    }

    private TableVisualization getTableVisualization(Method method) {
        Util.getAllPropertyName(ParameterInfo.class);
        TableVisualization tvis = new TableVisualization();
        List<String> tableHeaders =
                Arrays.asList(method.getAnnotation(WebDemo.class).tableHeaders().clone());
        Class<?> returnType = method.getReturnType();
        Class<?> rowType = method.getAnnotation(WebDemo.class).tableRowType();
        if (!returnType.isAssignableFrom(List.class) && !returnType.isAssignableFrom(Set.class)) {
            log.warn("The visualization type of method {} is set to Table, but it does not return data of type List or Set, so the visualization will not be applied to the demo corresponding to the method", method.getName());
            return null;
        }
        if (Util.isBasicType(rowType)) {
            tvis.isBasicList = true;
        } else if (rowType.isAssignableFrom(Map.class)) {
            //Nothing to do
        } else {
            tvis.headers = Util.getAllPropertyName(rowType);
        }

        //if table_headers is exist,take it as headers and overwrite other options
        if (!tableHeaders.isEmpty())
            tvis.headers = tableHeaders;
        return tvis;


        // Because of the Type Erasure mechanism, the table header information cannot be inferred
        // from the generic information of the returned value type


    }

    public void stop() {
        this.app.stop();
    }

    private void handleMethodCall(Context ctx) throws InvocationTargetException, IllegalAccessException {
        List<Object> parameters = ctx.bodyAsClass(List.class);
        int methodHash = Integer.parseInt(ctx.pathParam("method-hash"));
        if (methodMap.containsKey(methodHash)) {
            MethodInfo method = methodMap.get(methodHash);
            Object returnValue = method.method.invoke(null, parameters.toArray());
            if (returnValue != null) {
                try {
                    if (getMethodVisualizationType(methodHash).equals(VisualizationType.TABLE))
                        ctx.json(((Collection) returnValue).toArray());
                    ctx.json(returnValue);
                } catch (Exception e) {
                    if (e instanceof JsonMappingException)
                        log.error("Error during serialization, make sure the object returned by the {} method contains the Getter methods", method.getName());
                    else
                        log.error(e.getLocalizedMessage());
                }
            }
        } else {
            //hashcode not found
            ctx.status(412);
        }

    }

    private void handleFetchScheme(Context ctx) {
        ctx.json(getScheme());

    }

    private void handleFetchMetaInfo(Context ctx) {
        ctx.json(metaInfo);
    }

    public List<MethodInfo> getScheme() {
        return methodMap
                .entrySet()
                .stream()
                .map(it -> it.getValue())
                .sorted(Comparator.comparing(it -> it.name))
                .collect(Collectors.toList());
    }

    private VisualizationType getMethodVisualizationType(int hashcode) {
        return Optional.of(methodMap.get(hashcode))
                .map(MethodInfo::getVisualization)
                .map(Visualization::getVisualizationTypeEnum)
                .orElse(VisualizationType.NONE);
    }

    private boolean checkIsMethodAvailable(Method method) {
        boolean isStatic = Modifier.isStatic(method.getModifiers());
        boolean isMethodPublic = Modifier.isPublic(method.getModifiers());
        boolean isClassPublic = Modifier.isPublic(method.getDeclaringClass().getModifiers());
        if (!isStatic)
            log.warn("Skipped:{}\n.This non-static method is skipped, because of the library does not support non-static methods."
                    , method.getName());
        if ((!isClassPublic) || (!isMethodPublic))
            log.warn("Skipped:{}\n.Because of Java's reflection safety feature, methods that are not Public (or the class they belong to is not Public) will not be called by external libraries, so this method is skipped."
                    , method.getName());
        return isStatic && isClassPublic && isMethodPublic;
    }

    private boolean openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("xdg-open " + url);
            }
        } catch (Exception e) {
            //Whether the browser starts successfully or not is an unimportant matter
            return false;
        }
        return true;
    }

    private String getHostName(String host, int port) {
        return "http://" +
                (host.equals("0.0.0.0") ? "localhost" : host) +
                ":" + port + "/";
    }
}
