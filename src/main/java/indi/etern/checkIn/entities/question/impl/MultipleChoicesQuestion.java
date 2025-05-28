package indi.etern.checkIn.entities.question.impl;

import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.linkUtils.impl.ToQuestionGroupLink;
import indi.etern.checkIn.entities.question.interfaces.RandomOrderable;
import indi.etern.checkIn.entities.question.interfaces.answer.Answerable;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.SettingService;
import indi.etern.checkIn.throwable.entity.QuestionBuilderException;
import indi.etern.checkIn.throwable.entity.QuestionException;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.*;

@Entity
@Getter
public class MultipleChoicesQuestion extends Question implements RandomOrderable, Answerable<List<String>> {
    protected boolean randomOrdered;
    
    protected MultipleChoicesQuestion() {
    }
    
    public MultipleChoicesQuestion(String questionContent, List<Choice> choices/*, Set<Partition> partitions*/, User author) {
        content = questionContent;
        this.choices = choices;
//        this.partitions = partitions;
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
    
    boolean checkAnswer(MultipleChoiceAnswer answer) {
        if (answer instanceof MultipleChoiceAnswer multipleChoiceAnswer) {
            final Set<Choice> correctChoices = multipleChoiceType.getCorrectChoices(this);
            return multipleChoiceAnswer.getSelectedChoices().containsAll(correctChoices) &&
                    correctChoices.containsAll(multipleChoiceAnswer.getSelectedChoices());
        } else {
            throw new ClassCastException("MultipleChoiceAnswer type not match");
        }
    }
    
    @Override
    public MultipleChoiceAnswer newAnswerFrom(List<String> choiceIds) {
        final MultipleChoiceAnswer multipleChoiceAnswer = new MultipleChoiceAnswer();
        multipleChoiceAnswer.initFromSource(this, choiceIds);
        return multipleChoiceAnswer;
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
    
    @Column(name = "sub_type")
//    @Enumerated(EnumType.STRING)
    MultipleChoicesQuestion.Type multipleChoiceType;
    
    @ElementCollection
    @OrderBy("orderIndex")
//    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
//    @Fetch(value = FetchMode.SUBSELECT)
//    @JoinColumn(name = "question_id", referencedColumnName = "id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    List<Choice> choices;
    
    private void setRandomOrdered(boolean randomOrdered) {
        this.randomOrdered = randomOrdered;
    }
    
    private void setId(String id) {
        this.id = id;
    }
    
    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        MultipleChoicesQuestion multipleQuestion;
        @Getter
        final List<Choice> choices = new ArrayList<>();
        String id;
        String questionContent;
        @Getter
        final Map<String, String> imageBase64Strings = new LinkedHashMap<>();
        User author;
        boolean enable = false;
        QuestionLinkImpl<?> linkWrapper;
        private boolean randomOrdered = false;
        
        public static Builder from(MultipleChoicesQuestion multiPartitionableQuestion) {
            final Builder builder = new Builder().setQuestionContent(multiPartitionableQuestion.getContent())
                    .addChoices(multiPartitionableQuestion.getChoices())
                    .setAuthor(multiPartitionableQuestion.getAuthor())
                    .setEnable(multiPartitionableQuestion.isEnabled())
                    .setRandomOrdered(multiPartitionableQuestion.isRandomOrdered())
                    .setId(multiPartitionableQuestion.getId());
            if (multiPartitionableQuestion.getLinkWrapper() instanceof ToPartitionsLink toPartitionsLinkWrapper) {
                builder.usePartitionLinks(toPartitionLinkWrapper1 -> {
                    Set<Partition> targets = toPartitionLinkWrapper1.getTargets();
                    targets.clear();
                    targets.addAll(toPartitionsLinkWrapper.getTargets());
                });
            } else if (multiPartitionableQuestion.getLinkWrapper() instanceof ToQuestionGroupLink toQuestionGroupLinkWrapper) {
                toQuestionGroupLinkWrapper.getTarget().getQuestionLinks().clear();
                builder.useQuestionGroupLinks((toQuestionGroupLinkWrapper1 -> toQuestionGroupLinkWrapper1.setTarget(toQuestionGroupLinkWrapper.getTarget())));
            }
            return builder;
        }
        
        public Builder usePartitionLinks(ToPartitionsLink.Configurator configurator) {
            linkWrapper = new ToPartitionsLink();
            configurator.configure((ToPartitionsLink) linkWrapper);
            return this;
        }
        
        public Builder useQuestionGroupLinks(ToQuestionGroupLink.Configurator configurator) {
            linkWrapper = new ToQuestionGroupLink();
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
        
        public Builder setRandomOrdered(boolean randomOrdered) {
            this.randomOrdered = randomOrdered;
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
                throw new QuestionBuilderException("No correct choice found");
            }
            if (questionContent == null) {
                throw new QuestionBuilderException("Question content not set");
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
            multipleQuestion = new MultipleChoicesQuestion(questionContent, choices, /*partitions,*/ author);
            multipleQuestion.setEnabled(enable);
            multipleQuestion.setRandomOrdered(randomOrdered);
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
            multipleQuestion.setImageBase64Strings(imageBase64Strings);
            return multipleQuestion;
        }
        
        public Builder setId(String id) {
            this.id = id;
            return this;
        }
    }
    
}
