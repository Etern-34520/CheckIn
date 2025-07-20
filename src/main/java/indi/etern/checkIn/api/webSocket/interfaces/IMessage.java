package indi.etern.checkIn.api.webSocket.interfaces;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import indi.etern.checkIn.api.webSocket.Message;
import lombok.Getter;

import java.io.IOException;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface IMessage {
    Type getType();
    
    String getMessageId();
    
    void setMessageId(String messageId);
    
    @Getter
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = Message.Type.Deserializer.class)
    class Type {
        
        public static class Deserializer extends StdDeserializer<Message.Type> {
            protected Deserializer() {
                super(Type.class);
            }
            
            @Override
            public Type deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                return IMessage.Type.of(jsonParser.getText());
            }
        }
        
        public static final Type SUCCESS = new Type("success");
        public static final Type WARNING = new Type("warning");
        public static final Type ERROR = new Type("error");
        
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
}
