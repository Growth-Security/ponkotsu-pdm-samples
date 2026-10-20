package info.syachiku.books;

import java.sql.PreparedStatement;
import java.sql.Statement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final JdbcTemplate jdbc;

    public OrderService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public OrderResponse create(int quantity) {
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO orders (quantity) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, quantity);
            return statement;
        }, keyHolder);

        // このサンプルの商品は1種類。在庫不足ならUPDATEは0件になる。
        int updated = jdbc.update("""
                UPDATE stock SET available = available - ?
                WHERE product_id = 1 AND available >= ?
                """, quantity, quantity);
        if (updated == 0) {
            // Controllerまで捕捉せず返すため、先ほどのINSERTもロールバックされる。
            throw new InsufficientStockException();
        }
        return new OrderResponse(keyHolder.getKey().longValue(), quantity);
    }
}
