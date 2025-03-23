package indi.etern.checkIn.api.webSocket;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Getter;

@Getter
public class JsonRawMessage extends Message<String> {
    @JsonRawValue
    String data;
}
