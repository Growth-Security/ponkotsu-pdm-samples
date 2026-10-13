package info.syachiku.blog;

public interface UserRepository {
    boolean existsByEmail(String email);
    long save(UserRegisterCommand command);
}
