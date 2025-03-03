package indi.etern.checkIn.action;

import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public abstract class TransactionalAction extends MapResultAction {
    abstract protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception;
    
    @Transactional
    @Override
    public Optional<LinkedHashMap<String, Object>> call(Map<String, Object> initData) {
        try {
            return super.call(initData);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
