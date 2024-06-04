package indi.etern.checkIn.action.question.update;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.question.update.utils.Utils;
import indi.etern.checkIn.entities.question.impl.group.QuestionGroup;
import indi.etern.checkIn.service.dao.QuestionService;

import java.util.Map;
import java.util.Optional;

@Action(name = "createOrUpdateQuestionGroup")
public class CreateOrUpdateQuestionGroup extends BaseAction<Object, Map<String,Object>> {
    Map<String,Object> data;
    QuestionGroup questionGroup;
    final QuestionService multiPartitionableQuestionService;

    public CreateOrUpdateQuestionGroup(QuestionService multiPartitionableQuestionService) {
        this.multiPartitionableQuestionService = multiPartitionableQuestionService;
    }

    @Override
    public String requiredPermissionName() {
        //TODO
        return "";
    }

    @Override
    protected Optional<Object> doAction() throws Exception {
        multiPartitionableQuestionService.save(questionGroup);
        return Optional.of(questionGroup);
    }

    @Override
    public void initData(Map<String,Object> dataObj) {
        data = dataObj;
        questionGroup = Utils.createQuestionGroup(data);
    }
}
