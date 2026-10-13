package info.syachiku.learning;

/** DB保存はこの教材の検証範囲外。テストでは戻り値・例外を設定する。 */
public interface OrderRepository {
    long save(OrderDraft draft);
}
