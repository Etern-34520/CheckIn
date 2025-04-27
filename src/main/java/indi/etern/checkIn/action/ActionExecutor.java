package indi.etern.checkIn.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.action.interfaces.*;
import indi.etern.checkIn.throwable.action.ActionException;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@Eager
public class ActionExecutor {
    @Resource
    private ApplicationContext applicationContext;
    private final Logger logger = LoggerFactory.getLogger(ActionExecutor.class);
    
    private final Map<String, Class<? extends BaseAction<?, ?>>> actionMap = new HashMap<>();
    private final ObjectMapper objectMapper;
    
    public ActionExecutor(ResourceLoader resourceLoader, ObjectMapper objectMapper) throws IOException, ClassNotFoundException {
        ClassLoader loader = Action.class.getClassLoader();
        Class<?> baseAction1Class = loader.loadClass("indi.etern.checkIn.action.BaseAction");
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
                if (baseAction1Class.isAssignableFrom(clazz)) {
                    if (actionMap.containsKey(name)) {
                        throw new ActionException("Action name conflict: " + name);
                    } else if (actionAnnotation.exposed()) {
                        //noinspection unchecked
                        actionMap.put(name, (Class<? extends BaseAction<?, ?>>) clazz);
                    }
                } else {
                    throw new RuntimeException("Action class \"" + clazz.getName() + "\" must extends class \"BaseAction\"");
                }
            }
        }
        this.objectMapper = objectMapper;
    }
    
    @SneakyThrows
    public <I extends InputData, O extends OutputData, A extends BaseAction<I, O>>
    ResultContext<O> execute(Class<A> actionClass, I inputData) {
        logger.trace("Executing action \"{}\" with input \"{}\"", actionClass.getName(), inputData);
        //noinspection unchecked
        return execute(actionClass, Context.class, inputData);
    }
    
    @SneakyThrows
    public <O extends OutputData, A extends BaseAction<NullInput, O>>
    ResultContext<O> execute(Class<A> actionClass) {
        logger.trace("Executing action {}", actionClass.getName());
        //noinspection unchecked
        return execute(actionClass, Context.class, new NullInput());
    }
    
    @SneakyThrows
    public <I extends InputData, O extends OutputData, C extends Context<I, O>, A extends BaseAction<I, O>>
    ResultContext<O> execute(Class<A> actionClass,Class<C> contextClass, I inputData) {
        logger.trace("Executing action {} with", actionClass.getName());
        BaseAction<I, O> action = applicationContext.getBean(actionClass);
        C context;
        if (contextClass.equals(Context.class)) {
            //safe: contextClass is equals to Context.class
            //noinspection unchecked
            context = (C) applicationContext.getBean("universalActionContext",Context.class);
        } else {
            context = applicationContext.getBean(contextClass);
        }
        context.setInput(inputData);
        action.execute(context);
//        action.call(context);
        return context;
    }
    
    public <I extends InputData, O extends OutputData, A extends BaseAction<I, O>>
    ResultJsonContext<O> executeWithJson(String actionTypeName, String json) throws JsonProcessingException {
        final Class<? extends BaseAction<?, ?>> type = actionMap.get(actionTypeName);
        //noinspection unchecked
        Class<A> actionClass = (Class<A>) type;
        if (json == null) {
            //noinspection unchecked
            return (ResultJsonContext<O>) execute(actionClass, JsonContext.class, null);
        } else {
            //noinspection unchecked
            return (ResultJsonContext<O>) executeWithJson(actionClass, JsonContext.class, json);
        }
    }
    
    public <I extends InputData, O extends OutputData, A extends BaseAction<I, O>>
    ResultJsonContext<O> executeWithJson(Class<A> actionClass, String json) throws JsonProcessingException {
        //noinspection unchecked
        return (ResultJsonContext<O>) executeWithJson(actionClass, JsonContext.class, json);
    }
    
    public <I extends InputData, O extends OutputData, C extends Context<I, O>, A extends BaseAction<I, O>>
    ResultContext<O> executeWithJson(Class<A> actionClass,Class<C> contextClass, String json) throws JsonProcessingException {
        BaseAction<I, O> action = applicationContext.getBean(actionClass);
        
        C context;
        if (contextClass.equals(Context.class)) {
            //safe: contextClass is equals to Context.class
            //noinspection unchecked
            context = (C) applicationContext.getBean("universalActionContext",Context.class);
        } else {
            context = applicationContext.getBean(contextClass);
        }
        
        ResolvableType actionType = ResolvableType.forClass(actionClass);
        ResolvableType baseActionType = ResolvableType.forClass(BaseAction.class);
        
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
            action.execute(context);
            return context;
        } else {
            //won't happen normally
            throw new IllegalStateException("action input is not implements InputData");
        }
    }
}