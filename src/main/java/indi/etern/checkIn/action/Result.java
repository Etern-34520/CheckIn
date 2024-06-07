package indi.etern.checkIn.action;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class Result {
    Optional<JsonObject> result;
//    String loggingMessage = "";
//    boolean shouldLogging;
}
