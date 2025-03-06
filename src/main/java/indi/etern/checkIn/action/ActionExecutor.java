package indi.etern.checkIn.action;

import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.throwable.auth.PermissionDeniedException;
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
            Optional<LinkedHashMap<String,Object>> optionalResultData;
            try{
                optionalResultData = mapResultAction.call(contentMap);
            } catch (PermissionDeniedException e) {
                Result result = new Result();
                LinkedHashMap<String,Object> map = new LinkedHashMap<>();
                map.put("type", "error");
                map.put("message", "权限不足，需要 \"" + e.getRequiredPermissionName() + "\"");
                result.setResult(Optional.of(map));
                return result;
            } catch (NullPointerException e) {
                Result result = new Result();
                LinkedHashMap<String,Object> value = new LinkedHashMap<>();
                value.put("type", "error");
                value.put("message", "Action \"" + actionName + "\" : parameter absent ( " + e.getMessage() + " )");
                if (logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
                result.setResult(Optional.of(value));
                return result;
            } catch (ClassCastException e) {
                Result result = new Result();
                LinkedHashMap<String,Object> value = new LinkedHashMap<>();
                value.put("type", "error");
                value.put("message", "Action \"" + actionName + "\" : parameter type error ( " + e.getMessage() + " )");
                if (logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
                result.setResult(Optional.of(value));
                return result;
            }
            Result result = new Result();
            result.setResult(optionalResultData);

            LinkedHashMap<String,Object> map;
            map = optionalResultData.orElseGet(LinkedHashMap::new);
            map.put("messageId", contentMap.get("messageId").toString());
            
            logger.debug("Execute by map: {}", actionName);
            return result;
        } else {
            throw new RuntimeException("Not supported action base type");
        }
    }

    @SneakyThrows
    public <T,InitDataType> Optional<T> executeByTypeClass(Class<? extends BaseAction<T, InitDataType>> clazz, InitDataType initData) {
        BaseAction<T, InitDataType> action = applicationContext.getBean(clazz);
        return action.call(initData);
    }
}