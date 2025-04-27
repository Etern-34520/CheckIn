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
    
    public JsonRawMessage(Type type, String messageId, String rawData) {
        this.type = type;
        this.messageId = messageId;
        this.data = rawData;
    }
    
    public JsonRawMessage(Type type, String data) {
        this.type = type;
        this.data = data;
    }
    
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
//            final TreeNode treeNode = jsonParser.readValueAsTree();
//            return treeNode.toString();
            
            //based on DeepSeek
            // 1. 创建TokenBuffer并复制当前属性的Token流
            try (TokenBuffer tokenBuffer = new TokenBuffer(jsonParser)) {
                tokenBuffer.copyCurrentStructure(jsonParser); // 自动遍历并复制整个结构
                
                // 2. 将TokenBuffer写入字符串
                StringWriter sw = new StringWriter();
                try (JsonGenerator gen = jsonParser.getCodec().getFactory().createGenerator(sw)) {
                    tokenBuffer.serialize(gen);
                }
                return sw.toString();
            }
        }
    }
}
