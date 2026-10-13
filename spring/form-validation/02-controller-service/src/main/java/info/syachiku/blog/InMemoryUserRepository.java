package info.syachiku.blog;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Repository;

// 説明用。実運用ではDBの一意制約・トランザクションも必要です。
@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<String, UserRegisterCommand> users = new LinkedHashMap<>();

    @Override
    public boolean existsByEmail(String email) {
        return users.containsKey(email);
    }

    @Override
    public long save(UserRegisterCommand command) {
        users.put(command.email(), command);
        return users.size();
    }
}
