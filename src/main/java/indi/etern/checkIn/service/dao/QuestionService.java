package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroup;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.repositories.QuestionRepository;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Service
public class QuestionService {
    public static QuestionService singletonInstance;
    final TransactionTemplate transactionTemplate;
    final PartitionService partitionService;
    @Resource
    private QuestionRepository questionRepository;

    protected QuestionService(TransactionTemplate transactionTemplate, PartitionService partitionService) {
        singletonInstance = this;
        this.transactionTemplate = transactionTemplate;
        this.partitionService = partitionService;
    }
    
    @CachePut(value = "question",key = "#question.id")
    public Question save(Question question) {
        return questionRepository.save(question);
    }

    @Cacheable(value = "question",key = "#id")
    public Optional<Question> findById(String id) {
        return questionRepository.findById(id);
    }
    
    @CacheEvict(value = "question",key = "#question.id")
    public void delete(Question question) {
        if (question instanceof QuestionGroup questionGroup) {
            questionGroup.getQuestionLinks().forEach(link -> {
                Question subQuestion = link.getSource();
                questionRepository.delete(subQuestion);
            });
        }
        questionRepository.delete(question);
    }
    
    public List<Question> findAllById(List<String> questionIds) {
        return questionRepository.findAllById(questionIds);
    }
    
    @CacheEvict(value = "question",allEntries = true)
    public void saveAll(Collection<Question> questions) {
        questionRepository.saveAll(questions);
    }
    
    public List<Question> findAllByAuthor(User author) {
        return questionRepository.findAllByAuthor(author)
                .stream().parallel().filter((question) -> question.getLinkWrapper() instanceof ToPartitionsLink)
                .toList();
    }
    
    @CacheEvict(value = "question",allEntries = true)
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
    
    public List<Question> findLatestModifiedQuestions() {
        return questionRepository.findAllByLastModifiedTimeBeforeAndLinkWrapper_LinkType(
                LocalDateTime.now(), QuestionLinkImpl.LinkType.PARTITION_LINK, Sort.by(Sort.Direction.DESC,"lastModifiedTime"), Limit.of(20));
    }
}

