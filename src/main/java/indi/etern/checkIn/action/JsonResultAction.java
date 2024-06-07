package indi.etern.checkIn.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import indi.etern.checkIn.auth.PermissionDeniedException;
import indi.etern.checkIn.entities.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;

public abstract class JsonResultAction extends BaseAction<JsonObject, Map<String, Object>> /*Callable<Optional<JsonObject>>*/ {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected static final Optional<JsonObject> successOptionalJsonObject;
    protected Logger logger = LoggerFactory.getLogger(JsonResultAction.class);
    @Autowired
    ObjectMapper objectMapper;

    @Getter(AccessLevel.PROTECTED)
    final User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    static {
        JsonObject successJsonObject = new JsonObject();
        successJsonObject.addProperty("type", "success");
        successOptionalJsonObject = Optional.of(successJsonObject);
    }

    protected boolean logging = true;
    private Map<String, Object> dataObj;

    protected Optional<JsonObject> getOptionalErrorJsonObject(String message) {
        return Optional.of(getErrorJsonObject(message));
    }

    protected JsonObject getErrorJsonObject(String message) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "error");
        jsonObject.addProperty("message", message);
        return jsonObject;
    }

    @Override
    public Optional<JsonObject> call() throws Exception {
        try {
            return super.call();
        } catch (PermissionDeniedException e) {
            return getOptionalErrorJsonObject("权限不足，需要" + e.getRequiredPermissionName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    abstract public String requiredPermissionName();

    abstract protected Optional<JsonObject> doAction() throws Exception;

    /*public Optional<JsonObject> logMessage(Optional<JsonObject> result) {
        return result;
    }*/

    @Override
    public void initData(Map<String, Object> dataObj) {
        this.dataObj = dataObj;
    }

    public boolean shouldLogging() {
        return logging;
    }

    protected JsonObject getSuccessJsonObject() {
        JsonObject successJsonObject = new JsonObject();
        successJsonObject.addProperty("type", "success");
        return successJsonObject;
    }

    protected void preLog() {
        if (dataObj != null)
            try {
                logger.info(objectMapper.writeValueAsString(dataObj));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
    }
}
