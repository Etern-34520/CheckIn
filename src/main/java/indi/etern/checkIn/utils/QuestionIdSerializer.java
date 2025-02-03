package indi.etern.checkIn.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import indi.etern.checkIn.entities.question.impl.Question;

import java.io.IOException;

public class QuestionIdSerializer extends JsonSerializer<Question> {
    @Override
    public void serialize(Question value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.getId());
    }
}
