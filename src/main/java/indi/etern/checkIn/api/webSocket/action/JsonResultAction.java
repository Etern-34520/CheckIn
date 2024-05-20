package indi.etern.checkIn.api.webSocket.action;

import com.google.gson.JsonObject;
import indi.etern.checkIn.auth.JwtTokenProvider;

import java.util.Optional;
import java.util.concurrent.Callable;

public abstract class JsonResultAction implements Callable<Optional<JsonObject>> {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected static final Optional<JsonObject> successOptionalJsonObject;

    static {
        JsonObject successJsonObject = new JsonObject();
        successJsonObject.addProperty("type", "success");
        successOptionalJsonObject = Optional.of(successJsonObject);
    }

    protected boolean logging = true;
    private Optional<JsonObject> result;

    protected JsonResultAction() {
    }

    protected Optional<JsonObject> getOptionalErrorJsonObject(String message) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "error");
        jsonObject.addProperty("message", message);
        return Optional.of(jsonObject);
    }

    @Override
//    @Transactional
    public Optional<JsonObject> call() throws Exception {
//        return TransactionTemplateUtil.getTransactionTemplate().execute((res) -> {
            try {
                String requiredPermissionName = requiredPermissionName();
                if (requiredPermissionName != null && !requiredPermissionName.isEmpty())
                    for (String s : requiredPermissionName.split(",")) {
                        if (!JwtTokenProvider.currentUserHasPermission(s)) {
                            return getOptionalErrorJsonObject("权限不足，需要" + s);
                        }
                    }
                Optional<JsonObject> optionalResult = doAction();
                result = optionalResult;
                return optionalResult;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
//        });
    }

    abstract public String requiredPermissionName();

    abstract protected Optional<JsonObject> doAction() throws Exception;

    public void afterAction() {
    }

    public Optional<JsonObject> logMessage(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<JsonObject> result) {
        return result;
    }

    public boolean shouldLogging() {
        return logging;
    }

    protected JsonObject getSuccessJsonObject() {
        JsonObject successJsonObject = new JsonObject();
        successJsonObject.addProperty("type", "success");
        return successJsonObject;
    }
}
