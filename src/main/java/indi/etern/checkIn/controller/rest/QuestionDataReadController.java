package indi.etern.checkIn.controller.rest;

import com.google.gson.Gson;
import indi.etern.checkIn.entities.question.interfaces.ImagesWith;
import indi.etern.checkIn.entities.question.interfaces.MultiPartitionableQuestion;
import indi.etern.checkIn.service.dao.MultiPartitionableQuestionService;
import indi.etern.checkIn.service.dao.PartitionService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping(path = "question/")
public class QuestionDataReadController {
    @Autowired
    MultiPartitionableQuestionService multiPartitionableQuestionService;
    @Autowired
    PartitionService partitionService;
    
    @GetMapping(path = "image/count/{questionMD5}")
    public String getImageQuantity(@PathVariable String questionMD5) {
        MultiPartitionableQuestion question = multiPartitionableQuestionService.getByMD5(questionMD5);
        if (question instanceof ImagesWith questionWithImages) {
            return String.valueOf(questionWithImages.getImagePathStrings().size());//TODO
        } else {
            return "0";
        }
    }
    
    @GetMapping(path = "image/{questionMD5}/", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getAllImageJsonData(@PathVariable String questionMD5) {
        MultiPartitionableQuestion question = multiPartitionableQuestionService.getByMD5(questionMD5);
        if (question instanceof ImagesWith questionWithImages) {
            Gson gson = new Gson();
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("count", questionWithImages.getImagePathStrings().size());
            
            List<String> imageNames = new ArrayList<>();
            dataMap.put("names", imageNames);
            List<Integer> sizes = new ArrayList<>();
            dataMap.put("sizes", sizes);
            
            Map<String, String> imageBase64Map = new HashMap<>();
            int index = 0;
            for (String imagePathString : questionWithImages.getImagePathStrings()) {
                final String imageName = imagePathString.substring(imagePathString.lastIndexOf('/') + 1);
                imageNames.add(imageName);
                byte[] bytes;
                try {
                    final InputStream inputStream = getImageInputStreamOf(questionWithImages, index);
                    bytes = IOUtils.toByteArray(inputStream);
                    sizes.add(bytes.length);
                    inputStream.close();
                    String type;
                    if (imageName.endsWith(".jpg") || imageName.endsWith(".jpeg"))
                        type = "data:image/jpeg;base64,";
                    else if (imageName.endsWith(".png"))
                        type = "data:image/png;base64,";
                    else if (imageName.endsWith(".gif"))
                        type = "data:image/gif;base64,";
                    else
                        type = "data:image;base64,";
                    imageBase64Map.put(imageName, type + Base64.getEncoder().encodeToString(bytes));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                index++;
            }
            dataMap.put("imagesBase64", imageBase64Map);
            return gson.toJson(dataMap);
        } else {
            return "{\"count\": 0}";
        }
    }
    
    @GetMapping(path = "image/{questionMD5}/{imageIndex}")
    public void getImage(HttpServletResponse httpServletResponse, @PathVariable String questionMD5, @PathVariable int imageIndex) {
        MultiPartitionableQuestion question = multiPartitionableQuestionService.getByMD5(questionMD5);
        if (question != null) {
            //TODO 后缀
            httpServletResponse.setContentType("image/png");
            try {
                IOUtils.copy(getImageInputStreamOf((ImagesWith) question, imageIndex), httpServletResponse.getOutputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private InputStream getImageInputStreamOf(ImagesWith questionWithImage, int imageIndex) {
        try {
            return new FileInputStream(questionWithImage.getImagePathStrings().get(imageIndex));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
