package indi.etern.checkIn.action.partition;

import indi.etern.checkIn.action.BaseAction1;
import indi.etern.checkIn.action.NullInput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.service.dao.PartitionService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Action("getPartitions")
public class GetPartitionsAction extends BaseAction1<NullInput, GetPartitionsAction.Output> {
    public record Output(List<Map<String, Object>> partitionDataList) implements OutputData {
        @Override
        public Result result() {
            return Result.SUCCESS;
        }
    }
    
    private final PartitionService partitionService;
    
    public GetPartitionsAction(PartitionService partitionService) {
        this.partitionService = partitionService;
    }
    
    @Override
    @Transactional
    public void execute(ExecuteContext<NullInput, Output> context) {
        final List<Map<String, Object>> partitionDataList = partitionService.findAll().stream().map(Partition::toInfoMap).toList();
        context.resolve(new Output(partitionDataList));
    }
}
