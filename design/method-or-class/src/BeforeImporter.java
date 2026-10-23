import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class BeforeImporter {
    public void importAccounts(List<String[]> rows, Consumer<List<Account>> save) {
        var accounts = new ArrayList<Account>();
        for (String[] row : rows) {
            if (row.length != 2 || row[0].isBlank()) {
                throw new IllegalArgumentException("列数またはメールが不正");
            }
            int seats = Integer.parseInt(row[1]);
            if (seats < 1) {
                throw new IllegalArgumentException("契約数は1以上");
            }
            accounts.add(new Account(row[0], seats));
        }
        save.accept(accounts);
    }
}
