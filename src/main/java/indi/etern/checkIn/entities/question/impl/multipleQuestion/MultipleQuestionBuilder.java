package indi.etern.checkIn.entities.question.impl.multipleQuestion;

import indi.etern.checkIn.entities.question.Question;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.SettingService;
import jakarta.servlet.http.Part;
import lombok.Getter;
import org.apache.commons.io.file.PathUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

public class MultipleQuestionBuilder {
    MultiPartitionableQuestion multipleQuestion;
    @Getter
    final List<Choice> choices = new ArrayList<>();
    String md5;
    String questionContent;
    @Getter
    final Set<Partition> partitions = new HashSet<>();
    @Getter
    final List<Part> imageParts = new ArrayList<>();

    User author;
    boolean enable = false;

    public static MultipleQuestionBuilder from(MultipleChoiceQuestion multiPartitionableQuestion) {
        return new MultipleQuestionBuilder().setQuestionContent(multiPartitionableQuestion.getContent())
                .addChoices(multiPartitionableQuestion.getChoices())
                .addPartitions(multiPartitionableQuestion.getPartitions())
                .setAuthor(multiPartitionableQuestion.getAuthor())
                .setEnable(multiPartitionableQuestion.isEnabled())
                .setMD5(multiPartitionableQuestion.getMd5());
    }

    private MultipleQuestionBuilder addPartitions(Collection<Partition> partitions) {
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

    public MultipleQuestionBuilder addImage(Part part) {
        imageParts.add(part);
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
            if (imageParts.isEmpty())
                multipleQuestion = new SingleCorrectQuestion(questionContent, choices, partitions, author);
            else
                multipleQuestion = new SingleCorrectQuestionWithImages(questionContent, choices, partitions, author);
        } else {
            if (imageParts.isEmpty())
                multipleQuestion = new MultipleCorrectQuestion(questionContent, choices, partitions, author);
            else
                multipleQuestion = new MultipleCorrectQuestionWithImages(questionContent, choices, partitions, author);
        }
        multipleQuestion.setEnabled(enable);
        if (md5 != null) {
            try {
                final Field md5Field = Question.class.getDeclaredField("md5");
                md5Field.setAccessible(true);
                md5Field.set(multipleQuestion, md5);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new MultipleQuestionBuilderException("editing md5");
            }
        } else {
            multipleQuestion.initMD5();
        }
        if (!questionContent.isEmpty()) {//非模板题目
            for (Partition partition : partitions) {
//                final PartitionService partitionService = (PartitionService) CheckInApplication.applicationContext.getBean("partitionService");
                PartitionService.singletonInstance.addQuestionOf(partition, multipleQuestion);
//                partition.addQuestion(multipleQuestion);
            }
        }
        List<String> imagePathStrings = new ArrayList<>();
        final Path imagesPath = Path.of("data/images/" + multipleQuestion.getMd5() + "/");
        if (!imageParts.isEmpty()) {
            if (!Files.exists(imagesPath)) {
                try {
                    Files.createDirectories(imagesPath);
                } catch (Exception e) {
                    throw new MultipleQuestionBuilderException("On creating images directory:" + e.getMessage());
                }
            }
            try {
                for (Part imagePart : imageParts) {
                    final String pathString = "data/images/" + multipleQuestion.getMd5() + "/" + imagePart.getSubmittedFileName();
                    final Path path = Path.of(pathString);
                    if (!Files.exists(path)) {
                        Files.createFile(path);
                    }
                    final OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.WRITE);
                    BufferedOutputStream bos = new BufferedOutputStream(outputStream, 1024);
                    final InputStream inputStream = imagePart.getInputStream();
//                    inputStream.skip(new String(imagePart.getInputStream().readNBytes(25)).split("base64,",2)[0].length()+7);
                    inputStream.transferTo(bos);
                    inputStream.close();
                    outputStream.close();
                    bos.close();
                    imagePathStrings.add(pathString);
                }
            } catch (Exception e) {
                throw new MultipleQuestionBuilderException("On writing image to path:" + e.getMessage());
            }
            try (Stream<Path> pathStream = Files.list(imagesPath)) {
                pathStream.forEach(path1 -> {
                    if (!imagePathStrings.contains(path1.toString().replace('\\', '/'))) {
                        try {
                            Files.delete(path1);
                        } catch (Exception e) {
                            throw new MultipleQuestionBuilderException("On deleting unused images:" + e.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                throw new MultipleQuestionBuilderException("On deleting unused images:" + e.getMessage());
            }
        } else {
            try {
                if (Files.exists(imagesPath))
                    PathUtils.deleteDirectory(imagesPath);
            } catch (IOException e) {
                throw new MultipleQuestionBuilderException("On deleting unused images:" + e.getMessage());
            }
        }
        if (multipleQuestion instanceof MultipleCorrectQuestionWithImages) {
            ((MultipleCorrectQuestionWithImages) multipleQuestion).setImagePathStrings(imagePathStrings);
        } else if (multipleQuestion instanceof SingleCorrectQuestionWithImages) {
            ((SingleCorrectQuestionWithImages) multipleQuestion).setImagePathStrings(imagePathStrings);
        }
        return multipleQuestion;
    }

    public MultipleQuestionBuilder setMD5(String md5) {
        this.md5 = md5;
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
