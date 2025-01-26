package indi.etern.checkIn.action;

import indi.etern.checkIn.throwable.auth.PermissionDeniedException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public abstract class MapResultAction extends BaseAction<LinkedHashMap<String,Object>, Map<String, Object>> /*Callable<Optional<LinkedHashMap<String,Object>>>*/ {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected static final Optional<LinkedHashMap<String,Object>> successOptionalMap;

    static {
        LinkedHashMap<String,Object> successLinkedHashMap = new LinkedHashMap<>();
        successLinkedHashMap.put("type", "success");
        successOptionalMap = Optional.of(successLinkedHashMap);
    }

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
    
    protected LinkedHashMap<String,Object> getSuccessMap() {
        LinkedHashMap<String,Object> successLinkedHashMap = new LinkedHashMap<>();
        successLinkedHashMap.put("type", "success");
        return successLinkedHashMap;
    }
}