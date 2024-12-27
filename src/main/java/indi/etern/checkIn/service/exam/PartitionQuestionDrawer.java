package indi.etern.checkIn.service.exam;

import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroup;
import indi.etern.checkIn.service.exam.specialPartitionLimit.SpecialPartitionLimit;
import indi.etern.checkIn.service.exam.throwable.PartitionEmptiedException;
import indi.etern.checkIn.service.exam.throwable.PartitionMaxLimitReachedException;
import indi.etern.checkIn.utils.QuestionRealCountCounter;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class PartitionQuestionDrawer {
    private final Random random;
    @Getter
    Partition partition;
    List<Question> availableQuestions;
    List<Question> unavailableQuestions;
    @Getter
    int unavailableCount = 0;
    @Getter
    List<Question> drewQuestions;
    @Getter
    int drewCount = 0;
    @Setter
    @Getter
    SpecialPartitionLimit specialPartitionLimit;
    
    public PartitionQuestionDrawer(Partition partition, Random random) {
        this.partition = partition;
        this.random = random;
        availableQuestions = partition.getEnabledQuestionsList();
        unavailableQuestions = new ArrayList<>();
        drewQuestions = new ArrayList<>();
    }
    
    private int count(Question question) {
        return question instanceof QuestionGroup questionGroup ? questionGroup.getQuestionLinks().size() : 1;
    }
    
    private Question draw(Question question,int count) {
        drewCount += count;
        unavailableCount += count;
        availableQuestions.remove(question);
        unavailableQuestions.add(question);
        drewQuestions.add(question);
        return question;
    }
    
    private Question draw(Question question) {
        return draw(question,count(question));
    }
    
    private Question invalid(Question question, int count) {
        unavailableCount += count;
        availableQuestions.remove(question);
        unavailableQuestions.add(question);
        return question;
    }
    
    private Question invalid(Question question) {
        return invalid(question,count(question));
    }
    
    public List<Question> initLeastQuestions() {
        if (specialPartitionLimit == null) return drewQuestions;
        int min = Math.min(specialPartitionLimit.getMinLimit(),partition.getEnabledQuestionCount());
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
            if (specialPartitionLimit != null) {
                if (!specialPartitionLimit.checkMax(drewCount)) {
                    throw new PartitionMaxLimitReachedException();
                }
            }
            final int bound = availableQuestions.size() - 1;
            if (bound < 0) throw new PartitionEmptiedException();
            final int randomIndex = bound > 0 ? random.nextInt(0, bound) : 0;
            Question question = availableQuestions.get(randomIndex);
            int count = count(question);
            if (count <= limit) {
                return draw(question,count);
            } else {
                invalid(question,count);
            }
        }
    }
    
    public List<Question> invalidIdentical(Collection<Question> questions) {
        final List<Question> list = availableQuestions.stream().filter(questions::contains).toList();
        list.forEach(this::invalid);
        return list;
    }
    
    public int getAvailableCount() {
        return QuestionRealCountCounter.count(availableQuestions);
    }
    
    public String toString() {
        return "PartitionQuestionDrawer(" + partition.toString() + ")";
    }
}