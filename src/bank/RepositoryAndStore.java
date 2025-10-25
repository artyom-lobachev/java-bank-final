package bank;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

class AccountRepository implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, BankAccount> byId = new HashMap<>();
    private final Map<String, String> byIban = new HashMap<>();
    private final Map<String, Set<String>> byBic = new HashMap<>();
    private final Map<String, Set<String>> byOwner = new HashMap<>();
    private final Map<String, Set<String>> byBank = new HashMap<>();

    public Collection<BankAccount> all() { return byId.values(); }

    public BankAccount add(BankAccount acc) {
        byId.put(acc.id(), acc);
        byIban.put(acc.iban(), acc.id());
        index(byBic, acc.bic(), acc.id());
        index(byOwner, acc.ownerName().toLowerCase(Locale.ROOT), acc.id());
        index(byBank, acc.bankName().toLowerCase(Locale.ROOT), acc.id());
        return acc;
    }

    public Optional<BankAccount> getById(String id) { return Optional.ofNullable(byId.get(id)); }
    public Optional<BankAccount> getByIban(String iban) {
        String id = byIban.get(iban);
        return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
    }

    private static void index(Map<String, Set<String>> map, String key, String id) {
        map.computeIfAbsent(key, k -> new HashSet<>()).add(id);
    }

    public List<BankAccount> searchAccounts(String iban, String bic, String ownerSubstr, String bankSubstr) {
        // тут я строю множество кандидатов от самого узкого критерия к более широким
        Set<String> candidateIds = null;

        if (iban != null && !iban.isBlank()) {
            String id = byIban.get(iban);
            candidateIds = (id == null) ? Set.of() : Set.of(id);
        }
        if (bic != null && !bic.isBlank()) {
            candidateIds = intersect(candidateIds, byBic.getOrDefault(bic, Set.of()));
        }
        if (ownerSubstr != null && !ownerSubstr.isBlank()) {
            String key = ownerSubstr.toLowerCase(Locale.ROOT);
            candidateIds = filterBySubstring(candidateIds, byOwner, key);
        }
        if (bankSubstr != null && !bankSubstr.isBlank()) {
            String key = bankSubstr.toLowerCase(Locale.ROOT);
            candidateIds = filterBySubstring(candidateIds, byBank, key);
        }
        if (candidateIds == null) return new ArrayList<>(byId.values());

        List<BankAccount> res = new ArrayList<>();
        for (String id : candidateIds) {
            BankAccount acc = byId.get(id);
            if (acc != null) res.add(acc);
        }
        return res;
    }

    private static Set<String> intersect(Set<String> acc, Set<String> next) {
        if (acc == null) return new HashSet<>(next);
        Set<String> r = new HashSet<>(acc);
        r.retainAll(next);
        return r;
    }

    private static Set<String> filterBySubstring(Set<String> acc, Map<String, Set<String>> index, String substr) {
        Set<String> hits = new HashSet<>();
        for (var e : index.entrySet()) if (e.getKey().contains(substr)) hits.addAll(e.getValue());
        return intersect(acc, hits);
    }

    public List<Transaction> searchTransactions(String accountId,
                                                TransactionType type,
                                                LocalDateTime from,
                                                LocalDateTime to,
                                                BigDecimal min,
                                                BigDecimal max,
                                                String descrSubstr) {
        BankAccount acc = byId.get(accountId);
        if (acc == null) return List.of();

        List<Transaction> out = new ArrayList<>();
        for (Transaction t : acc.transactions()) {
            if (type != null && t.type() != type) continue;
            if (from != null && t.timestamp().isBefore(from)) continue;
            if (to != null && t.timestamp().isAfter(to)) continue;
            if (min != null && t.amount().compareTo(min) < 0) continue;
            if (max != null && t.amount().compareTo(max) > 0) continue;
            if (descrSubstr != null && !descrSubstr.isBlank()
                    && !t.description().toLowerCase(Locale.ROOT).contains(descrSubstr.toLowerCase(Locale.ROOT))) continue;
            out.add(t);
        }
        return out;
    }

    public void exportCsv(String accountId, Path path) {
        BankAccount acc = byId.get(accountId);
        if (acc == null) throw new IllegalArgumentException("Нет такого счёта");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(path))) { // гарантируется закрытие файла
            pw.println("timestamp;type;amount;description;iban;owner;bank");
            for (Transaction t : acc.transactions()) {
                pw.printf("%s;%s;%s;%s;%s;%s;%s%n",
                        t.timestamp().format(fmt),
                        t.type(),
                        t.amount().toPlainString(),
                        escape(t.description()),
                        acc.iban(), acc.ownerName(), acc.bankName());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String escape(String s) { return s == null ? "" : s.replace(";", ","); }
}

interface DataStore {
    void save(AccountRepository repo);
    AccountRepository loadOrCreateEmpty();
}

class FileDataStore implements DataStore {
    private final String filename;
    public FileDataStore(String filename) { this.filename = filename; }

    @Override public void save(AccountRepository repo) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(repo);
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка сохранения: " + filename, e);
        }
    }

    @Override public AccountRepository loadOrCreateEmpty() {
        File f = new File(filename);
        if (!f.exists()) return new AccountRepository();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Object o = ois.readObject();
            return (AccountRepository) o;
        } catch (IOException | ClassNotFoundException e) {
            return new AccountRepository(); // на битом файле стартую с пустого состояния
        }
    }
}
