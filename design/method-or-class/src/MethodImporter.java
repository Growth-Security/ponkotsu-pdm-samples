import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class MethodImporter {
    public void importAccounts(List<String[]> rows, Consumer<List<Account>> save) {
        var accounts = new ArrayList<Account>();
        for (String[] row : rows) {
            accounts.add(toAccount(row));
        }
        save.accept(accounts);
    }

    private Account toAccount(String[] row) {
        if (row.length != 2 || row[0].isBlank()) {
            throw new IllegalArgumentException("列数またはメールが不正");
        }
        int seats = Integer.parseInt(row[1]);
        if (seats < 1) {
            throw new IllegalArgumentException("契約数は1以上");
        }
        return new Account(row[0], seats);
    }
}
