package indi.etern.checkIn.api.webSocket.action;

import com.google.gson.JsonObject;
import indi.etern.checkIn.auth.JwtTokenProvider;
import indi.etern.checkIn.utils.TransactionTemplateUtil;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.Callable;

public abstract class JsonResultAction implements Callable<Optional<JsonObject>> {
    protected JsonResultAction() {
    }
    
    protected Optional<JsonObject> getOptionalErrorJsonObject(String message) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "error");
        jsonObject.addProperty("message", message);
        return Optional.of(jsonObject);
    }
    
    @Override
    @Transactional
    public Optional<JsonObject> call() throws Exception {
        return TransactionTemplateUtil.getTransactionTemplate().execute((res) -> {
            try {
                String requiredPermissionName = requiredPermissionName();
                if (requiredPermissionName!=null&&!JwtTokenProvider.currentUserHasPermission(requiredPermissionName)) {
                    return getOptionalErrorJsonObject("权限不足");
                }
                return doAction();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    abstract public String requiredPermissionName();
    
    abstract protected Optional<JsonObject> doAction() throws Exception;
    
    public void afterAction() {
    }
}
