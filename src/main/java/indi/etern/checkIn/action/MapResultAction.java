package indi.etern.checkIn.action;

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
    public Optional<LinkedHashMap<String,Object>> call(Map<String, Object> initData) throws Exception {
        try {
            return super.call(initData);
        } catch (RuntimeException e) {
            throw e;
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