package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.question.impl.multipleQuestion.MultipleQuestionBuilder;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.repositories.MultiplePartitionableQuestionRepository;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.apache.commons.io.file.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class MultiPartitionableQuestionService {
    public static MultiPartitionableQuestionService singletonInstance;
    final TransactionTemplate transactionTemplate;
    final PartitionService partitionService;
    final Logger logger = LoggerFactory.getLogger(getClass());
    @Resource
    private MultiplePartitionableQuestionRepository multiplePartitionableQuestionRepository;
    private volatile boolean update = false;

    protected MultiPartitionableQuestionService(TransactionTemplate transactionTemplate, PartitionService partitionService) {
        singletonInstance = this;
        this.transactionTemplate = transactionTemplate;
        this.partitionService = partitionService;
    }

    public static MultiPartitionableQuestion buildQuestionFromRequest(HttpServletRequest httpServletRequest, String id, User author) throws IOException, ServletException {
        Map<String, String[]> map = httpServletRequest.getParameterMap();
        MultipleQuestionBuilder multipleQuestionBuilder = new MultipleQuestionBuilder();
        if (id != null) {
            multipleQuestionBuilder.setId(id);
        } else {
            id = map.get("id")[0];
            if (id != null) {
                multipleQuestionBuilder.setId(id);
            }
        }
/*
        if (author == null) {
            author = UserService.singletonInstance.findByQQNumber(Long.parseLong(httpServletRequest.getParameter("author"))).orElseThrow();
        }
*/

        //content
        multipleQuestionBuilder.setQuestionContent(httpServletRequest.getParameter("questionContent"));

        //image
        int imageIndex = 0;
        while (true) {
            final Part part = httpServletRequest.getPart("image_" + imageIndex);
            if (part == null) {
                break;
            }
            multipleQuestionBuilder.addImage(part);
            imageIndex++;
        }

        //choices&partition
        Map<Integer, Object[]> choiceParamMap = new HashMap<>();
        for (String key : map.keySet()) {
            if (key.matches("-?\\d+(\\.\\d+)?")) {//为数字
                String choiceContent = map.get(key)[0];
                choiceParamMap.put(Integer.parseInt(key), new Object[]{choiceContent, false});
            } else if (key.startsWith("question_partition_") && map.get(key)[0].equals("true")) {
                int partitionId = Integer.parseInt(key.substring(19));
//                    multipleQuestionBuilder.addPartition(partitionName);
                multipleQuestionBuilder.addPartition(partitionId);
            }
        }
        for (String key : map.keySet()) {
            if (key.startsWith("correct")) {
                final String indexString = key.substring(7);
                if (indexString.matches("-?\\d+(\\.\\d+)?")) {
                    int index = Integer.parseInt(indexString);
                    choiceParamMap.get(index)[1] = map.get(key)[0].equals("true");
                }
            }
        }
        List<Choice> choices = new ArrayList<>();
        for (Object[] choiceParam : choiceParamMap.values()) {
            Choice choice = new Choice((String) choiceParam[0], (Boolean) choiceParam[1]);
            choices.add(choice);
        }
        multipleQuestionBuilder.addChoices(choices);

        multipleQuestionBuilder.setAuthor(author);

        multipleQuestionBuilder.setEnable(Boolean.parseBoolean(httpServletRequest.getParameter("enabled")));

        return multipleQuestionBuilder.build();
    }

    public MultiPartitionableQuestion save(MultiPartitionableQuestion multiPartitionableQuestion) {
        return multiplePartitionableQuestionRepository.save(multiPartitionableQuestion);
    }

    public List<MultiPartitionableQuestion> findAll() {
        return multiplePartitionableQuestionRepository.findAll();
    }

    public MultiPartitionableQuestion getById(String id) {
        return multiplePartitionableQuestionRepository.findById(id).orElse(null);
    }

    public void saveAndFlush(MultiPartitionableQuestion multiPartitionableQuestion) {
        multiplePartitionableQuestionRepository.saveAndFlush(multiPartitionableQuestion);
    }

    public Optional<MultiPartitionableQuestion> findById(String id) {
        return multiplePartitionableQuestionRepository.findById(id);
    }

    public void deleteAll() {
        multiplePartitionableQuestionRepository.deleteAll();
        Path path = Paths.get("data/images/");
        try {
            PathUtils.deleteDirectory(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void unbindAndDeleteById(String questionID) {
        transactionTemplate.execute((TransactionCallback<Object>) result -> {
            MultiPartitionableQuestion multiPartitionableQuestion = getById(questionID);
            Set<Partition> partitions = multiPartitionableQuestion.getPartitions();
            for (Partition partition : partitions) {
                partition.getQuestions().remove(multiPartitionableQuestion);
                partitionService.save(partition);
            }
            return Boolean.TRUE;
        });
        deleteById(questionID);
    }

    public void deleteById(String id) {
        transactionTemplate.execute((TransactionCallback<Object>) status -> {
            final Optional<MultiPartitionableQuestion> optionalMultiPartitionableQuestion;
            optionalMultiPartitionableQuestion = findById(id);
            if (optionalMultiPartitionableQuestion.isPresent()) {
                Field partitionSetField;
                try {
                    partitionSetField = MultiPartitionableQuestion.class.getDeclaredField("partitions");
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
                partitionSetField.setAccessible(true);
                final MultiPartitionableQuestion multiPartitionableQuestion;
                multiPartitionableQuestion = optionalMultiPartitionableQuestion.get();
                Set<Partition> partitions;
                partitions = multiPartitionableQuestion.getPartitions();
                for (Partition partition : partitions) {
                    Set<MultiPartitionableQuestion> questions;
                    questions = partition.getQuestions();
                    questions.remove(multiPartitionableQuestion);
                    partitionService.save(partition);
                }
                try {
                    partitionSetField.set(multiPartitionableQuestion, new HashSet<>());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                multiplePartitionableQuestionRepository.save(multiPartitionableQuestion);
//                multiplePartitionableQuestionRepository.flush();
                multiplePartitionableQuestionRepository.deleteById(id);
//                multiplePartitionableQuestionRepository.flush();
                if (!update)
                    try {
                        Path path = Paths.get("data/images/" + id + "/");
                        if (Files.exists(path))
                            PathUtils.deleteDirectory(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
            }
            return Boolean.TRUE;
        });
    }

    public void update(MultiPartitionableQuestion multiPartitionableQuestion) {
        update = true;
        transactionTemplate.execute((TransactionCallback<Object>) status -> {
            try {
//                final String id = multiPartitionableQuestion.getId();
//                if (multiplePartitionableQuestionRepository.existsById(id)) {
//                    MultiPartitionableQuestion oldMultiPartitionableQuestion = multiplePartitionableQuestionRepository.findById(id).orElseThrow();
//                    oldMultiPartitionableQuestion.getPartitions().forEach(partition -> {
//                        partition.getQuestions().remove(oldMultiPartitionableQuestion);
//                        partitionService.save(partition);
//                    });
//                    deleteById(id);
//                    multiPartitionableQuestion.getPartitions().forEach(partition -> {
//                        partition.getQuestions().remove(multiPartitionableQuestion);
//                        partitionService.save(partition);
//                    });
////                    status.flush();
//                }
                multiplePartitionableQuestionRepository.save(multiPartitionableQuestion);
                return Boolean.TRUE;
            } catch (JpaObjectRetrievalFailureException jpaObjectRetrievalFailureException) {
                String errorId = jpaObjectRetrievalFailureException.getMessage().split("with id ")[1];
                logger.error("error id:" + errorId);
                try {
                    multiplePartitionableQuestionRepository.deleteById(errorId);
                } catch (Exception e) {
                    e.printStackTrace();
                    update = false;
                }
                update = false;
            } catch (Exception e) {
                status.setRollbackOnly();
                logger.error(e.getMessage());
                logger.debug("trace:");
                e.printStackTrace();
                update = false;
            }
            update = false;
            return Boolean.FALSE;
        });
    }

    public List<MultiPartitionableQuestion> findAllById(List<String> questionIds) {
        return multiplePartitionableQuestionRepository.findAllById(questionIds);
    }

    public long count() {
        return multiplePartitionableQuestionRepository.count();
    }

    public Map<String, MultiPartitionableQuestion> mapAllById(List<String> questionIds) {
        Map<String, MultiPartitionableQuestion> map = new HashMap<>();
        List<MultiPartitionableQuestion> questions = findAllById(questionIds);
        for (MultiPartitionableQuestion question : questions) {
            map.put(question.getId(), question);
        }
        return map;
    }

    public List<MultiPartitionableQuestion> findEditedInLastDays(int dayCount, int count) {
        Pageable pageable = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "lastEditTime"));
        return multiplePartitionableQuestionRepository.findAllByLastEditTimeAfter(LocalDateTime.now().minusDays(dayCount),pageable);
    }

    public boolean existsById(String questionId) {
        return multiplePartitionableQuestionRepository.existsById(questionId);
    }

    public void saveAll(Collection<MultiPartitionableQuestion> multiPartitionableQuestions) {
        multiplePartitionableQuestionRepository.saveAll(multiPartitionableQuestions);
    }

    public void deleteAllById(Collection<String> ids) {
        for (String id : ids) {
            deleteById(id);
        }
    }

    public long countEnabled() {
        return multiplePartitionableQuestionRepository.countByEnabledIsTrue();
    }

    public List<MultiPartitionableQuestion> enableAllById(Collection<String> ids) {
        List<MultiPartitionableQuestion> multiPartitionableQuestions = new ArrayList<>();
        for (String id : ids) {
            MultiPartitionableQuestion multiPartitionableQuestion = multiplePartitionableQuestionRepository.findById(id).orElseThrow();
            multiPartitionableQuestion.setEnabled(true);
            multiplePartitionableQuestionRepository.save(multiPartitionableQuestion);
            multiPartitionableQuestions.add(multiPartitionableQuestion);
        }
        return multiPartitionableQuestions;
    }

    public List<MultiPartitionableQuestion> disableAllById(Collection<String> ids) {
        List<MultiPartitionableQuestion> multiPartitionableQuestions = new ArrayList<>();
        for (String id : ids) {
            MultiPartitionableQuestion multiPartitionableQuestion = multiplePartitionableQuestionRepository.findById(id).orElseThrow();
            multiPartitionableQuestion.setEnabled(false);
            multiplePartitionableQuestionRepository.save(multiPartitionableQuestion);
            multiPartitionableQuestions.add(multiPartitionableQuestion);
        }
        return multiPartitionableQuestions;
    }

    public List<MultiPartitionableQuestion> findAllByAuthor(User author) {
        return multiplePartitionableQuestionRepository.findAllByAuthor(author);
    }
}

