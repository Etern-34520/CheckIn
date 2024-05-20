package indi.etern.checkIn.entities.question.interfaces;

import java.util.Map;

public interface ImagesWith {
    Map<String,String> getImageBase64Strings();

    void setImageBase64Strings(Map<String, String> imageBase64Strings);
}
