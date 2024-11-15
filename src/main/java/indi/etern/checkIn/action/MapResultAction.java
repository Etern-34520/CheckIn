package indi.etern.checkIn.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.auth.PermissionDeniedException;
import indi.etern.checkIn.entities.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public abstract class MapResultAction extends BaseAction<LinkedHashMap<String,Object>, Map<String, Object>> /*Callable<Optional<LinkedHashMap<String,Object>>>*/ {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected static final Optional<LinkedHashMap<String,Object>> successOptionalMap;
    protected Logger logger = LoggerFactory.getLogger(MapResultAction.class);
    @Autowired
    ObjectMapper objectMapper;

    @Getter(AccessLevel.PROTECTED)
    final User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    static {
        LinkedHashMap<String,Object> successLinkedHashMap = new LinkedHashMap<>();
        successLinkedHashMap.put("type", "success");
        successOptionalMap = Optional.of(successLinkedHashMap);
    }

    protected boolean logging = true;
    private Map<String, Object> dataMap;

    protected Optional<LinkedHashMap<String,Object>> getOptionalErrorMap(String message) {
        return Optional.of(getErrorMap(message));
    }

    protected LinkedHashMap<String,Object> getErrorMap(String message) {
        LinkedHashMap<String,Object> map = new LinkedHashMap<>();
        map.put("type", "error");
        map.put("message", message);
        return map;
    }

    @Override
    public Optional<LinkedHashMap<String,Object>> call() throws Exception {
        try {
            return super.call();
        } catch (PermissionDeniedException e) {
            return getOptionalErrorMap("权限不足，需要" + e.getRequiredPermissionName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    abstract public String requiredPermissionName();

    abstract protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception;

    /*public Optional<LinkedHashMap<String,Object>> logMessage(Optional<LinkedHashMap<String,Object>> result) {
        return result;
    }*/

    @Override
    public void initData(Map<String, Object> dataMap) {
        this.dataMap = dataMap;
    }

    public boolean shouldLogging() {
        return logging;
    }

    protected LinkedHashMap<String,Object> getSuccessMap() {
        LinkedHashMap<String,Object> successLinkedHashMap = new LinkedHashMap<>();
        successLinkedHashMap.put("type", "success");
        return successLinkedHashMap;
    }

    protected void preLog() {
        if (dataMap != null)
            try {
                logger.info(objectMapper.writeValueAsString(dataMap));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
    }
}