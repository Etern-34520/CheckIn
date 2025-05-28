package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionsLink;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.QuestionGroup;
import indi.etern.checkIn.entities.user.User;
import indi.etern.checkIn.repositories.QuestionRepository;
import indi.etern.checkIn.repositories.QuestionStatisticRepository;
import jakarta.annotation.Resource;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
//@CacheConfig(cacheNames = "question")
public class QuestionService {
    public static QuestionService singletonInstance;
    final PartitionService partitionService;
    @Resource
    private QuestionRepository questionRepository;
    private final Cache partitionCache;
    private final QuestionStatisticRepository questionStatisticRepository;
    
    protected QuestionService(PartitionService partitionService, CacheManager cacheManager, QuestionStatisticRepository questionStatisticRepository) {
        singletonInstance = this;
        this.partitionService = partitionService;
        partitionCache = cacheManager.getCache("partition");
        this.questionStatisticRepository = questionStatisticRepository;
    }
    
    private void evictRelatedPartitionCache(Question question) {
        if (question.getLinkWrapper() instanceof ToPartitionsLink toPartitionsLink) {
            final Set<Partition> partitions = toPartitionsLink.getTargets();
            for (Partition partition : partitions) {
                partitionCache.evict(partition.getId());
            }
        }
    }
    
//    @CachePut(key = "#question.id")
    public Question save(Question question) {
        evictRelatedPartitionCache(question);
        return questionRepository.save(question);
    }
    
//    @Cacheable(key = "#id")
    public Optional<Question> findById(String id) {
        return questionRepository.findById(id);
    }
    
//    @CacheEvict(key = "#question.id")
    public void delete(Question question) {
        if (question.getLinkWrapper() instanceof ToPartitionsLink toPartitionsLink) {
            toPartitionsLink.getTargets().forEach(partition -> {
                partition.getQuestionLinks().remove(toPartitionsLink);
                partition.getEnabledQuestionsSet().remove(question);
            });
        }
        if (question instanceof QuestionGroup questionGroup) {
            questionGroup.getQuestionLinks().forEach(link -> {
                Question subQuestion = link.getSource();
                questionRepository.delete(subQuestion);
            });
        }
        questionStatisticRepository.deleteById(question.getId());
        evictRelatedPartitionCache(question);
        questionRepository.delete(question);
    }
    
    public List<Question> findAllById(List<String> questionIds) {
        return questionRepository.findAllById(questionIds);
    }
    
//    @CacheEvict(allEntries = true)
    public void saveAll(Collection<Question> questions) {
        for (Question question : questions) {
            evictRelatedPartitionCache(question);
        }
        questionRepository.saveAll(questions);
    }
    
    public List<Question> findAllByAuthor(User author) {
        return questionRepository.findAllByAuthor(author)
                .stream().parallel().filter((question) -> question.getLinkWrapper() instanceof ToPartitionsLink)
                .toList();
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

