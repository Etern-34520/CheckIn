package indi.etern.checkIn.controller.rest;

import com.google.gson.Gson;
import indi.etern.checkIn.service.dao.PartitionService;
import indi.etern.checkIn.service.dao.QuestionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "question/")
public class QuestionDataRead {
    final QuestionService multiPartitionableQuestionService;
    final PartitionService partitionService;
    final Gson gson;
    
    public QuestionDataRead(QuestionService multiPartitionableQuestionService, PartitionService partitionService, Gson gson) {
        this.multiPartitionableQuestionService = multiPartitionableQuestionService;
        this.partitionService = partitionService;
        this.gson = gson;
    }
    
    /*@GetMapping(path = "image/count/{questionID}")
    public String getImageQuantity(@PathVariable String questionID) {
        Question question = multiPartitionableQuestionService.getById(questionID);
        if (question instanceof ImagesWith questionWithImages) {
            return String.valueOf(questionWithImages.getImageBase64Strings().size());
        } else {
            return "0";
        }
    }*/

/*
    @GetMapping(path = "/withImages/ofPartition/{partitionId}")
    public List<String> getQuestionWithImageIds(@PathVariable int partitionId) throws BadRequestException {
        Partition partition = partitionService.findById(partitionId).orElseThrow(() -> new BadRequestException("partitions not exist"));
        return partition.getQuestions().stream().map(Question::getId).toList();
    }
*/
    
    /*@GetMapping(path = "image/{questionID}/", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getAllImageJsonData(@PathVariable String questionID) {
        Question question = multiPartitionableQuestionService.getById(questionID);
        if (question instanceof ImagesWith questionWithImages) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("count", questionWithImages.getImageBase64Strings().size());
            
            List<String> imageNames = new ArrayList<>();
            dataMap.put("names", imageNames);
            List<Integer> sizes = new ArrayList<>();
            dataMap.put("sizes", sizes);
            
            Map<String, String> imageBase64Map = new HashMap<>();
            int index = 0;
            for (String imagePathString : questionWithImages.getImageBase64Strings()) {
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
    }*/
    
/*
    @GetMapping(path = "image/{questionID}/{imageIndex}")
    public void getImage(HttpServletResponse httpServletResponse, @PathVariable String questionID, @PathVariable int imageIndex) {
        Question question = multiPartitionableQuestionService.getById(questionID);
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
*/

//    private InputStream getImageInputStreamOf(ImagesWith questionWithImage, int imageIndex) {
//        try {
//            return new FileInputStream(questionWithImage.getImageBase64Strings().get(imageIndex));
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
