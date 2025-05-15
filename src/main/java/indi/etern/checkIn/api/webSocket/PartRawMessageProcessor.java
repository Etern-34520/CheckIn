package indi.etern.checkIn.api.webSocket;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PartRawMessageProcessor {
    private Map<String, String> partMap = new LinkedHashMap<>();

    public PartRawMessageProcessor(List<String> partIds) {
        for (String partId : partIds) {
            partMap.put(partId, "");
        }
    }

    public void put(String partId, String messagePart) {
        if (partMap.containsKey(partId)) {
            partMap.put(partId, messagePart);
        } else {
            throw new IllegalArgumentException("partId not found");
        }
    }

    public boolean contains(String partId) {
        return partMap.containsKey(partId);
    }

    public boolean isComplete() {
        return partMap.values().stream().noneMatch(String::isEmpty);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : partMap.entrySet()) {
            sb.append(entry.getValue());
        }
        return sb.toString();
    }
}
