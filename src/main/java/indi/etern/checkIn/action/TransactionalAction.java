package indi.etern.checkIn.action;

import java.util.LinkedHashMap;
import indi.etern.checkIn.utils.TransactionTemplateUtil;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public abstract class TransactionalAction extends MapResultAction {
    abstract protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception;

    @Override
    public Optional<LinkedHashMap<String,Object>> call() throws Exception {
        AtomicReference<Optional<LinkedHashMap<String,Object>>> result = new AtomicReference<>();
        TransactionTemplateUtil.getTransactionTemplate().executeWithoutResult((res)->{
            try {
                result.set(super.call());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return result.get();
    }
}
