package indi.etern.checkIn.service.exam;

import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.QuestionGroup;
import indi.etern.checkIn.service.exam.specialPartitionLimit.SpecialPartitionLimit;
import indi.etern.checkIn.throwable.exam.generate.PartitionEmptiedException;
import indi.etern.checkIn.throwable.exam.generate.PartitionMaxLimitReachedException;
import indi.etern.checkIn.throwable.exam.generate.UnachievableLimitException;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class PartitionQuestionSampler {
    private final Random random;
    @Getter
    private final Set<Question> partitionAllEnabledQuestions;
    @Getter
    Partition partition;
    final List<Question> availableQuestions;
    @Getter
    int availableCount;
    int unavailableCount = 0;
    final List<Question> sampledQuestions;
    int sampledCount = 0;
    @Setter
    @Getter
    SpecialPartitionLimit specialPartitionLimit;
//    private final int allEnabledQuestionCount;
    
    public PartitionQuestionSampler(Partition partition, Random random) {
        this.partition = partition;
        this.random = random;
        partitionAllEnabledQuestions = partition.getEnabledQuestionsSet();
        availableQuestions = new ArrayList<>(partitionAllEnabledQuestions);
        availableCount = availableQuestions.stream()
                .map(question -> question instanceof QuestionGroup questionGroup ? questionGroup.getQuestionLinks().size():1)
                .reduce(0, Integer::sum);
        sampledQuestions = new ArrayList<>();
        //TODO improve throughput 68745ms
/*
        allEnabledQuestionCount = availableQuestions.stream()
                .mapToInt(question -> question instanceof QuestionGroup questionGroup ? questionGroup.getQuestionLinks().size() : 1)
                .sum();
*/
    }
    
    private int count(Question question) {
        return question instanceof QuestionGroup questionGroup ? questionGroup.getQuestionLinks().size() : 1;
    }

    private Question sample(Question question, int count) {
        sampledCount += count;
        unavailableCount += count;
        availableCount -= count;
        //TODO improve throughput 9981ms
        availableQuestions.remove(question);
        sampledQuestions.add(question);
        return question;
    }
    
    private void invalid(Question question, int count) {
        unavailableCount += count;
        availableCount -= count;
        availableQuestions.remove(question);
    }
    
    public void invalidAll(Collection<Question> questions) {
        int initCount = availableQuestions.size();
        //TODO improve throughput 3021ms
        availableQuestions.removeAll(questions);
        int removeCount = initCount - availableQuestions.size();
        unavailableCount += removeCount;
        availableCount -= removeCount;
    }

    public List<Question> initLeastQuestions() {
        if (specialPartitionLimit == null) return sampledQuestions;
//        int min = specialPartitionLimit.isMinLimitEnabled() ? Math.min(specialPartitionLimit.getMinLimit(), allEnabledQuestionCount) : 0;
        try {
            while (!specialPartitionLimit.checkMin(sampledCount)) {
                sampleOneCountLessThanOrEqual(null);
            }
        } catch (PartitionEmptiedException emptiedException) {
            throw new UnachievableLimitException();
        }
        return sampledQuestions;
    }
    
    public Question sampleOneCountLessThanOrEqual(@Nullable Integer limit) {
        if (limit != null && limit <= 0) throw new IllegalArgumentException("limit must greater than 0");
        while (true) {
            final int bound = availableQuestions.size() - 1;
            if (bound < 0) throw new PartitionEmptiedException();
            final int randomIndex = bound > 0 ? random.nextInt(0, bound) : 0;
            Question question = availableQuestions.get(randomIndex);
            int count = count(question);
            if (specialPartitionLimit != null && !specialPartitionLimit.checkMax(sampledCount + count)) {
                invalid(question, count);
                if (availableQuestions.isEmpty()) {
                    throw new PartitionMaxLimitReachedException();
                }
            } else if (limit == null || count <= limit) {
                //TODO improve throughput 10177ms
                return sample(question, count);
            } else {
                invalid(question, count);
            }
        }
    }
    
    public String toString() {
        return "PartitionQuestionSampler(" + partition.toString() + ")";
    }
}