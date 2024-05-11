package indi.etern.checkIn.entities.question.impl.multipleQuestion;

import indi.etern.checkIn.entities.question.Question;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.SettingService;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.*;

@SuppressWarnings("UnusedReturnValue")
public class MultipleQuestionBuilder {
    MultiPartitionableQuestion multipleQuestion;
    @Getter
    final List<Choice> choices = new ArrayList<>();
    String id;
    String questionContent;
    @Getter
    final Set<Partition> partitions = new HashSet<>();
    @Getter
    final Map<String,String> imageBase64Strings = new LinkedHashMap<>();

    User author;
    boolean enable = false;

    public static MultipleQuestionBuilder from(MultipleChoiceQuestion multiPartitionableQuestion) {
        return new MultipleQuestionBuilder().setQuestionContent(multiPartitionableQuestion.getContent())
                .addChoices(multiPartitionableQuestion.getChoices())
                .addPartitions(multiPartitionableQuestion.getPartitions())
                .setAuthor(multiPartitionableQuestion.getAuthor())
                .setEnable(multiPartitionableQuestion.isEnabled())
                .setId(multiPartitionableQuestion.getId());
    }

    public MultipleQuestionBuilder addPartitions(Set<Partition> partitions) {
        this.partitions.addAll(partitions);
        return this;
    }

    public MultipleQuestionBuilder addChoice(Choice choice) {
        choices.add(choice);
        return this;
    }

    public MultipleQuestionBuilder addChoices(List<Choice> choices) {
        this.choices.addAll(choices);
        return this;
    }

    public MultipleQuestionBuilder addChoices(Choice... choices) {
        Collections.addAll(this.choices, choices);
        return this;
    }

    public MultipleQuestionBuilder setQuestionContent(String content) {
        questionContent = content;
        return this;
    }

//    @Deprecated
//    public MultipleQuestionBuilder addImage(Part part) {
//        imageParts.add(part);
//        return this;
//    }

    public MultipleQuestionBuilder addBase64Image(String name, String base64String) {
        imageBase64Strings.put(name,base64String);
        return this;
    }

    public MultipleQuestionBuilder addPartition(String partitionString) {
        partitions.add(Partition.getInstance(partitionString));
        return this;
    }

    public MultipleQuestionBuilder addPartition(Partition partition) {
        partitions.add(partition);
        return this;
    }

    public MultipleQuestionBuilder setAuthor(User author) {
        this.author = author;
        return this;
    }

    public MultipleQuestionBuilder setEnable(boolean enable) {
        this.enable = enable;
        return this;
    }

    public MultiPartitionableQuestion build() {
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

        if (partitions.isEmpty()) {
            String string = SettingService.singletonInstance.get("other.defaultPartitionName");
            if (string == null) string = "undefined";
            partitions.add(Partition.getInstance(string));
        }
        if (singleCorrect) {
            if (imageBase64Strings.isEmpty())
                multipleQuestion = new SingleCorrectQuestion(questionContent, choices, partitions, author);
            else
                multipleQuestion = new SingleCorrectQuestionWithImages(questionContent, choices, partitions, author);
        } else {
            if (imageBase64Strings.isEmpty())
                multipleQuestion = new MultipleCorrectQuestion(questionContent, choices, partitions, author);
            else
                multipleQuestion = new MultipleCorrectQuestionWithImages(questionContent, choices, partitions, author);
        }
        multipleQuestion.setEnabled(enable);
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
        if (!questionContent.isEmpty()) {//非模板题目
            for (Partition partition : partitions) {
//                final PartitionService partitionService = (PartitionService) CheckInApplication.applicationContext.getBean("partitionService");
                PartitionService.singletonInstance.addQuestionOf(partition, multipleQuestion);
//                partition.addQuestion(multipleQuestion);
            }
        }
        if (multipleQuestion instanceof MultipleCorrectQuestionWithImages) {
            ((MultipleCorrectQuestionWithImages) multipleQuestion).setImageBase64Strings(imageBase64Strings);
        } else if (multipleQuestion instanceof SingleCorrectQuestionWithImages) {
            ((SingleCorrectQuestionWithImages) multipleQuestion).setImageBase64Strings(imageBase64Strings);
        }
        return multipleQuestion;
    }

    public MultipleQuestionBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public MultipleQuestionBuilder addPartition(int partitionId) {
        Partition partition = Partition.getInstance(partitionId);
        if (partition == null) {
            partition = Partition.getInstance("undefined");
        }
        partitions.add(partition);
        return this;
    }

    public MultipleQuestionBuilder addPartitionsById(List<Integer> partitionId) {
        for (Integer id : partitionId) {
            partitions.add(Partition.getInstance(id));
        }
        return this;
    }
}
