package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroup;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.repositories.QuestionRepository;
import indi.etern.checkIn.repositories.ToPartitionLinkRepository;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


@Service
public class QuestionService {
    public static QuestionService singletonInstance;
    final TransactionTemplate transactionTemplate;
    final PartitionService partitionService;
    final Logger logger = LoggerFactory.getLogger(getClass());
    @Resource
    private QuestionRepository questionRepository;
    @Resource
    private ToPartitionLinkRepository partitionLinkRepository;

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

    public Optional<Question> findById(String id) {
        return questionRepository.findById(id);
    }
    
    public Question deleteById(String id) {
        AtomicReference<Question> question = new AtomicReference<>();
        transactionTemplate.executeWithoutResult(status -> {
            Question question1 = questionRepository.findById(id).orElseThrow();
            delete(question1);
            question.set(question1);
        });
        return question.get();
    }
    
    public void delete(Question question) {
        if (question instanceof QuestionGroup questionGroup) {
            questionGroup.getQuestionLinks().forEach(link -> {
                Question subQuestion = link.getSource();
                questionRepository.delete(subQuestion);
            });
        }
        questionRepository.delete(question);
    }
    
    public void update(Question Question) {
        questionRepository.save(Question);
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
    
    public List<Question> findAllHasPartition() {
        return partitionLinkRepository.findAll().stream().map(QuestionLinkImpl::getSource).filter(Question::isEnabled).toList();
    }
    
    public List<Question> findLatestModifiedQuestions() {
        return questionRepository.findAllByLastModifiedTimeBeforeAndLinkWrapper_LinkType(
                LocalDateTime.now(), QuestionLinkImpl.LinkType.PARTITION_LINK, Sort.by(Sort.Direction.DESC,"lastModifiedTime"), Limit.of(20));
//        return questionRepository.findAll(Question.NOT_SUB_QUESTION_EXAMPLE,LocalDateTime.now(),Sort.by(Sort.Direction.DESC,"lastModifiedTime"), Limit.of(20));
//        return questionRepository.findAll(Question.NOT_SUB_QUESTION_EXAMPLE, PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "lastModifiedTime"))).getContent();
    }
}

