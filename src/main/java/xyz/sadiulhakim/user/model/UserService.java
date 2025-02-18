package xyz.sadiulhakim.user.model;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.modulith.NamedInterface;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import xyz.sadiulhakim.util.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@NamedInterface("user-service")
@RequiredArgsConstructor
public class UserService {

    private final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepo;
    private final AppProperties appProperties;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Async("taskExecutor")
    @EventListener
    void appStarted(ApplicationReadyEvent event) {
        User user = userRepo.findByEmail("sadiulhakim@gmail.com").orElse(null);
        if (user == null) {
            userRepo.save(
                    new User(null, "Sadiul", "Hakim", "sadiulhakim@gmail.com",
                            passwordEncoder.encode("hakim@123"), "ROLE_ADMIN",
                            appProperties.getDefaultUserPhotoName(), ColorUtil.getRandomColor(),
                            LocalDateTime.now())
            );
        }
    }

    public User findByEmail(String email) {

        return userRepo.findByEmail(email).orElse(null);
    }

    public Optional<User> getById(UUID userId) {

        Optional<User> user = userRepo.findById(userId);
        if (user.isEmpty()) {
            LOGGER.error("UserService.getById :: Could not find user {}", userId);
        }

        return user;
    }

    public void save(UserDTO user) {

        try {

            LOGGER.info("UserService.save :: saving/updating user {}", user.getEmail());

            User existingUser = findByEmail(user.getEmail());
            if (existingUser != null) {
                LOGGER.warn("UserService.save :: User with email {} already exists!", user.getEmail());
            }

            User userModel = modelMapper.map(user, User.class);
            userModel.setPicture(appProperties.getDefaultUserPhotoName());
            userModel.setRole("ROLE_USER");
            userModel.setCreatedAt(LocalDateTime.now());
            userModel.setPassword(passwordEncoder.encode(user.getRawPassword()));
            userModel.setTextColor(ColorUtil.getRandomColor());
            userRepo.save(userModel);

        } catch (Exception ex) {
            LOGGER.error("UserService.save :: {}", ex.getMessage());
        }
    }

    private void update(UUID userId, UserDTO user, MultipartFile photo) throws IOException {

        User exUser = getById(userId).orElse(null);
        if (exUser == null) {
            LOGGER.warn("UserService.update :: User does not exists with id {}", user);
            return;
        }

        if (StringUtils.hasText(user.getFirstname())) {
            exUser.setFirstname(user.getFirstname());
        }

        if (StringUtils.hasText(user.getLastname())) {
            exUser.setLastname(user.getLastname());
        }

        if (photo != null && !Objects.requireNonNull(photo.getOriginalFilename()).isEmpty()) {
            String fileName = FileUtil.uploadFile(appProperties.getUserImageFolder(), photo.getOriginalFilename(), photo.getInputStream());

            if (StringUtils.hasText(fileName) && !exUser.getPicture().equals(appProperties.getDefaultUserPhotoName())) {
                boolean deleted = FileUtil.deleteFile(appProperties.getUserImageFolder(), exUser.getPicture());
                if (deleted) {
                    LOGGER.info("UserService.update :: File {} is deleted", exUser.getPicture());
                }
            }

            if (StringUtils.hasText(fileName)) {
                exUser.setPicture(fileName);
            }
        }

        userRepo.save(exUser);
    }

    public PaginationResult findAllPaginated(int pageNumber) {
        return findAllPaginatedWithSize(pageNumber, appProperties.getPaginationSize());
    }

    public PaginationResult findAllPaginatedWithSize(int pageNumber, int size) {

        LOGGER.info("UserService.findAllPaginated :: finding user page : {}", pageNumber);
        Page<User> page = userRepo.findAll(PageRequest.of(pageNumber, size, Sort.by("name")));
        return PageUtil.prepareResult(page);
    }

    public PaginationResult searchUser(String text, int pageNumber) {

        LOGGER.info("UserService.searchUser :: search user by text : {}", text);
        Page<User> page = userRepo.findByFirstnameContainingOrLastnameContainingOrEmailContaining(text, text, text,
                PageRequest.of(pageNumber, 200));
        return PageUtil.prepareResult(page);
    }

    public void delete(UUID id) {

        Optional<User> user = userRepo.findById(id);
        user.ifPresent(u -> {

            if (!u.getPicture().equals(appProperties.getDefaultUserPhotoName())) {
                boolean deleted = FileUtil.deleteFile(appProperties.getUserImageFolder(), u.getPicture());
                if (deleted) {
                    LOGGER.info("UserService.delete :: deleted file {}", u.getPicture());
                }
            }
            userRepo.delete(u);
        });
    }

    public String changePassword(PasswordDTO passwordDTO, UUID userId) {

        try {
            Optional<User> user = getById(userId);
            if (user.isEmpty()) {
                return "User does not exist!";
            }

            if (!StringUtils.hasText(passwordDTO.getCurrentPassword()) ||
                    !StringUtils.hasText(passwordDTO.getNewPassword()) ||
                    !StringUtils.hasText(passwordDTO.getConfirmPassword())) {
                return "Password can not be empty!";
            }

            if (!passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
                return "Password Does not Match!";
            }

            User exUser = user.get();
            if (passwordEncoder.matches(passwordDTO.getNewPassword(), exUser.getPassword())) {
                return "Invalid Password!";
            }

            exUser.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
            userRepo.save(exUser);

            return "Password is reset successfully!";
        } catch (Exception ex) {
            LOGGER.error("UserService.changePassword :: {}", ex.getMessage());
            return "Could not reset password, Please try again!";
        }
    }

    public long count() {
        return userRepo.numberOfUsers();
    }

    public byte[] getCsvData() {
        final int batchSize = 500;
        int batchNumber = 0;
        StringBuilder sb = new StringBuilder("Id,First Name,Last Name,Email,Role,Picture,Text Color,Date\n");
        Page<User> page;
        do {
            page = userRepo.findAll(PageRequest.of(batchNumber, batchSize));
            List<User> users = page.getContent();
            for (User user : users) {
                sb.append(user.getId())
                        .append(",")
                        .append(user.getFirstname())
                        .append(",")
                        .append(user.getLastname())
                        .append(",")
                        .append(user.getEmail())
                        .append(",")
                        .append(user.getRole())
                        .append(",")
                        .append(user.getPicture())
                        .append(",")
                        .append(user.getTextColor())
                        .append(",")
                        .append(DateUtil.format(user.getCreatedAt()))
                        .append("\n");

            }
            batchNumber++;
        } while (page.hasNext());

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}
