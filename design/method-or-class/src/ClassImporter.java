import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class ClassImporter {
    private final AccountMapper mapper;

    public ClassImporter(AccountMapper mapper) {
        this.mapper = mapper;
    }

    public void importAccounts(List<String[]> rows, Consumer<List<Account>> save) {
        var accounts = new ArrayList<Account>();
        for (String[] row : rows) {
            accounts.add(mapper.toAccount(row));
        }
        save.accept(accounts);
    }
}
