package indi.etern.checkIn.action;

import com.google.gson.JsonObject;
import indi.etern.checkIn.auth.PermissionDeniedException;
import indi.etern.checkIn.entities.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;

public abstract class JsonResultAction extends BaseAction<JsonObject, Map<String, Object>> /*Callable<Optional<JsonObject>>*/ {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected static final Optional<JsonObject> successOptionalJsonObject;

    @Getter(AccessLevel.PROTECTED)
    final User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    static {
        JsonObject successJsonObject = new JsonObject();
        successJsonObject.addProperty("type", "success");
        successOptionalJsonObject = Optional.of(successJsonObject);
    }

    protected boolean logging = true;

    protected Optional<JsonObject> getOptionalErrorJsonObject(String message) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "error");
        jsonObject.addProperty("message", message);
        return Optional.of(jsonObject);
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

    public boolean shouldLogging() {
        return logging;
    }

    protected JsonObject getSuccessJsonObject() {
        JsonObject successJsonObject = new JsonObject();
        successJsonObject.addProperty("type", "success");
        return successJsonObject;
    }
}
