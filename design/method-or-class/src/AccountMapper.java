public final class AccountMapper {
    private final int maxSeats;

    // 分割だけを行う標準版: 正のintを受け付ける。
    public AccountMapper() {
        this(Integer.MAX_VALUE);
    }

    // 別の変更要求: 契約数に上限を設ける場合だけ指定する。
    public AccountMapper(int maxSeats) {
        if (maxSeats < 1) {
            throw new IllegalArgumentException("上限は1以上");
        }
        this.maxSeats = maxSeats;
    }

    public Account toAccount(String[] row) {
        if (row.length != 2 || row[0].isBlank()) {
            throw new IllegalArgumentException("列数またはメールが不正");
        }
        int seats = Integer.parseInt(row[1]);
        if (seats < 1) {
            throw new IllegalArgumentException("契約数は1以上");
        }
        if (seats > maxSeats) {
            throw new IllegalArgumentException("契約数は" + maxSeats + "以下");
        }
        return new Account(row[0], seats);
    }
}
