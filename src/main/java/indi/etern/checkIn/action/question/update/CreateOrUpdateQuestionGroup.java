package indi.etern.checkIn.action.question.update;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.utils.QuestionCreateUtils;
import indi.etern.checkIn.entities.linkUtils.impl.QuestionLinkImpl;
import indi.etern.checkIn.entities.question.impl.Question;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroup;
import indi.etern.checkIn.service.dao.QuestionService;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Action(value = "createOrUpdateQuestionGroup",exposed = false)
public class CreateOrUpdateQuestionGroup extends BaseAction<Question, Map<String,Object>> {
    Map<String,Object> data;
    QuestionGroup questionGroup;
    final QuestionService questionService;

    public CreateOrUpdateQuestionGroup(QuestionService questionService) {
        this.questionService = questionService;
    }

    @Override
    public String requiredPermissionName() {
        Set<String> requiredPermissionNames = new HashSet<>();
        Optional<Question> previousQuestion = questionService.findById(questionGroup.getId());
        if (previousQuestion.isPresent() && data.containsKey("authorQQ")) {
            requiredPermissionNames.add("change question group author");
        }
        if (previousQuestion.isEmpty() ||
                previousQuestion.get().getAuthor() != null &&
                        previousQuestion.get().getAuthor().equals(getCurrentUser())) {
            requiredPermissionNames.add("create and edit owns question groups");
        } else {
            requiredPermissionNames.add("edit others question groups");
        }
        if (data.containsKey("enabled")) {
            requiredPermissionNames.add("enable and disable question groups");
        }
        return String.join(",", requiredPermissionNames);
    }

    @Override
    protected Optional<Question> doAction() throws Exception {
        questionService.saveAll(questionGroup.getQuestionLinks().stream().map(QuestionLinkImpl::getSource).toList());
        questionService.save(questionGroup);
        return Optional.of(questionGroup);
    }

    @Override
    public void initData(Map<String,Object> dataMap) {
        data = dataMap;
        questionGroup = QuestionCreateUtils.createQuestionGroup(data);
    }
}