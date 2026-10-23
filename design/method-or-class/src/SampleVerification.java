import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class SampleVerification {
    @FunctionalInterface
    interface Importer {
        void run(List<String[]> rows, Consumer<List<Account>> save);
    }

    record Outcome(int saveCalls, List<List<Account>> batches,
                   Class<?> exceptionType, String message) {}

    record Case(String name, List<String[]> rows, Outcome expected) {}

    private static List<String[]> rows(String[]... rows) {
        return List.of(rows);
    }

    private static String[] row(String email, String seats) {
        return new String[]{email, seats};
    }

    private static Outcome saved(Account... accounts) {
        return new Outcome(1, List.of(List.of(accounts)), null, null);
    }

    private static Outcome rejected(Class<?> type, String message) {
        return new Outcome(0, List.of(), type, message);
    }

    private static Outcome observe(Importer importer, List<String[]> rows) {
        var batches = new ArrayList<List<Account>>();
        try {
            importer.run(rows, accounts -> batches.add(List.copyOf(accounts)));
            return new Outcome(batches.size(), List.copyOf(batches), null, null);
        } catch (RuntimeException e) {
            return new Outcome(batches.size(), List.copyOf(batches),
                    e.getClass(), e.getMessage());
        }
    }

    private static void check(String version, Importer importer, Case c) {
        Outcome actual = observe(importer, c.rows());
        if (!c.expected().equals(actual)) {
            throw new AssertionError(version + " / " + c.name()
                    + " expected=" + c.expected() + " actual=" + actual);
        }
        System.out.println("PASS " + version + " / " + c.name() + " -> " + actual);
    }

    public static void main(String[] args) {
        var cases = List.of(
            new Case("two valid rows", rows(row("a@example.test", "1"), row("b@example.test", "2")),
                    saved(new Account("a@example.test", 1), new Account("b@example.test", 2))),
            new Case("invalid first row", rows(row("", "1"), row("b@example.test", "2")),
                    rejected(IllegalArgumentException.class, "列数またはメールが不正")),
            new Case("invalid middle row", rows(row("a@example.test", "1"), row("", "2"), row("c@example.test", "3")),
                    rejected(IllegalArgumentException.class, "列数またはメールが不正")),
            new Case("not a number", rows(row("a@example.test", "x")),
                    rejected(NumberFormatException.class, "For input string: \"x\"")),
            new Case("zero seats", rows(row("a@example.test", "0")),
                    rejected(IllegalArgumentException.class, "契約数は1以上")),
            new Case("negative seats", rows(row("a@example.test", "-1")),
                    rejected(IllegalArgumentException.class, "契約数は1以上")),
            new Case("missing column", rows(new String[]{"a@example.test"}),
                    rejected(IllegalArgumentException.class, "列数またはメールが不正")),
            new Case("extra column", rows(new String[]{"a@example.test", "1", "extra"}),
                    rejected(IllegalArgumentException.class, "列数またはメールが不正")),
            new Case("empty input", List.of(), saved()),
            new Case("101 is valid in baseline", rows(row("a@example.test", "101")),
                    saved(new Account("a@example.test", 101))),
            new Case("maximum int is valid", rows(row("a@example.test", "2147483647")),
                    saved(new Account("a@example.test", Integer.MAX_VALUE)))
        );
        Importer[] versions = {
            new BeforeImporter()::importAccounts,
            new MethodImporter()::importAccounts,
            new ClassImporter(new AccountMapper())::importAccounts
        };
        String[] names = {"before", "private-method", "separate-class"};
        for (int i = 0; i < versions.length; i++) {
            for (Case c : cases) check(names[i], versions[i], c);
        }

        // 分割の検証とは別: 契約数を1〜100に変更する要求。
        Importer limited = new ClassImporter(new AccountMapper(100))::importAccounts;
        var extension = List.of(
            new Case("0 rejected", rows(row("a@example.test", "0")), rejected(IllegalArgumentException.class, "契約数は1以上")),
            new Case("1 accepted", rows(row("a@example.test", "1")), saved(new Account("a@example.test", 1))),
            new Case("100 accepted", rows(row("a@example.test", "100")), saved(new Account("a@example.test", 100))),
            new Case("101 rejected", rows(row("a@example.test", "101")), rejected(IllegalArgumentException.class, "契約数は100以下")),
            new Case("invalid middle still saves zero times", rows(row("a@example.test", "1"), row("b@example.test", "101")), rejected(IllegalArgumentException.class, "契約数は100以下")),
            new Case("empty input still saves once", List.of(), saved())
        );
        for (Case c : extension) check("extension-1-to-100", limited, c);
        System.out.println("OK: baseline 11 cases x 3 versions = 33 checks; extension 6 checks; total 39 checks.");
    }
}
