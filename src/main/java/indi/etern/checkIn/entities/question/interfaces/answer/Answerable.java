package indi.etern.checkIn.entities.question.interfaces.answer;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Answerable<AnswerSource> {
    @JsonIgnore
    Answer<?,AnswerSource> newAnswerFrom(AnswerSource answerSource);
}