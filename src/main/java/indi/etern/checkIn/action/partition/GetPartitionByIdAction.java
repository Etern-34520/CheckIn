package indi.etern.checkIn.action.partition;

import indi.etern.checkIn.action.TransactionalAction;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.service.dao.PartitionService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Action("getPartitionById")
public class GetPartitionByIdAction extends TransactionalAction {
    private final PartitionService partitionService;
    private Integer partitionId;
    
    public GetPartitionByIdAction(PartitionService partitionService) {
        this.partitionService = partitionService;
    }
    
    @Override
    public String requiredPermissionName() {
        return "";
    }
    
    @Override
    protected Optional<LinkedHashMap<String, Object>> doAction() throws Exception {
        Optional<Partition> optionalPartition = partitionService.findById(partitionId);
        if (optionalPartition.isPresent()) {
            Partition partition = optionalPartition.get();
            LinkedHashMap<String, Object> result = new LinkedHashMap<>();
            result.put("partition", partition.toInfoMap());
            return Optional.of(result);
        } else {
            return getOptionalErrorMap("Partition not found");
        }
    }
    
    @Override
    public void initData(Map<String, Object> dataMap) {
        partitionId = ((Number) dataMap.get("id")).intValue();
    }
}
