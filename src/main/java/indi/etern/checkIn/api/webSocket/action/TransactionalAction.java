package indi.etern.checkIn.api.webSocket.action;

import com.google.gson.JsonObject;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public abstract class TransactionalAction extends JsonResultAction{
    @Transactional
    abstract protected Optional<JsonObject> doAction() throws Exception;
}
