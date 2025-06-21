package indi.etern.checkIn.utils;

import indi.etern.checkIn.dto.manage.*;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.question.impl.MultipleChoicesQuestion;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.QuestionGroup;
import indi.etern.checkIn.service.dao.QuestionService;
import indi.etern.checkIn.service.dao.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class QuestionCreateUtils {
    private static <T extends CommonQuestionDTO> MultipleChoicesQuestion create(T commonQuestionDTO, LinkHandler<T> linkHandler) {
        String id = commonQuestionDTO.getId();
        Optional<Question> questionOptional = QuestionService.singletonInstance.findById(id);
        
        MultipleChoicesQuestion.Builder builder;
        MultipleChoicesQuestion multipleChoicesQuestion = null;
        if (questionOptional.isEmpty()) {
            builder = new MultipleChoicesQuestion.Builder();
        } else {
            final Question question = questionOptional.get();
            if (question instanceof MultipleChoicesQuestion multipleChoicesQuestion1) {
                multipleChoicesQuestion = multipleChoicesQuestion1;
                builder = MultipleChoicesQuestion.Builder.from(multipleChoicesQuestion1);
            } else {
                throw new RuntimeException("Question is not a multiple choice question");
            }
        }
        builder.setId(id);
        
        final String content = commonQuestionDTO.getContent();
        if (content != null) {
            builder.setQuestionContent(content);
        }
        
        Boolean enabled = commonQuestionDTO.getEnabled();
        if (enabled != null) {
            builder.setEnable(enabled);
        }
        
        if (commonQuestionDTO instanceof MultipleChoicesQuestionDTO multipleChoicesQuestionDTO) {
            List<ChoiceDTO> choices = multipleChoicesQuestionDTO.getChoices();
            if (choices != null) {
                builder.getChoices().clear();
                for (ChoiceDTO choiceDTO : choices) {
                    builder.addChoice(choiceDTO.toChoice());
                }
            }
        }
        
        linkHandler.handle(commonQuestionDTO, builder, Optional.ofNullable(multipleChoicesQuestion));
        
        Long authorQQ = commonQuestionDTO.getAuthorQQ();
        if (authorQQ != null) {//FIXME
            builder.setAuthor(UserService.singletonInstance.findByQQNumber(authorQQ).orElse(null));
        }
        
        List<ImageDTO> imageDTOs = commonQuestionDTO.getImages();
        if (imageDTOs != null) {
            builder.getImageBase64Strings().clear();
            for (ImageDTO imageDTO : imageDTOs) {
                builder.addBase64Image(imageDTO.getName(), imageDTO.getUrl());
            }
        }
        return builder.build();
    }
    
    public static MultipleChoicesQuestion createMultipleChoicesQuestion(MultipleChoicesQuestionDTO questionDataMap) {
        return create(questionDataMap, (multipleChoicesQuestionDTO, builder, previousQuestion) -> {
            List<String> partitionIds = multipleChoicesQuestionDTO.getPartitionIds();
            if (partitionIds != null) {
                builder.usePartitionLinks(linkWrapper -> {
                    final Set<Partition> targets = linkWrapper.getTargets();
                    targets.clear();
                    for (String partitionId : partitionIds) {
                        targets.add(Partition.ofId(partitionId));
                    }
                    previousQuestion.ifPresent(question -> {
                        final ToPartitionsLink linkWrapper1 = (ToPartitionsLink) question.getLinkWrapper();
                        final Set<Partition> partitions = new HashSet<>(linkWrapper1.getTargets());
                        partitions.removeAll(targets);
                        partitions.forEach(partition -> {
                            partition.getQuestionLinks().remove(linkWrapper1);
                            partition.getEnabledQuestionsSet().remove(question);
                        });
                    });
                });
            }
        });
    }
    
    protected static MultipleChoicesQuestion createSubMultipleChoicesQuestion(MultipleChoicesQuestionDTO multipleChoicesQuestionDTO, QuestionGroup questionGroup) {
        return create(multipleChoicesQuestionDTO, (questionDataMap1, builder1, previousQuestion) -> builder1.useQuestionGroupLinks(linkWrapper -> {
            linkWrapper.setTarget(questionGroup);
        }));
    }
    
    public static QuestionGroup createQuestionGroup(QuestionGroupDTO questionGroupDTO) {
        String id = questionGroupDTO.getId();
        Optional<Question> questionOptional = QuestionService.singletonInstance.findById(id);
        QuestionGroup.Builder builder;
        if (questionOptional.isPresent() && questionOptional.get() instanceof QuestionGroup previousQuestionGroup) {
            builder = QuestionGroup.Builder.from(previousQuestionGroup);
        } else {
            builder = new QuestionGroup.Builder();
        }
        builder.setId(questionGroupDTO.getId());
        
        String content = questionGroupDTO.getContent();
        if (content != null) {
            builder.setContent(content);
        }
        
        List<String> partitionIds = questionGroupDTO.getPartitionIds();
        if (partitionIds != null) {
            builder.getPartitions().clear();
            for (String partitionId : partitionIds) {
                builder.addPartition(Partition.ofId(partitionId));
            }
        }
        
        Long authorQQ = questionGroupDTO.getAuthorQQ();
        if (authorQQ != null) {
            builder.setAuthor(UserService.singletonInstance.findByQQNumber(authorQQ).orElse(null));
        }
        
        Boolean enabled = questionGroupDTO.getEnabled();
        if (enabled != null) {
            builder.setEnabled(enabled);
        }
        
        List<CommonQuestionDTO> questions = questionGroupDTO.getQuestions();
        if (questions != null) {
            builder.getQuestions().clear();
        }
        List<ImageDTO> imageDTOS = questionGroupDTO.getImages();
        if (imageDTOS != null) {
            builder.getImageBase64Strings().clear();
            for (ImageDTO imageDTO : imageDTOS) {
                String key = imageDTO.getName();
                String value = imageDTO.getUrl();
                builder.addBase64Image(key, value);
            }
        }
        QuestionGroup questionGroup = builder.build();
        if (questions != null) {
            for (CommonQuestionDTO questionInfoObj : questions) {
                if (questionInfoObj instanceof MultipleChoicesQuestionDTO multipleChoicesQuestionDTO) {
                    MultipleChoicesQuestion multipleChoicesQuestion = createSubMultipleChoicesQuestion(multipleChoicesQuestionDTO, questionGroup);
                    questionGroup.addQuestion(multipleChoicesQuestion);
                }
            }
        }
        return questionGroup;
    }
    
    @FunctionalInterface
    private interface LinkHandler<T> {
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        void handle(T commonQuestionDTO, MultipleChoicesQuestion.Builder builder, Optional<MultipleChoicesQuestion> previousQuestion);
    }
}