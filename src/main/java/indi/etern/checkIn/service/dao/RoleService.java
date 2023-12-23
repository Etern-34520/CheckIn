package indi.etern.checkIn.service.dao;

import indi.etern.checkIn.entities.user.Role;
import indi.etern.checkIn.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    @Autowired
    RoleRepository roleRepository;
    public List<Role> findAll() {
        return roleRepository.findAll();
    }
    
    public void save(Role role) {
        roleRepository.save(role);
    }
    
    public Optional<Role> findByType(String role) {
        return roleRepository.findById(role);
    }
}
