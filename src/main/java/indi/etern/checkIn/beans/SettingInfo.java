package indi.etern.checkIn.beans;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SettingInfo {
/*
    private final SettingService settingService;
    private final PartitionInfo partitionInfo;
    private int partitionCountMin;
    private int partitionCountMax;
    private int passScore;
    private String passMessage;
    private String notPassMessage;
    private String enableAutoCreateUser;
    private String robotToken;
    private String defaultPartitionName;

    public SettingInfo(SettingService settingService, PartitionInfo partitionInfo) {
        this.settingService = settingService;
        this.partitionInfo = partitionInfo;
    }
    
    public String getTitle() {
        return settingService.get("exam.title");
    }
    
    public void setTitle(String title) {
        settingService.set("exam.title", title);
    }
    
    public String getDescription() {
        return settingService.get("exam.description");
    }
    
    public void setDescription(String description) {
        settingService.set("exam.description", description);
    }
    
    public Integer getQuestionCount() {
        final String s = settingService.get("exam.questionCount");
        if (s != null) {
            return Integer.parseInt(s);
        } else {
            return 0;
        }
    }
    
    public void setQuestionCount(Integer questionCount) {
        settingService.set("exam.questionCount", questionCount.toString());
    }
    
    public Integer getPartitionCountMin() {
        if (settingService.get("exam.partitionCountMin").isEmpty()) settingService.set("exam.partitionCountMin","1");
        partitionCountMin = Integer.parseInt(settingService.get("exam.partitionCountMin"));
        return Math.min(partitionCountMin, partitionInfo.getPartitionsNotEmpty().size());
    }
    
    public Integer getPartitionCountMax() {
        if (settingService.get("exam.partitionCountMax").isEmpty()) settingService.set("exam.partitionCountMax","1");
        partitionCountMax = Integer.parseInt(settingService.get("exam.partitionCountMax"));
        return Math.min(partitionCountMax, partitionInfo.getPartitionsNotEmpty().size());
    }
    
    public Integer getPassScore() {
        passScore = Integer.parseInt(settingService.get("checking.passScore"));
        if (passScore < 0) {
            passScore = 0;
        } else if (passScore > 100) {
            passScore = 100;
        }
        return passScore;
    }
    
    public String getPassMessage() {
        passMessage = settingService.get("checking.passMessage");
        return passMessage;
    }
    
    public String getNotPassMessage() {
        notPassMessage = settingService.get("checking.notPassMessage");
        return notPassMessage;
    }

    public String getEnableAutoCreateUser() {
        enableAutoCreateUser = settingService.get("checking.enableAutoCreateUser");
        return enableAutoCreateUser;
    }
    
    public String getRobotToken() {
        robotToken = settingService.get("other.robotToken");
        if (robotToken.isEmpty()) robotToken = settingService.set("other.robotToken", UUID.randomUUID().toString());
        return robotToken;
    }

    public String getDefaultPartitionName() {
        defaultPartitionName = settingService.get("other.defaultPartitionName");
        if (defaultPartitionName.isEmpty()) defaultPartitionName = settingService.set("other.defaultPartitionName", "undefined");
        return defaultPartitionName;
    }
*/
}