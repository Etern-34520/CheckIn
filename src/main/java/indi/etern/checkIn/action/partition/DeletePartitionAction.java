package indi.etern.checkIn.action.partition;

import java.util.LinkedHashMap;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.service.dao.PartitionService;

import java.util.Map;
import java.util.Optional;

@Action("deletePartition")
public class DeletePartitionAction extends PartitionMapResultAction {

    private int partitionId;

    @Override
    public String requiredPermissionName() {
        return "delete partition";
    }

    @Override
    protected Optional<LinkedHashMap<String,Object>> doAction() throws Exception {
        Partition partition = PartitionService.singletonInstance.findById(partitionId).orElseThrow();
        if (partition.getQuestionLinks().isEmpty()) {
            PartitionService.singletonInstance.delete(partition);
        } else {
            return getOptionalErrorMap("partition \"" + partition.getName() + "\" is not empty");
        }
        sendDeletePartitionToAll(partition);
        return Optional.empty();
    }

    @Override
    public void initData(Map<String, Object> dataMap) {
        partitionId = (int) dataMap.get("id");
    }
}
