package indi.etern.checkIn.service.exam;

import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroup;
import indi.etern.checkIn.service.exam.specialPartitionLimit.SpecialPartitionLimit;
import indi.etern.checkIn.throwable.exam.generate.PartitionEmptiedException;
import indi.etern.checkIn.throwable.exam.generate.PartitionMaxLimitReachedException;
import indi.etern.checkIn.throwable.exam.generate.UnachievableLimitException;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class PartitionQuestionDrawer {
    private final Random random;
    @Getter
    private final Set<Question> partitionAllEnabledQuestions;
    @Getter
    Partition partition;
    List<Question> availableQuestions;
    @Getter
    int availableCount;
    int unavailableCount = 0;
    List<Question> drewQuestions;
    int drewCount = 0;
    @Setter
    @Getter
    SpecialPartitionLimit specialPartitionLimit;
    private final int allEnabledQuestionCount;
    
    public PartitionQuestionDrawer(Partition partition, Random random) {
        this.partition = partition;
        this.random = random;
        partitionAllEnabledQuestions = partition.getEnabledQuestionsSet();
        availableQuestions = new ArrayList<>(partitionAllEnabledQuestions);
        availableCount = availableQuestions.size();
        drewQuestions = new ArrayList<>();
        //TODO improve throughput 68745ms
        allEnabledQuestionCount = availableQuestions.stream()
                .mapToInt(question -> question instanceof QuestionGroup questionGroup ? questionGroup.getQuestionLinks().size() : 1)
                .sum();
    }
    
    private int count(Question question) {
        return question instanceof QuestionGroup questionGroup ? questionGroup.getQuestionLinks().size() : 1;
    }
    
    private Question draw(Question question, int count) {
        drewCount += count;
        unavailableCount += count;
        availableCount -= count;
        //TODO improve throughput 9981ms
        availableQuestions.remove(question);
        drewQuestions.add(question);
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
        if (specialPartitionLimit == null) return drewQuestions;
        int min = Math.min(specialPartitionLimit.getMinLimit(), allEnabledQuestionCount);
        try {
            while (unavailableCount < min && drewCount < min) {
                drawOneCountLessThanOrEqual(min - drewCount);
            }
        } catch (PartitionEmptiedException emptiedException) {
            throw new UnachievableLimitException();
        }
        return drewQuestions;
    }
    
    public Question drawOneCountLessThanOrEqual(int limit) {
        if (limit <= 0) throw new IllegalArgumentException("limit must greater than 0");
        while (true) {
            if (specialPartitionLimit != null && !specialPartitionLimit.checkMax(drewCount)) {
                throw new PartitionMaxLimitReachedException();
            }
            final int bound = availableQuestions.size() - 1;
            if (bound < 0) throw new PartitionEmptiedException();
            final int randomIndex = bound > 0 ? random.nextInt(0, bound) : 0;
            Question question = availableQuestions.get(randomIndex);
            int count = count(question);
            if (count <= limit) {
                //TODO improve throughput 10177ms
                return draw(question, count);
            } else {
                invalid(question, count);
            }
        }
    }
    
    public String toString() {
        return "PartitionQuestionDrawer(" + partition.toString() + ")";
    }
}