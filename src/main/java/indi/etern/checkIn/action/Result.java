package indi.etern.checkIn.action;

import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class Result {
    Optional<LinkedHashMap<String,Object>> result;
//    String loggingMessage = "";
//    boolean shouldLogging;
}
