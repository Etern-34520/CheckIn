package indi.etern.checkIn.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import indi.etern.checkIn.action.interfaces.InputData;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.action.interfaces.ResultJsonContext;
import indi.etern.checkIn.auth.JwtTokenProvider;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("jsonContext")
@Scope("prototype")
public class JsonContext<I extends InputData, O extends OutputData> extends Context<I, O> implements ResultJsonContext<O> {
    private final ObjectMapper objectMapper;
    
    public JsonContext(JwtTokenProvider jwtTokenProvider, ObjectMapper objectMapper) {
        super(jwtTokenProvider);
        this.objectMapper = objectMapper;
    }
    
    private String jsonResult = null;
    
    @Override
    @SneakyThrows
    public void resolve(O output) {
        super.resolve(output);
        jsonResult = objectMapper.writeValueAsString(output);
    }
    
    @Override
    public Optional<String> getOptionalJsonResult() {
        return Optional.ofNullable(jsonResult);
    }
}
