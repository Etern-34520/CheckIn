package indi.etern.checkIn.api.webSocket;

import indi.etern.checkIn.api.webSocket.interfaces.IMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
public class Message<D> implements IMessage<D> {
    private Type type;
    @Setter
    private String messageId;
    protected D data;
    @Setter
    private String channelName;
    
    public Message(Type type, String messageId, D data) {
        this.type = type;
        this.messageId = messageId;
        this.data = data;
    }
    
    public Message(Type type, D data) {
        this.type = type;
        this.data = data;
    }
    
    protected Message() {
    }
    
    public static <O> Message<O> success(String contextId, O data) {
        return new Message<>(Type.SUCCESS,contextId,data);
    }
    
    public static <O> Message<O> warn(String contextId, O data) {
        return new Message<>(Type.WARNING,contextId,data);
    }
    
    public static <O> Message<O> error(String contextId, O data) {
        return new Message<>(Type.ERROR,contextId,data);
    }
    
    public static <O> Message<O> of(String typeName, String contextId, O data) {
        return new Message<>(Type.of(typeName),contextId,data);
    }
    
    public static <O> Message<O> of(String typeName, O data) {
        return new Message<>(Type.of(typeName), UUID.randomUUID().toString(),data);
    }
}