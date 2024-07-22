package indi.etern.checkIn.action;

import com.google.gson.JsonObject;
import indi.etern.checkIn.action.interfaces.Action;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.data.repository.cdi.Eager;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Eager
public class ActionExecutor {
    @Resource
    private ApplicationContext applicationContext;
    private final Logger logger = LoggerFactory.getLogger(ActionExecutor.class);

    private final Map<String, Class<? extends BaseAction<?, ?>>> actionMap = new HashMap<>();

    public ActionExecutor(ResourceLoader resourceLoader) throws IOException, ClassNotFoundException {
        ClassLoader loader = Action.class.getClassLoader();
        Class<?> baseActionClass = loader.loadClass("indi.etern.checkIn.action.BaseAction");
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        MetadataReaderFactory metaReader = new CachingMetadataReaderFactory(resourceLoader);
        org.springframework.core.io.Resource[] resources = resolver.getResources("classpath*:indi/etern/checkIn/action/**/*.class");

        for (org.springframework.core.io.Resource r : resources) {
            MetadataReader reader = metaReader.getMetadataReader(r);
            String className = reader.getClassMetadata().getClassName();
            Class<?> clazz = loader.loadClass(className);

            if (clazz.isAnnotationPresent(Action.class)) {
                if (!baseActionClass.isAssignableFrom(clazz)) {
                    throw new RuntimeException("Action class \"" + clazz.getName() + "\" must extends JsonResultAction");
                }
                Action actionAnnotation = clazz.getAnnotation(Action.class);
                String name = actionAnnotation.name();
                if (actionMap.containsKey(name)) {
                    throw new RuntimeException("Action name conflict: " + name);
                }
                //noinspection unchecked
                actionMap.put(name, (Class<? extends BaseAction<?, ?>>) clazz);
            }
        }
    }

    public Result executeByMap(Map<String, Object> contentMap) throws Exception {
        String actionName = (String) contentMap.get("type");
        if (!actionMap.containsKey(actionName)) {
            Result result = new Result();
            JsonObject value = new JsonObject();
            value.addProperty("type", "error");
            value.addProperty("message", "Action \"" + actionName + "\" not found");
            result.setResult(Optional.of(value));
            return result;
        }
        JsonResultAction jsonResultAction = (JsonResultAction) applicationContext.getBean(actionMap.get(actionName));
        try {
            jsonResultAction.initData(contentMap);
        } catch (NullPointerException e) {
            Result result = new Result();
            JsonObject value = new JsonObject();
            value.addProperty("type", "error");
            value.addProperty("message", "Action \"" + actionName + "\" : parameter absent");
            result.setResult(Optional.of(value));
            return result;
        } catch (ClassCastException e) {
            Result result = new Result();
            JsonObject value = new JsonObject();
            value.addProperty("type", "error");
            value.addProperty("message", "Action \"" + actionName + "\" : parameter type error");
            result.setResult(Optional.of(value));
            return result;
        }
        jsonResultAction.preLog();

        Result result = new Result();
        Optional<JsonObject> optionalResult = jsonResultAction.call();
        result.setResult(optionalResult);

/*
        if (contentMap.get("expectResponse") != null && !((boolean) contentMap.get("expectResponse"))) {
            result.setShouldLogging(jsonResultAction.shouldLogging());
            return result;
        }
*/
        JsonObject jsonObject;
        jsonObject = optionalResult.orElseGet(JsonObject::new);
        jsonObject.addProperty("messageId", contentMap.get("messageId").toString());
        /*{
            String message = jsonResultAction.getLogMessage(optionalResult);
            result.setLoggingMessage(message);
        }*/
        logger.debug("Execute by map: {}", actionName);
        return result;
    }

    public Optional<?> executeByTypeClass(Class<? extends BaseAction<?, ?>> clazz, Object dataObj) {
        BaseAction<?, ?> action = applicationContext.getBean(clazz);
//        action.initData(dataObj);
        Method[] methods = action.getClass().getMethods();
        try {
            for (Method method : methods) {
                if (method.getName().equals("initData")) {
                    Class<?> parameterType = method.getParameterTypes()[0];
                    if (parameterType.isInstance(dataObj)) {
                        method.invoke(action, dataObj);
                        break;
                    } else {
                        throw new ClassCastException("dataObj cannot be cased to '"+ parameterType +"'");
                    }
                }
            }
            action.preLog();
            logger.debug("Execute by type class and data object: {}", clazz.getName());
            return action.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<?> executeByTypeString(String type, Object dataObj) {
        return executeByTypeClass(actionMap.get(type), dataObj);
    }
}
