package indi.etern.checkIn.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.action.interfaces.*;
import indi.etern.checkIn.throwable.action.ActionException;
import indi.etern.checkIn.throwable.auth.PermissionDeniedException;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.data.repository.cdi.Eager;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@Eager
public class ActionExecutor {
    @Resource
    private ApplicationContext applicationContext;
    private final Logger logger = LoggerFactory.getLogger(ActionExecutor.class);
    
    private final Map<String, Class<? extends BaseAction<?, ?>>> actionMap = new HashMap<>();
    private final Map<String, Class<? extends BaseAction1<?, ?>>> action1Map = new HashMap<>();
    @Autowired
    private ObjectMapper objectMapper;
    
    public ActionExecutor(ResourceLoader resourceLoader) throws IOException, ClassNotFoundException {
        ClassLoader loader = Action.class.getClassLoader();
        Class<?> baseActionClass = loader.loadClass("indi.etern.checkIn.action.BaseAction");
        Class<?> baseAction1Class = loader.loadClass("indi.etern.checkIn.action.BaseAction1");
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        MetadataReaderFactory metaReader = new CachingMetadataReaderFactory(resourceLoader);
        org.springframework.core.io.Resource[] resources = resolver.getResources("classpath*:indi/etern/checkIn/action/**/*.class");
        
        for (org.springframework.core.io.Resource r : resources) {
            MetadataReader reader = metaReader.getMetadataReader(r);
            String className = reader.getClassMetadata().getClassName();
            Class<?> clazz = loader.loadClass(className);
            
            if (clazz.isAnnotationPresent(Action.class)) {
                Action actionAnnotation = clazz.getAnnotation(Action.class);
                String name = actionAnnotation.value();
                if (baseActionClass.isAssignableFrom(clazz)) {
                    if (actionMap.containsKey(name)) {
                        throw new ActionException("Action name conflict: " + name);
                    } else if (actionAnnotation.exposed()) {
                        //noinspection unchecked
                        actionMap.put(name, (Class<? extends BaseAction<?, ?>>) clazz);
                    }
                } else if (baseAction1Class.isAssignableFrom(clazz)) {
                    if (action1Map.containsKey(name)) {
                        throw new ActionException("Action name conflict: " + name);
                    } else if (actionAnnotation.exposed()) {
                        //noinspection unchecked
                        action1Map.put(name, (Class<? extends BaseAction1<?, ?>>) clazz);
                    }
                } else {
                    throw new RuntimeException("Action class \"" + clazz.getName() + "\" must extends class \"BaseAction\"");
                }
            }
        }
    }
    
    public Result executeByMap(Map<String, Object> contentMap) throws Exception {//TODO
        String actionName = (String) contentMap.get("type");
        final Class<? extends BaseAction<?, ?>> type = actionMap.get(actionName);
        if (!actionMap.containsKey(actionName)) {
            Result result = new Result();
            LinkedHashMap<String, Object> value = new LinkedHashMap<>();
            value.put("type", "error");
            value.put("message", "Action \"" + actionName + "\" not found");
            result.setResult(Optional.of(value));
            return result;
        } else if (BaseAction1.class.isAssignableFrom(type)) {
            throw new ActionException("type wrong");
        }
        final BaseAction<?, ?> action = applicationContext.getBean(type);
        if (action instanceof MapResultAction mapResultAction) {
            Optional<LinkedHashMap<String, Object>> optionalResultData;
            try {
                optionalResultData = mapResultAction.call(contentMap);
            } catch (PermissionDeniedException e) {
                Result result = new Result();
                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                map.put("type", "error");
                map.put("message", "权限不足，需要 \"" + e.getRequiredPermissionName() + "\"");
                result.setResult(Optional.of(map));
                return result;
            } catch (NullPointerException e) {
                Result result = new Result();
                LinkedHashMap<String, Object> value = new LinkedHashMap<>();
                value.put("type", "error");
                value.put("message", "Action \"" + actionName + "\" : parameter absent ( " + e.getMessage() + " )");
                if (logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
                result.setResult(Optional.of(value));
                return result;
            } catch (ClassCastException e) {
                Result result = new Result();
                LinkedHashMap<String, Object> value = new LinkedHashMap<>();
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
            
            LinkedHashMap<String, Object> map;
            map = optionalResultData.orElseGet(LinkedHashMap::new);
            map.put("messageId", contentMap.get("messageId").toString());
            
            logger.debug("Execute by map: {}", actionName);
            return result;
        } else {
            throw new RuntimeException("Not supported action base type");
        }
    }
    
    @SneakyThrows
    public <T, InitDataType> Optional<T> executeByTypeClass(Class<? extends BaseAction<T, InitDataType>> clazz, InitDataType initData) {
        BaseAction<T, InitDataType> action = applicationContext.getBean(clazz);
        return action.call(initData);
    }
    
    @SneakyThrows
    public <I extends InputData, O extends OutputData, A extends BaseAction1<I, O>>
    ResultContext<O> execute(Class<A> actionClass, I inputData) {
        //noinspection unchecked
        return execute(actionClass, Context.class, inputData);
    }
    
    @SneakyThrows
    public <O extends OutputData, A extends BaseAction1<NullInput, O>>
    ResultContext<O> execute(Class<A> actionClass) {
        //noinspection unchecked
        return execute(actionClass, Context.class, new NullInput());
    }
    
    @SneakyThrows
    public <I extends InputData, O extends OutputData, C extends Context<I, O>, A extends BaseAction1<I, O>>
    ResultContext<O> execute(Class<A> actionClass,Class<C> contextClass, I inputData) {
        BaseAction1<I, O> action = applicationContext.getBean(actionClass);
        C context;
        if (contextClass.equals(Context.class)) {
            //safe: contextClass is equals to Context.class
            //noinspection unchecked
            context = (C) applicationContext.getBean("universalActionContext",Context.class);
        } else {
            context = applicationContext.getBean(contextClass);
        }
        context.setInput(inputData);
        action.call(context);
        return context;
    }
    
    public <I extends InputData, O extends OutputData, A extends BaseAction1<I, O>>
    ResultJsonContext<O> executeWithJson(String actionTypeName, String json) throws JsonProcessingException {
        //noinspection unchecked
        Map<Object,Object> map = objectMapper.readValue(json, Map.class);
        final Class<? extends BaseAction1<?, ?>> type = action1Map.get(actionTypeName);
        if (type == null) {
            throw new ActionException("use old executeByMap instead");
        }
        //noinspection unchecked
        Class<A> actionClass = (Class<A>) type;
        //noinspection unchecked
        return (ResultJsonContext<O>) executeWithJson(actionClass, JsonContext.class, json);
    }
    
    public <I extends InputData, O extends OutputData, A extends BaseAction1<I, O>>
    ResultJsonContext<O> executeWithJson(Class<A> actionClass, String json) throws JsonProcessingException {
        //noinspection unchecked
        return (ResultJsonContext<O>) executeWithJson(actionClass, JsonContext.class, json);
    }
    
    public <I extends InputData, O extends OutputData, C extends Context<I, O>, A extends BaseAction1<I, O>>
    ResultContext<O> executeWithJson(Class<A> actionClass,Class<C> contextClass, String json) throws JsonProcessingException {
        BaseAction1<I, O> action = applicationContext.getBean(actionClass);
        
        C context;
        if (contextClass.equals(Context.class)) {
            //safe: contextClass is equals to Context.class
            //noinspection unchecked
            context = (C) applicationContext.getBean("universalActionContext",Context.class);
        } else {
            context = applicationContext.getBean(contextClass);
        }
        
        ResolvableType actionType = ResolvableType.forClass(actionClass);
        ResolvableType baseActionType = ResolvableType.forClass(BaseAction1.class);
        
        while (!Objects.equals(baseActionType.resolve(), actionType.resolve())){
            actionType = actionType.getSuperType();
            if (actionType.equalsType(ResolvableType.NONE)) {
                //won't happen normally
                throw new IllegalStateException("action is not extends BaseAction");
            }
        }
        
        final Class<?> resolve = actionType.getGeneric(0).resolve();
        if (resolve != null && InputData.class.isAssignableFrom(resolve)) {
            //safe: resolve is equals to type of I
            //noinspection unchecked
            I inputData = (I) objectMapper.readValue(json, resolve);
            context.setInput(inputData);
            action.call(context);
            return context;
        } else {
            //won't happen normally
            throw new IllegalStateException("action input is not implements InputData");
        }
    }
}