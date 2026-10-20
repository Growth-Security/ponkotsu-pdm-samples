package info.syachiku.books;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderFlowTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    // テスト自体には@Transactionalを付けず、Serviceの境界だけで確定する。
    @BeforeEach
    void resetDatabase() {
        jdbc.update("DELETE FROM orders");
        jdbc.update("UPDATE stock SET available = 5 WHERE product_id = 1");
    }

    @Test
    void savesOrderAndReturnsGeneratedIdAndQuantity() throws Exception {
        long id = new com.fasterxml.jackson.databind.ObjectMapper().readTree(
                mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}"))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.orderId").isNumber())
                        .andExpect(jsonPath("$.quantity").value(2))
                        .andReturn().getResponse().getContentAsString())
                .get("orderId").asLong();

        assertThat(jdbc.queryForObject("SELECT quantity FROM orders WHERE id = ?", Integer.class, id))
                .isEqualTo(2);
        assertThat(orderCount()).isEqualTo(1);
        assertThat(available()).isEqualTo(3);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"quantity\":0}", "{\"quantity\":\"abc\"}", "{}", "{\"quantity\":null}"})
    void invalidInputReturns400WithoutChangingDatabase(String json) throws Exception {
        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        assertThat(orderCount()).isZero();
        assertThat(available()).isEqualTo(5);
    }

    @Test
    void stockFailureRollsBackInsertedOrderAndPreservesCommittedData() throws Exception {
        // 先に成功した注文がある状態で、残り在庫3に対して4個を注文する。
        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":2}")).andExpect(status().isCreated());
        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":4}"))
                .andExpect(status().isConflict())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(InsufficientStockException.class));

        // 別のDB読み取りで、失敗したINSERTが残っていないことを確認する。
        assertThat(orderCount()).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT quantity FROM orders", Integer.class)).isEqualTo(2);
        assertThat(available()).isEqualTo(3);
    }

    private int orderCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
    }

    private int available() {
        return jdbc.queryForObject("SELECT available FROM stock WHERE product_id = 1", Integer.class);
    }
}
