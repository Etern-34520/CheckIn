package indi.etern.checkIn.service.exam;

import indi.etern.checkIn.entities.question.impl.Partition;
import indi.etern.checkIn.entities.setting.SettingItem;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.SettingService;

import java.util.List;

public enum CompletingStrategy {
    NONE {
        public List<Partition> getCompletingPartitions() {
            return List.of();
        }
    },
    REQUIRED {
        public List<Partition> getCompletingPartitions() {
            SettingItem settingItem = SettingService.singletonInstance.getItem("drawing", "requiredPartitions");
            //noinspection unchecked
            List<Integer> partitionIds = (List<Integer>) settingItem.getValue(List.class);
            return PartitionService.singletonInstance.findAllByIds(partitionIds);
        }
    },
    ALL {
        public List<Partition> getCompletingPartitions() {
            return PartitionService.singletonInstance.findAll();
        }
    },
    SELECTED {
        public List<Partition> getCompletingPartitions() {
            SettingItem settingItem = SettingService.singletonInstance.getItem("drawing", "completingPartitions");
            //noinspection unchecked
            List<Integer> partitionIds = (List<Integer>) settingItem.getValue(List.class);
            return PartitionService.singletonInstance.findAllByIds(partitionIds);
        }
    };
    
    abstract public List<Partition> getCompletingPartitions();
}
