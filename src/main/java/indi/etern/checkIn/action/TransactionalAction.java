package indi.etern.checkIn.action;

import com.google.gson.JsonObject;
import indi.etern.checkIn.utils.TransactionTemplateUtil;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public abstract class TransactionalAction extends JsonResultAction{
    abstract protected Optional<JsonObject> doAction() throws Exception;

    @Override
    public Optional<JsonObject> call() throws Exception {
        AtomicReference<Optional<JsonObject>> result = new AtomicReference<>();
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
