package indi.etern.checkIn.entities.question.impl.question;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionLink;
import indi.etern.checkIn.entities.linkUtils.impl.ToQuestionGroupLink;
import indi.etern.checkIn.entities.question.impl.Choice;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.interfaces.RandomOrderable;
import indi.etern.checkIn.entities.serializer.ExamQuestionSerializer;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.SettingService;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.*;

@JsonSerialize(using = ExamQuestionSerializer.class)
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Getter
public class MultipleChoicesQuestion extends Question implements RandomOrderable {
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
        if (singleCorrect) type = Type.SINGLE_CORRECT;
        else if (multipleCorrect) type = Type.MULTIPLE_CORRECT;
        else throw new QuestionException("No correct choice found");
    }

    public enum Type {
        SINGLE_CORRECT("single_correct") {
            private Choice correctChoice;

            public Choice getCorrectChoice(List<Choice> choices) {
                if (correctChoice != null) {
                    return correctChoice;
                }
                for (Choice choice : choices) {
                    if (choice.isCorrect()) {
                        correctChoice = choice;
                        return choice;
                    }
                }
                throw new QuestionException("No correct choice found");
            }
        }, MULTIPLE_CORRECT("multiple_correct") {
            private List<Choice> correctChoices;

            public List<Choice> getCorrectChoices(List<Choice> choices) {
                if (correctChoices != null) {
                    return correctChoices;
                }
                List<Choice> correctChoices = new ArrayList<>();
                for (Choice choice : choices) {
                    if (choice.isCorrect()) {
                        correctChoices.add(choice);
                    }
                }
                if (correctChoices.isEmpty()) {
                    throw new QuestionException("No correct choice found");
                }
                this.correctChoices = correctChoices;
                return correctChoices;
            }
        };

        private final String name;

        Type(String name) {
            this.name = name;
        }
    }

    @Column(name = "sub_type", nullable = false)
//    @Enumerated(EnumType.STRING)
    MultipleChoicesQuestion.Type type;

    @OrderBy("orderIndex")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY,orphanRemoval = true)
    @Fetch(value = FetchMode.SUBSELECT)
    @JoinColumn(name = "question_id", referencedColumnName = "id")
    List<Choice> choices;

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        MultipleChoicesQuestion multipleQuestion;
        @Getter
        final List<Choice> choices = new ArrayList<>();
        String id;
        String questionContent;
        @Getter
        final Set<Partition> partitions = new HashSet<>();
        @Getter
        final Map<String, String> imageBase64Strings = new LinkedHashMap<>();
        User author;
        boolean enable = false;
        QuestionLinkImpl<?> linkWrapper;
        private boolean manualLink = false;
        private boolean randomOrdered = false;
        
        public static Builder from(MultipleChoicesQuestion multiPartitionableQuestion) {
            final Builder builder = new Builder().setQuestionContent(multiPartitionableQuestion.getContent())
                    .addChoices(multiPartitionableQuestion.getChoices())
                    .setAuthor(multiPartitionableQuestion.getAuthor())
                    .setEnable(multiPartitionableQuestion.isEnabled())
                    .setRandomOrdered(multiPartitionableQuestion.isRandomOrdered())
                    .setId(multiPartitionableQuestion.getId());
            if (multiPartitionableQuestion.getLinkWrapper() instanceof ToPartitionLink toPartitionLinkWrapper) {
                builder.usePartitionLinks(toPartitionLinkWrapper1 -> {
                    Set<Partition> targets = toPartitionLinkWrapper1.getTargets();
                    targets.clear();
                    targets.addAll(toPartitionLinkWrapper.getTargets());
                });
            } else if (multiPartitionableQuestion.getLinkWrapper() instanceof ToQuestionGroupLink toQuestionGroupLinkWrapper) {
                toQuestionGroupLinkWrapper.getTarget().getQuestionLinks().clear();
                builder.useQuestionGroupLinks((toQuestionGroupLinkWrapper1 -> {
                    toQuestionGroupLinkWrapper1.setTarget(toQuestionGroupLinkWrapper.getTarget());
                }));
            }
            return builder;
        }
        
        public Builder usePartitionLinks(ToPartitionLink.Configurator configurator) {
            linkWrapper = new ToPartitionLink();
            configurator.configure((ToPartitionLink) linkWrapper);
            return this;
        }
        
        public Builder useQuestionGroupLinks(ToQuestionGroupLink.Configurator configurator) {
            linkWrapper = new ToQuestionGroupLink();
            configurator.configure((ToQuestionGroupLink) linkWrapper);
            return this;
        }
        
        public Builder useManualLinkLater() {
            manualLink = true;
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
                throw new MultipleQuestionBuilderException("No correct choice found");
            }
            if (questionContent == null) {
                throw new MultipleQuestionBuilderException("Question content not set");
            }
            if (choices.size() < 2) {
                throw new QuestionException("Less than two choices");
            }
            if (!manualLink && linkWrapper == null) {
                throw new QuestionException("link not set");
            }
            if (partitions.isEmpty()) {
                String string = SettingService.singletonInstance.getItem("other.defaultPartitionName").getValue().toString();
                if (string == null) string = "undefined";
                partitions.add(Partition.getInstance(string));
            }
            multipleQuestion = new MultipleChoicesQuestion(questionContent, choices, /*partitions,*/ author);
            multipleQuestion.setEnabled(enable);
            multipleQuestion.setRandomOrdered(randomOrdered);
            if (id != null) {
                multipleQuestion.setId(id);
            } else {
                multipleQuestion.initId();
            }
            if (!manualLink) {
                multipleQuestion.setLinkWrapper(linkWrapper);
            }
/*
            if (id != null) {
                try {
                    final Field idField = Question.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(multipleQuestion, id);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new MultipleQuestionBuilderException("editing id");
                }
            } else {
                multipleQuestion.initId();
            }
*/
/*
            if (!questionContent.isEmpty()) {//非模板题目
                for (Partition partition : partitions) {
    //                final PartitionService partitionService = (PartitionService) CheckInApplication.applicationContext.getBean("partitionService");
                    PartitionService.singletonInstance.addQuestionOf(partition, multipleQuestion);
    //                partition.addQuestion(multipleQuestion);
                }
            }
*/
//            if (multipleQuestion instanceof ImagesWith imagesWith)
            multipleQuestion.setImageBase64Strings(imageBase64Strings);
            return multipleQuestion;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }
    }
    
    private void setRandomOrdered(boolean randomOrdered) {
        this.randomOrdered = randomOrdered;
    }
    
    private void setId(String id) {
        this.id = id;
    }
}
