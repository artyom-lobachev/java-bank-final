package bank;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.*;

class ConsoleApp {
    private final AccountRepository repo;
    private final DataStore store;

    public ConsoleApp(AccountRepository repo, DataStore store) {
        this.repo = repo;
        this.store = store;
    }

    public void run() {
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n=== Меню ===");
                System.out.println("1) Открыть счёт");
                System.out.println("2) Положить деньги");
                System.out.println("3) Снять деньги");
                System.out.println("4) Показать баланс");
                System.out.println("5) Показать транзакции");
                System.out.println("6) Искать счета по атрибутам");
                System.out.println("7) Искать транзакции в счёте");
                System.out.println("8) Экспорт транзакций в CSV");
                System.out.println("9) Сохранить и выйти");
                System.out.print("Выбор: ");

                String choice = sc.nextLine().trim();
                switch (choice) {
                    case "1" -> openAccount(sc);
                    case "2" -> deposit(sc);
                    case "3" -> withdraw(sc);
                    case "4" -> showBalance(sc);
                    case "5" -> showTransactions(sc);
                    case "6" -> searchAccounts(sc);
                    case "7" -> searchTransactions(sc);
                    case "8" -> exportCsv(sc);
                    case "9" -> { store.save(repo); System.out.println("Сохранено. Пока!"); return; }
                    default -> System.out.println("Неверный пункт.");
                }
            }
        }
    }

    private void openAccount(Scanner sc) {
        System.out.print("IBAN: "); String iban = sc.nextLine().trim();
        System.out.print("BIC: "); String bic = sc.nextLine().trim();
        System.out.print("Банк: "); String bank = sc.nextLine().trim();
        System.out.print("Владелец: "); String owner = sc.nextLine().trim();
        BankAccount acc = new BankAccount(iban, bic, bank, owner);
        repo.add(acc);
        System.out.println("Создан счёт: " + acc);
    }

    private BankAccount pickAccount(Scanner sc) {
        System.out.print("Введите IBAN или часть имени владельца (пусто — показать все): ");
        String key = sc.nextLine().trim();
        List<BankAccount> list = key.isBlank() ? new ArrayList<>(repo.all()) : repo.searchAccounts(key, null, key, key);
        if (list.isEmpty()) { System.out.println("Не найдено."); return null; }
        for (int i = 0; i < list.size(); i++) System.out.printf("%d) %s%n", i + 1, list.get(i));
        System.out.print("Выберите #: ");
        try {
            int n = Integer.parseInt(sc.nextLine().trim());
            if (n < 1 || n > list.size()) return null;
            return list.get(n - 1);
        } catch (NumberFormatException e) { return null; }
    }

    private static BigDecimal readAmount(Scanner sc, String prompt) {
        System.out.print(prompt);
        String s = sc.nextLine().trim().replace(',', '.');
        try { return new BigDecimal(s).setScale(2, RoundingMode.HALF_UP); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("Сумма должна быть числом"); }
    }

    private void deposit(Scanner sc) {
        BankAccount acc = pickAccount(sc); if (acc == null) return;
        try {
            BigDecimal a = readAmount(sc, "Сумма: ");
            System.out.print("Описание: "); String d = sc.nextLine();
            acc.deposit(a, d);
            System.out.println("Готово. Баланс: " + acc.balance());
        } catch (Exception e) { System.out.println("Ошибка: " + e.getMessage()); }
    }

    private void withdraw(Scanner sc) {
        BankAccount acc = pickAccount(sc); if (acc == null) return;
        try {
            BigDecimal a = readAmount(sc, "Сумма: ");
            System.out.print("Описание: "); String d = sc.nextLine();
            acc.withdraw(a, d);
            System.out.println("Готово. Баланс: " + acc.balance());
        } catch (Exception e) { System.out.println("Ошибка: " + e.getMessage()); }
    }

    private void showBalance(Scanner sc) {
        BankAccount acc = pickAccount(sc); if (acc == null) return;
        System.out.println("Баланс: " + acc.balance());
    }

    private void showTransactions(Scanner sc) {
        BankAccount acc = pickAccount(sc); if (acc == null) return;
        var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Transaction t : acc.transactions())
            System.out.printf("%s | %-10s | %8s | %s%n", t.timestamp().format(fmt), t.type(), t.amount(), t.description());
    }

    private void searchAccounts(Scanner sc) {
        System.out.print("IBAN (или пусто): "); String iban = sc.nextLine().trim();
        System.out.print("BIC (или пусто): "); String bic = sc.nextLine().trim();
        System.out.print("Владелец (подстрока): "); String owner = sc.nextLine().trim();
        System.out.print("Банк (подстрока): "); String bank = sc.nextLine().trim();
        List<BankAccount> res = repo.searchAccounts(emptyToNull(iban), emptyToNull(bic), emptyToNull(owner), emptyToNull(bank));
        if (res.isEmpty()) System.out.println("Ничего не найдено.");
        else res.forEach(System.out::println);
    }

    private void searchTransactions(Scanner sc) {
        BankAccount acc = pickAccount(sc); if (acc == null) return;
        System.out.print("Тип (DEPOSIT/WITHDRAWAL/пусто): ");
        String st = sc.nextLine().trim().toUpperCase(Locale.ROOT);
        TransactionType tt = ("DEPOSIT".equals(st) || "WITHDRAWAL".equals(st)) ? TransactionType.valueOf(st) : null;
        System.out.print("Мин сумма (пусто): "); String smin = sc.nextLine().trim();
        System.out.print("Макс сумма (пусто): "); String smax = sc.nextLine().trim();
        System.out.print("Подстрока в описании (пусто): "); String ds = sc.nextLine().trim();

        BigDecimal min = smin.isBlank() ? null : new BigDecimal(smin.replace(',', '.'));
        BigDecimal max = smax.isBlank() ? null : new BigDecimal(smax.replace(',', '.'));

        List<Transaction> res = repo.searchTransactions(acc.id(), tt, null, null, min, max, ds);
        if (res.isEmpty()) System.out.println("Нет результатов.");
        else for (Transaction t : res) System.out.printf("%s | %s | %s | %s%n", t.timestamp(), t.type(), t.amount(), t.description());
    }

    private void exportCsv(Scanner sc) {
        BankAccount acc = pickAccount(sc); if (acc == null) return;
        System.out.print("Путь к CSV (например, transactions.csv): ");
        String p = sc.nextLine().trim();
        try {
            repo.exportCsv(acc.id(), Path.of(p));
            System.out.println("Экспортировано: " + p);
        } catch (Exception e) { System.out.println("Ошибка экспорта: " + e.getMessage()); }
    }

    private static String emptyToNull(String s) { return (s == null || s.isBlank()) ? null : s; }
}
