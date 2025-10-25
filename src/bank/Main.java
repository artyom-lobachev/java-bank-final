package bank;

public class Main {
    private static final String DEFAULT_DB = "bank.dat";

    public static void main(String[] args) {
        DataStore store = new FileDataStore(DEFAULT_DB);
        AccountRepository repo = store.loadOrCreateEmpty();
        if (args.length > 0 && "console".equalsIgnoreCase(args[0])) {
            new ConsoleApp(repo, store).run();
        } else {
            SwingApp.launch(repo, store);
        }
    }
}
