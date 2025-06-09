package indi.etern.checkIn.api.webSocket;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import indi.etern.checkIn.api.webSocket.interfaces.IMessage;
import lombok.Getter;

import java.io.IOException;
import java.io.StringWriter;

@Getter
public class JsonRawMessage implements IMessage<String> {
    private Type type;
    private String messageId;
    @JsonRawValue
    @JsonDeserialize(using = JsonRawMessage.RawDeserializer.class)
    protected String data;
    
    @SuppressWarnings("unused")
    public JsonRawMessage(Type type, String messageId, String rawData) {
        this.type = type;
        this.messageId = messageId;
        this.data = rawData;
    }
    
    @SuppressWarnings("unused")
    public JsonRawMessage(Type type, String data) {
        this.type = type;
        this.data = data;
    }
    
    @SuppressWarnings("unused")
    protected JsonRawMessage() {
    }
    
    @Override
    public Type getType() {
        return type;
    }
    
    @Override
    public String getMessageId() {
        return messageId;
    }
    
    @Override
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public static class RawDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            try (TokenBuffer tokenBuffer = new TokenBuffer(jsonParser)) {
                tokenBuffer.copyCurrentStructure(jsonParser); // 自动遍历并复制整个结构
                
                StringWriter sw = new StringWriter();
                try (JsonGenerator gen = jsonParser.getCodec().getFactory().createGenerator(sw)) {
                    tokenBuffer.serialize(gen);
                }
                return sw.toString();
            }
        }
    }
}
