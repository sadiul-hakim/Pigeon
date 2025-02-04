package xyz.sadiulhakim.user.model;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.modulith.NamedInterface;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@NamedInterface("user-service")
public class UserService {
    private final UserRepository repository;
    private final ModelMapper modelMapper;

    public void save(User user) {
        User save = repository.save(user);
    }

    public UserDTO findById(long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id : {}" + id));

        return modelMapper.map(user, UserDTO.class);
    }

    public UserDTO findByEmail(String email) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email : {}" + email));

        return modelMapper.map(user, UserDTO.class);
    }

    public List<UserDTO> findAll() {
        List<User> users = repository.findAll();
        List<UserDTO> dtoList = new ArrayList<>();

        users.forEach(u -> dtoList.add(modelMapper.map(u, UserDTO.class)));
        return dtoList;
    }
}
