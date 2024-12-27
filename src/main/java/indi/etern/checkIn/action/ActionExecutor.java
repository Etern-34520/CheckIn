package indi.etern.checkIn.action;

import indi.etern.checkIn.action.interfaces.Action;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
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
import java.util.LinkedHashMap;
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
                    throw new RuntimeException("Action class \"" + clazz.getName() + "\" must extends MapResultAction");
                }
                Action actionAnnotation = clazz.getAnnotation(Action.class);
                String name = actionAnnotation.value();
                if (actionMap.containsKey(name)) {
                    throw new RuntimeException("Action name conflict: " + name);
                } else if (actionAnnotation.exposed()) {
                    //noinspection unchecked
                    actionMap.put(name, (Class<? extends BaseAction<?, ?>>) clazz);
                }
            }
        }
    }

    public Result executeByMap(Map<String, Object> contentMap) throws Exception {
        String actionName = (String) contentMap.get("type");
        if (!actionMap.containsKey(actionName)) {
            Result result = new Result();
            LinkedHashMap<String,Object> value = new LinkedHashMap<>();
            value.put("type", "error");
            value.put("message", "Action \"" + actionName + "\" not found");
            result.setResult(Optional.of(value));
            return result;
        }
        final BaseAction<?, ?> action = applicationContext.getBean(actionMap.get(actionName));
        if (action instanceof MapResultAction mapResultAction) {
            try{
                mapResultAction.initData(contentMap);
            } catch (NullPointerException e) {
                Result result = new Result();
                LinkedHashMap<String,Object> value = new LinkedHashMap<>();
                value.put("type", "error");
                value.put("message", "Action \"" + actionName + "\" : parameter absent ( " + e.getMessage() + " )");
                result.setResult(Optional.of(value));
                return result;
            } catch (ClassCastException e) {
                Result result = new Result();
                LinkedHashMap<String,Object> value = new LinkedHashMap<>();
                value.put("type", "error");
                value.put("message", "Action \"" + actionName + "\" : parameter type error ( " + e.getMessage() + " )");
                result.setResult(Optional.of(value));
                return result;
            }
            
            Result result = new Result();
            Optional<LinkedHashMap<String,Object>> optionalResult = mapResultAction.call();
            result.setResult(optionalResult);

            LinkedHashMap<String,Object> map;
            map = optionalResult.orElseGet(LinkedHashMap::new);
            map.put("messageId", contentMap.get("messageId").toString());
            
            logger.debug("Execute by map: {}", actionName);
            return result;
        } else {
            throw new RuntimeException("Not supported action base type");
        }
    }

    @SneakyThrows
    public <T> Optional<T> executeByTypeClass(Class<? extends BaseAction<T, ?>> clazz) {
        BaseAction<T, ?> action = applicationContext.getBean(clazz);
        return action.call();
    }
    public <T> Optional<T> executeByTypeClass(Class<? extends BaseAction<T, ?>> clazz, Object dataObj) {
        BaseAction<?, ?> action = applicationContext.getBean(clazz);
        Method[] methods = action.getClass().getMethods();
        try {
            for (Method method : methods) {
                if (method.getName().equals("initData")) {
                    Class<?> parameterType = method.getParameterTypes()[0];
                    if (parameterType.isInstance(dataObj)) {
                        method.invoke(action, dataObj);
                        break;
                    } else {
                        throw new ClassCastException("dataMap cannot be cased to '"+ parameterType +"'");
                    }
                }
            }
            logger.debug("Execute by type class and data object: {}", clazz.getName());
            //noinspection unchecked
            return (Optional<T>) action.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}