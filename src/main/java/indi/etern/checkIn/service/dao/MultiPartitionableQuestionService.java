package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.entities.question.interfaces.Partition;
import indi.etern.checkIn.repositories.MultiplePartitionableQuestionRepository;
import jakarta.annotation.Resource;
import org.apache.commons.io.file.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
public class MultiPartitionableQuestionService {
    @Autowired
    TransactionTemplate transactionTemplate;
    @Autowired
    PartitionService partitionService;
    Logger logger = LoggerFactory.getLogger(getClass());
    @Resource
    private MultiplePartitionableQuestionRepository multiplePartitionableQuestionRepository;
    private volatile boolean update = false;
    
    public void save(MultiPartitionableQuestion multiPartitionableQuestion) {
        multiplePartitionableQuestionRepository.save(multiPartitionableQuestion);
    }
    
    public List<MultiPartitionableQuestion> findAll() {
        return multiplePartitionableQuestionRepository.findAll();
    }
    
    public MultiPartitionableQuestion getByMD5(String md5) {
        return multiplePartitionableQuestionRepository.findById(md5).orElse(null);
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
                    partitionService.saveAndFlush(partition);
                }
                try {
                    partitionSetField.set(multiPartitionableQuestion, new HashSet<>());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                multiplePartitionableQuestionRepository.save(multiPartitionableQuestion);
                multiplePartitionableQuestionRepository.flush();
                multiplePartitionableQuestionRepository.deleteById(id);
                multiplePartitionableQuestionRepository.flush();
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
                final String md5 = multiPartitionableQuestion.getMd5();
                if (multiplePartitionableQuestionRepository.existsById(md5)) {
                    MultiPartitionableQuestion oldMultiPartitionableQuestion = multiplePartitionableQuestionRepository.findById(md5).orElseThrow();
                    oldMultiPartitionableQuestion.getPartitions().forEach(partition -> {
                        partition.getQuestions().remove(oldMultiPartitionableQuestion);
                        partitionService.saveAndFlush(partition);
                    });
                    deleteById(md5);
                    multiPartitionableQuestion.getPartitions().forEach(partition -> {
                        partition.getQuestions().remove(multiPartitionableQuestion);
                        partitionService.saveAndFlush(partition);
                    });
                    status.flush();
                }
                return Boolean.TRUE;
            } catch (Exception e) {
                status.setRollbackOnly();
                logger.error(e.getMessage());
                logger.debug("trace:");
                e.printStackTrace();
                update = false;
            }
            return Boolean.FALSE;
        });
        transactionTemplate.execute((TransactionCallback<Object>) status -> {
            try {
                multiplePartitionableQuestionRepository.save(multiPartitionableQuestion);
                status.flush();
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
}
