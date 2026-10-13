package info.syachiku.blog;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public long register(UserRegisterCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new DuplicateEmailException();
        }
        return userRepository.save(command);
    }
}
