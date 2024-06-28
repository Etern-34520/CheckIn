package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.linkUtils.impl.ToPartitionLink;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.repositories.PartitionRepository;
import jakarta.annotation.Resource;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class PartitionService {
    public static PartitionService singletonInstance;
    final
    TransactionTemplate transactionTemplate;
    @Resource
    private PartitionRepository partitionRepository;

    protected PartitionService(TransactionTemplate transactionTemplate) {
        singletonInstance = this;
        this.transactionTemplate = transactionTemplate;
    }

//    @CacheEvict(value = "partition", key = "#partition.id")
    public void save(Partition partition) {
        partitionRepository.save(partition);
    }

    public List<Partition> findAll() {
        return partitionRepository.findAll();
    }
//    @CacheEvict(value = "partition", key = "#partition.id")
    public void saveAndFlush(Partition partition) {
        partitionRepository.saveAndFlush(partition);
    }

    public Optional<Partition> findByName(String name) {
        return partitionRepository.findByName(name);
/*
        Partition examplePartition = Partition.getExample(name);
        ExampleMatcher exampleMatcher = ExampleMatcher
                .matching()
                .withIgnorePaths("id");
        Example<Partition> example = Example.of(examplePartition, exampleMatcher);
        return partitionRepository.findOne(example);
*/
    }

//    @CacheEvict(value = "partition", allEntries = true)
    public void deleteAll() {
        partitionRepository.deleteAll();
    }

    public void deleteByName(String partitionName) {
        Optional<Partition> optionalPartition = findByName(partitionName);
        optionalPartition.ifPresent(partition -> partitionRepository.deleteById(partition.getId()));
    }

//    @CacheEvict(value = "partition", key = "#partition.id")
    public void delete(Partition partition) {
        partitionRepository.delete(partition);
    }

    public boolean existsById(int id) {
        return partitionRepository.existsById(id);
    }

//    @Cacheable(value = "partition", key = "#id")
    public Optional<Partition> findById(int id) {
        return partitionRepository.findById(id);
    }

//    @CacheEvict(value = "partition", key = "#partition.id")
    public void addQuestionOf(Partition partition, Question multipleQuestion) {
        transactionTemplate.execute((callback) -> {
            final Optional<Partition> partitionOptional = partitionRepository.findById(partition.getId());
            Partition partition1 = partitionOptional.orElse(partition);
            partition1.getQuestionLinks().add((ToPartitionLink) multipleQuestion.getLinkWrapper());
            partitionRepository.save(partition1);
            return Boolean.TRUE;
        });
    }

    public List<Question> generateExam(List<Integer> partitionIds, Random random) throws Exception {
        List<Question> multiPartitionableQuestions = new ArrayList<>();
        int questionCount = Integer.parseInt(SettingService.singletonInstance.get("exam.questionCount"));
        int partitionCountMin = Integer.parseInt(SettingService.singletonInstance.get("exam.partitionCountMin"));
        int partitionCountMax = Integer.parseInt(SettingService.singletonInstance.get("exam.partitionCountMax"));
        if (partitionCountMin > partitionIds.size()) {
            throw new BadRequestException("partition count cannot smaller than min count(" + partitionCountMin + ")");
        } else if (partitionIds.size() > partitionCountMax) {
            throw new BadRequestException("partition count cannot greater than max count(" + partitionCountMax + ")");
        }
        /*transactionTemplate.execute((callback) -> {
            partitionIds.forEach(partitionName -> {
                final Partition partition = findByName(partitionName);
                final List<Question> questions = new ArrayList<>();
                Random random = new Random();
                int quantity = Math.min(questionCount+1, partition.getQuestions().size());
                while (questions.size() < quantity) {
                    final Question question = partition.getQuestions().stream().toList().get(random.nextInt(partition.getQuestions().size()));
                    if (!questions.contains(question)) {
                        questions.add(question);
                    }
                }
                multiPartitionableQuestions.addAll(questions);
            });
            return Boolean.TRUE;
        });*/
//        Random random = new Random();
        AtomicReference<Optional<Exception>> exception = new AtomicReference<>();
        transactionTemplate.execute((callback) -> {
            try {
                List<Partition> partitions = new ArrayList<>();
                AtomicInteger questionQuantity = new AtomicInteger();
                partitionIds.forEach(partitionId -> {
                    final Optional<Partition> partitionOptional = findById(partitionId);
                    if (partitionOptional.isEmpty()) {
                        exception.set(Optional.of(new BadRequestException("partition with Id " + partitionId + " not found")));
                        return;
                    }
                    Partition partition = partitionOptional.get();
                    partitions.add(partition);
                    questionQuantity.addAndGet(partition.getEnabledQuestions().size());
                });
                while (multiPartitionableQuestions.size() < Math.min(questionCount, questionQuantity.get())) {
                    Partition partition = partitions.get(random.nextInt(partitions.size()));
                    final Set<Question> enabledQuestions = partition.getEnabledQuestions();
                    final int partitionQuestionCount = enabledQuestions.size();
                    if (partitionQuestionCount == 0) {
                        continue;
                    }
                    List<Question> list = enabledQuestions.stream().toList();
                    final Question question = list.get(random.nextInt(partitionQuestionCount));
                    if (!multiPartitionableQuestions.contains(question)) {
                        multiPartitionableQuestions.add(question);
                    }
                }
                return Boolean.TRUE;
            } catch (Exception e) {
                exception.set(Optional.of(e));
                return Boolean.FALSE;
            }
        });
        if (exception.get() != null && exception.get().isPresent()) {
            throw exception.get().get();
        }
        return multiPartitionableQuestions;
    }

    public List<Partition> findAllById(Collection<Integer> partitionId) {
        return partitionRepository.findAllById(partitionId);
    }

    public boolean existsByName(String partitionName) {
        return partitionRepository.existsByName(partitionName);
    }
}
