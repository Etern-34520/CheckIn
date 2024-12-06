package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.repositories.QuestionRepository;
import jakarta.annotation.Resource;
import org.apache.commons.io.file.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class QuestionService {
    public static QuestionService singletonInstance;
    final TransactionTemplate transactionTemplate;
    final PartitionService partitionService;
    final Logger logger = LoggerFactory.getLogger(getClass());
    @Resource
    private QuestionRepository questionRepository;

    protected QuestionService(TransactionTemplate transactionTemplate, PartitionService partitionService) {
        singletonInstance = this;
        this.transactionTemplate = transactionTemplate;
        this.partitionService = partitionService;
    }

    public Question save(Question Question) {
        return questionRepository.save(Question);
    }

    public List<Question> findAll() {
        return questionRepository.findAll();
    }

    public Question getById(String id) {
        return questionRepository.findById(id).orElse(null);
    }

    public void saveAndFlush(Question Question) {
        questionRepository.saveAndFlush(Question);
    }

    public Optional<Question> findById(String id) {
        return questionRepository.findById(id);
    }

    public void deleteAll() {
        questionRepository.deleteAll();
        Path path = Paths.get("data/images/");
        try {
            PathUtils.deleteDirectory(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*public void unbindAndDeleteById(String questionID) {
        transactionTemplate.execute((TransactionCallback<Object>) result -> {
            Question Question = getById(questionID);
            Set<Partition> partitions = Question.getPartitions();
            for (Partition partition : partitions) {
                partition.getQuestions().remove(Question);
                partitionService.save(partition);
            }
            return Boolean.TRUE;
        });
        deleteById(questionID);
    }*/

    public void deleteById(String id) {
        transactionTemplate.executeWithoutResult(status -> {
            questionRepository.deleteById(id);
/*
            final Optional<Question> optionalQuestion;
            optionalQuestion = findById(id);
            if (optionalQuestion.isPresent()) {
                Field partitionSetField;
                try {
                    partitionSetField = Question.class.getDeclaredField("partitions");
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
                partitionSetField.setAccessible(true);
                final Question Question;
                Question = optionalQuestion.get();
                Set<Partition> partitions;
                partitions = Question.getPartitions();
                for (Partition partition : partitions) {
                    Set<Question> questions;
                    questions = partition.getQuestions();
                    questions.remove(Question);
                    partitionService.save(partition);
                }
                try {
                    partitionSetField.set(Question, new HashSet<>());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                multiplePartitionableQuestionRepository.save(Question);
//                multiplePartitionableQuestionRepository.flush();
                multiplePartitionableQuestionRepository.deleteById(id);
//                multiplePartitionableQuestionRepository.flush();
            }
            return Boolean.TRUE;
*/
        });
    }

    public void update(Question Question) {
//        multiplePartitionableQuestionRepository.deleteById(Question.getId());
        questionRepository.save(Question);
//        entityManager.merge(Question);
    }

    public List<Question> findAllById(List<String> questionIds) {
        return questionRepository.findAllById(questionIds);
    }

    public long count() {
        return questionRepository.count();
    }

    public Map<String, Question> mapAllById(List<String> questionIds) {
        Map<String, Question> map = new HashMap<>();
        List<Question> questions = findAllById(questionIds);
        for (Question question : questions) {
            map.put(question.getId(), question);
        }
        return map;
    }

    public List<Question> findEditedInLastDays(int dayCount, int count) {
        Pageable pageable = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "lastEditTime"));
        return questionRepository.findAllByLastModifiedTimeAfter(LocalDateTime.now().minusDays(dayCount), pageable);
    }

    public boolean existsById(String questionId) {
        return questionRepository.existsById(questionId);
    }

    public void saveAll(Collection<Question> Questions) {
        questionRepository.saveAll(Questions);
    }

    public void deleteAllById(Collection<String> ids) {
        for (String id : ids) {
            deleteById(id);
        }
    }

    public long countEnabled() {
        return questionRepository.countByEnabledIsTrue();
    }

    public List<Question> enableAllById(Collection<String> ids) {
        List<Question> Questions = new ArrayList<>();
        for (String id : ids) {
            Question Question = questionRepository.findById(id).orElseThrow();
            Question.setEnabled(true);
            questionRepository.save(Question);
            Questions.add(Question);
        }
        return Questions;
    }

    public List<Question> disableAllById(Collection<String> ids) {
        List<Question> Questions = new ArrayList<>();
        for (String id : ids) {
            Question Question = questionRepository.findById(id).orElseThrow();
            Question.setEnabled(false);
            questionRepository.save(Question);
            Questions.add(Question);
        }
        return Questions;
    }

    public List<Question> findAllByAuthor(User author) {
        return questionRepository.findAllByAuthor(author)
                .stream().parallel().filter((question) -> question.getLinkWrapper() instanceof ToPartitionsLink)
                .toList();
    }

    public void flush() {
        questionRepository.flush();
    }

    public List<Question> findAllByUpVotersContains(User user) {
        return questionRepository.findAllByUpVotersContains(user).stream().parallel().filter((question) -> question.getLinkWrapper() instanceof ToPartitionsLink).toList();
    }

    public List<Question> findAllByDownVotersContains(User user) {
        return questionRepository.findAllByDownVotersContains(user).stream().parallel().filter((question) -> question.getLinkWrapper() instanceof ToPartitionsLink).toList();
    }

    public List<Question> findFirstLimitByUser(User user, int limit) {
        return questionRepository.findAllByAuthor(user, PageRequest.of(0, limit))
                .stream().parallel().filter((question) -> question.getLinkWrapper() instanceof ToPartitionsLink)
                .toList();
    }
}

