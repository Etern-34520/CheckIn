package indi.etern.checkIn.action.partition;

import indi.etern.checkIn.action.BaseAction;
import indi.etern.checkIn.action.NullInput;
import indi.etern.checkIn.action.interfaces.Action;
import indi.etern.checkIn.action.interfaces.ExecuteContext;
import indi.etern.checkIn.action.interfaces.OutputData;
import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.service.dao.PartitionService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Action("getPartitions")
public class GetPartitionsAction extends BaseAction<NullInput, GetPartitionsAction.Output> {
    public record Output(List<Partition> partitions) implements OutputData {
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
        final List<Partition> partitions = partitionService.findAll();
        context.resolve(new Output(partitions));
    }
}
