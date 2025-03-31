package indi.etern.checkIn.api.webSocket;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@Getter
public class Message<D> {
    private Type type;
    protected D data;
    @Setter
    private String contextId;
    
    public Message(Type type, String contextId, D data) {
        this.type = type;
        this.contextId = contextId;
        this.data = data;
    }
    
    public Message(Type type, D data) {
        this.type = type;
        this.data = data;
    }
    
    protected Message() {
    }
    
    @Getter
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = Type.Deserializer.class)
    public static class Type {
        public static class Deserializer extends StdDeserializer<Type> {
            protected Deserializer(Class<?> vc) {super(vc);}
            
            @Override
            public Type deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                return Type.of(jsonParser.getText());
            }
        }
        
        public static Type SUCCESS = new Type("success");
        public static Type WARN = new Type("warn");
        public static Type ERROR = new Type("error");
        
        private final String name;
        
        public Type(String name) {
            this.name = name;
        }
        
        public static Type of(String name) {
            return new Type(name);
        }
        
        public String toString() {
            return name;
        }
        
        public boolean equals(Type other) {
            return other != null && name.equals(other.name);
        }
        
        public int hashCode() {
            return name.hashCode();
        }
    }
    
    public static <O> Message<O> success(String contextId, O data) {
        return new Message<>(Type.SUCCESS,contextId,data);
    }
    
    public static <O> Message<O> warn(String contextId, O data) {
        return new Message<>(Type.WARN,contextId,data);
    }
    
    public static <O> Message<O> error(String contextId, O data) {
        return new Message<>(Type.ERROR,contextId,data);
    }
    
    public static <O> Message<O> of(String typeName, String contextId, O data) {
        return new Message<>(Type.of(typeName),contextId,data);
    }
}
