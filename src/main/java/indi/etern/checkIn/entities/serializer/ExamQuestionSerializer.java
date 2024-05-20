package indi.etern.checkIn.entities.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleChoicesQuestion;

import java.io.IOException;

public class ExamQuestionSerializer extends StdSerializer<MultipleChoicesQuestion> {
    public ExamQuestionSerializer() {
        super(MultipleChoicesQuestion.class);
    }
    
    @Override
    public void serialize(MultipleChoicesQuestion multipleChoiceQuestion, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        /*jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("content", multipleChoiceQuestion.getContent());
        jsonGenerator.writeStringField("type", multipleChoiceQuestion.getClass().getSimpleName());
        jsonGenerator.writeStringField("id", multipleChoiceQuestion.getId());
        jsonGenerator.writeObjectFieldStart("choices");
        multipleChoiceQuestion.getChoices().forEach(choice -> {
            try {
                jsonGenerator.writeStringField(choice.getId(),choice.getContent());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        jsonGenerator.writeEndObject();
        if (multipleChoiceQuestion instanceof ImagesWith imagesWith) {
            jsonGenerator.writeNumberField("imagesCount",imagesWith.getImageBase64Strings().size());
        }
        jsonGenerator.writeEndObject();*/
    }
}
