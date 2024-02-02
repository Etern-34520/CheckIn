package indi.etern.checkIn.entities.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleChoiceQuestion;
import indi.etern.checkIn.entities.question.interfaces.ImagesWith;

import java.io.IOException;

public class ExamQuestionSerializer extends StdSerializer<MultipleChoiceQuestion> {
    public ExamQuestionSerializer() {
        super(MultipleChoiceQuestion.class);
    }
    
    @Override
    public void serialize(MultipleChoiceQuestion multipleChoiceQuestion, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("content", multipleChoiceQuestion.getContent());
        jsonGenerator.writeStringField("type", multipleChoiceQuestion.getClass().getSimpleName());
        jsonGenerator.writeStringField("id", multipleChoiceQuestion.getMd5());
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
            jsonGenerator.writeNumberField("imagesCount",imagesWith.getImagePathStrings().size());
        }
        jsonGenerator.writeEndObject();
    }
}
