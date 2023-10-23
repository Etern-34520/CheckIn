package indi.etern.checkIn.service;

import indi.etern.checkIn.entities.question.interfaces.multipleChoice.Choice;
import indi.etern.checkIn.repositories.ChoiceRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class ChoiceService {
    @Resource
    ChoiceRepository choiceRepository;
    public void save(Choice choice) {
        choiceRepository.save(choice);
    }
    public void saveAll(Collection<Choice> choices){
        choiceRepository.saveAll(choices);
    }
    
    public List<Choice> findAll() {
        return choiceRepository.findAll();
    }
    
    public void saveAndFlush(Choice choice){
        choiceRepository.saveAndFlush(choice);
    }
}
