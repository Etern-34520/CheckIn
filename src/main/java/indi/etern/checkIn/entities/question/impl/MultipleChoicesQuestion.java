package indi.etern.checkIn.entities.question.impl;

import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.linkUtils.impl.ToQuestionGroupLink;
import indi.etern.checkIn.entities.question.interfaces.answer.Answer;
import indi.etern.checkIn.entities.question.interfaces.answer.Answerable;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.SettingService;
import indi.etern.checkIn.throwable.entity.QuestionBuilderException;
import indi.etern.checkIn.throwable.entity.QuestionException;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.*;

@Entity
@Getter
public class MultipleChoicesQuestion extends Question implements Answerable<List<String>> {
    @Column(name = "sub_type")
    MultipleChoicesQuestion.Type multipleChoiceType;
    @ElementCollection(fetch = FetchType.EAGER)
    @OrderBy("orderIndex")
    @Fetch(FetchMode.SUBSELECT)
    List<Choice> choices;

    protected MultipleChoicesQuestion() {}
    
    public MultipleChoicesQuestion(String questionContent, List<Choice> choices, User author, String explanation) {
        content = questionContent;
        this.explanation = explanation;
        this.choices = choices;
        this.author = author;
        boolean singleCorrect = false;
        boolean multipleCorrect = false;
        int index = 0;
        for (Choice choice : choices) {
            choice.setOrderIndex(index);
            if (!singleCorrect && choice.isCorrect()) {
                singleCorrect = true;
            } else if (choice.isCorrect()) {
                multipleCorrect = true;
            }
            index++;
        }
        if (singleCorrect) multipleChoiceType = Type.SINGLE_CORRECT;
        else if (multipleCorrect) multipleChoiceType = Type.MULTIPLE_CORRECT;
        else throw new QuestionException("No correct choice found");
    }
    
    @Override
    public MultipleChoiceAnswer newAnswerFrom(List<String> choiceIds) {
        final MultipleChoiceAnswer multipleChoiceAnswer = new MultipleChoiceAnswer();
        multipleChoiceAnswer.initFromSource(this, choiceIds);
        return multipleChoiceAnswer;
    }
    
    private void setId(String id) {
        this.id = id;
    }
    
    public enum AnswerCheckingStrategy {
        ALL_CORRECT {
            public MultipleChoiceAnswer.RateData checkAnswer(MultipleChoicesQuestion question, MultipleChoiceAnswer answer) {
                final CountData countData = getCountData(question, answer);
                final float rate = countData.correctCount == countData.correctChoicesCount && countData.wrongCount == 0 ? 1.0f : 0.0f;
                final Answer.CheckedResultType checkedResultType = getCheckedResultTypeStrict(countData);
                return new MultipleChoiceAnswer.RateData(rate, checkedResultType);
            }
        }, CORRECT_RATED {
            public MultipleChoiceAnswer.RateData checkAnswer(MultipleChoicesQuestion question, MultipleChoiceAnswer answer) {
                final CountData countData = getCountData(question, answer);
                final float rate = countData.getWrongRate() > 0f ? 0.0f : countData.getCorrectRate();
                final Answer.CheckedResultType checkedResultType = getCheckedResultTypeRated(countData, rate);
                return new MultipleChoiceAnswer.RateData(rate, checkedResultType);
            }
        }, CORRECT_RATED_AND_WRONG_RATED {
            public MultipleChoiceAnswer.RateData checkAnswer(MultipleChoicesQuestion question, MultipleChoiceAnswer answer) {
                final CountData countData = getCountData(question, answer);
                final float rate = countData.getCorrectRate() - countData.getWrongRate();
                final Answer.CheckedResultType checkedResultType = getCheckedResultTypeRated(countData, rate);
                return new MultipleChoiceAnswer.RateData(rate, checkedResultType);
            }
        }, CORRECT_RATED_AND_WRONG_DOUBLE_RATED {
            public MultipleChoiceAnswer.RateData checkAnswer(MultipleChoicesQuestion question, MultipleChoiceAnswer answer) {
                final CountData countData = getCountData(question, answer);
                final float rate = countData.getCorrectRate() - 2 * countData.getWrongRate();
                final Answer.CheckedResultType checkedResultType = getCheckedResultTypeRated(countData, rate);
                return new MultipleChoiceAnswer.RateData(rate, checkedResultType);
            }
        };
        
        private static Answer.CheckedResultType getCheckedResultTypeStrict(CountData countData) {
            Answer.CheckedResultType checkedResultType;
            if (countData.correctCount == countData.correctChoicesCount && countData.wrongCount == 0) {
                checkedResultType = Answer.CheckedResultType.CORRECT;
            } else {
                checkedResultType = Answer.CheckedResultType.WRONG;
            }
            return checkedResultType;
        }
        
        private static Answer.CheckedResultType getCheckedResultTypeRated(CountData countData, float rate) {
            Answer.CheckedResultType checkedResultType;
            if (countData.correctCount == countData.correctChoicesCount && countData.wrongCount == 0) {
                checkedResultType = Answer.CheckedResultType.CORRECT;
            } else if (countData.correctCount == 0 || rate <= 0) {
                checkedResultType = Answer.CheckedResultType.WRONG;
            } else {
                checkedResultType = Answer.CheckedResultType.HALF_CORRECT;
            }
            return checkedResultType;
        }
        
        abstract MultipleChoiceAnswer.RateData checkAnswer(MultipleChoicesQuestion question, MultipleChoiceAnswer answer);
        
        CountData getCountData(MultipleChoicesQuestion question, MultipleChoiceAnswer answer) {
            int correctCount = 0;
            int wrongCount = 0;
            final Set<Choice> correctChoices = question.multipleChoiceType.getCorrectChoices(question);
            int correctChoicesCount = correctChoices.size();
            int wrongChoiceCount = question.getChoices().size() -  correctChoicesCount;
            
            for (Choice choice : answer.getSelectedChoices()) {
                if (correctChoices.contains(choice)) {
                    correctCount++;
                } else {
                    wrongCount++;
                }
            }
            return new CountData(correctCount, wrongCount, correctChoicesCount, wrongChoiceCount);
        }
        
        record CountData(int correctCount, int wrongCount, int correctChoicesCount, int wrongChoicesCount) {
            public float getCorrectRate() {
                return (float) correctCount / correctChoicesCount;
            }
            public float getWrongRate() {
                if (wrongChoicesCount == 0) return 0f;
                return (float) wrongCount / wrongChoicesCount;
            }
        }
    }
    
    public enum Type {
        SINGLE_CORRECT() {
            public Set<Choice> getCorrectChoices(MultipleChoicesQuestion question) {
                List<Choice> choices = question.getChoices();
                for (Choice choice : choices) {
                    if (choice.isCorrect()) {
                        return Collections.singleton(choice);
                    }
                }
                throw new QuestionException("No correct choice found");
            }
        }, MULTIPLE_CORRECT() {
            public Set<Choice> getCorrectChoices(MultipleChoicesQuestion question) {
                List<Choice> choices = question.getChoices();
                Set<Choice> correctChoices = new HashSet<>();
                for (Choice choice : choices) {
                    if (choice.isCorrect()) {
                        correctChoices.add(choice);
                    }
                }
                if (correctChoices.isEmpty()) {
                    throw new QuestionException("No correct choice found");
                }
                return correctChoices;
            }
        };
        
        abstract Set<Choice> getCorrectChoices(MultipleChoicesQuestion question);
    }
    
    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        @Getter
        final List<Choice> choices = new ArrayList<>();
        @Getter
        final Map<String, String> imageBase64Strings = new LinkedHashMap<>();
        MultipleChoicesQuestion multipleQuestion;
        String id;
        String questionContent;
        User author;
        boolean enable = false;
        QuestionLinkImpl<?> linkWrapper;
        private String explanation;

        public static Builder from(MultipleChoicesQuestion question) {
            final Builder builder = new Builder().setQuestionContent(question.getContent())
                    .addChoices(question.getChoices())
                    .setAuthor(question.getAuthor())
                    .setEnable(question.isEnabled())
                    .setId(question.getId())
                    .setExplanation(question.getExplanation());
            builder.setLink(question.getLinkWrapper());
            return builder;
        }
        
        private void setLink(QuestionLinkImpl<?> link) {
            linkWrapper = link;
        }
        
        public Builder usePartitionLinks(ToPartitionsLink.Configurator configurator) {
            if (linkWrapper == null) {
                linkWrapper = new ToPartitionsLink();
            }
            configurator.configure((ToPartitionsLink) linkWrapper);
            return this;
        }
        
        public Builder useQuestionGroupLinks(ToQuestionGroupLink.Configurator configurator) {
            if (linkWrapper == null) {
                linkWrapper = new ToQuestionGroupLink();
            }
            configurator.configure((ToQuestionGroupLink) linkWrapper);
            return this;
        }
        
        public Builder addChoice(Choice choice) {
            choices.add(choice);
            return this;
        }
        
        public Builder addChoices(List<Choice> choices) {
            this.choices.addAll(choices);
            return this;
        }
        
        public Builder setQuestionContent(String content) {
            questionContent = content;
            return this;
        }
        
        public Builder addBase64Image(String name, String base64String) {
            imageBase64Strings.put(name, base64String);
            return this;
        }
        
        public Builder setAuthor(User author) {
            this.author = author;
            return this;
        }
        
        public Builder setEnable(boolean enable) {
            this.enable = enable;
            return this;
        }
        
        public MultipleChoicesQuestion build() {
            boolean singleCorrect = false;
            boolean multipleCorrect = false;
            for (Choice choice : choices) {
                if (choice.isCorrect() && !multipleCorrect && !singleCorrect) {
                    singleCorrect = true;
                } else if (choice.isCorrect() && singleCorrect) {
                    multipleCorrect = true;
                    singleCorrect = false;
                }
            }
            
            if (!singleCorrect && !multipleCorrect) {
                throw new QuestionBuilderException("No correct choice");
            }
            if (questionContent == null) {
                throw new QuestionBuilderException("Question content is null");
            }
            if (choices.size() < 2) {
                throw new QuestionBuilderException("Less than two choices");
            }
            if (linkWrapper == null) {
                String string = SettingService.singletonInstance.getItem("other", "defaultPartitionName").getValue(String.class);
                if (string == null) string = "undefined";
                String finalString = string;
                usePartitionLinks(partitionLink -> partitionLink.getTargets().add(Partition.ofName(finalString)));
            }

            if (linkWrapper instanceof ToPartitionsLink toPartitionsLink) {
                toPartitionsLink.getTargets().forEach(partition -> partition.getQuestionLinks().add(toPartitionsLink));
            }
            multipleQuestion = new MultipleChoicesQuestion(questionContent, choices, author, explanation);
            multipleQuestion.setEnabled(enable);
            if (id != null) {
                multipleQuestion.setId(id);
            } else {
                multipleQuestion.initId();
            }
            if (singleCorrect) {
                multipleQuestion.multipleChoiceType = Type.SINGLE_CORRECT;
            } else {
                multipleQuestion.multipleChoiceType = Type.MULTIPLE_CORRECT;
            }
            multipleQuestion.setLinkWrapper(linkWrapper);
            multipleQuestion.setImages(imageBase64Strings);
            return multipleQuestion;
        }
        
        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setExplanation(String explanation) {
            this.explanation = explanation;
            return this;
        }
    }
    
}
