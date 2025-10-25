package bank;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;

class SwingApp {
    public static void launch(AccountRepository repo, DataStore store) {
        SwingUtilities.invokeLater(() -> new SwingApp(repo, store).show()); // запуск GUI на EDT
    }

    private final AccountRepository repo;
    private final DataStore store;

    private JFrame frame;
    private JComboBox<BankAccount> accountCombo;
    private JTable table;
    private TransactionTableModel tableModel;
    private JTextField amountField;
    private JTextField descrField;
    private JTextField searchOwnerField;

    public SwingApp(AccountRepository repo, DataStore store) {
        this.repo = repo;
        this.store = store;
    }

    private void show() {
        frame = new JFrame("Bank Accounts");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        var top = new JPanel(new BorderLayout(8, 8));
        accountCombo = new JComboBox<>(repo.all().toArray(new BankAccount[0]));
        accountCombo.setPreferredSize(new Dimension(450, accountCombo.getPreferredSize().height));
        top.add(accountCombo, BorderLayout.WEST);

        var searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchOwnerField = new JTextField(16);
        searchOwnerField.addActionListener(this::onSearchAccounts);
        var searchBtn = new JButton("Поиск счетов");
        searchBtn.addActionListener(this::onSearchAccounts);
        var newAcc = new JButton("Открыть счёт");
        newAcc.addActionListener(this::onCreateAccount);
        searchPanel.add(new JLabel("Владелец/Банк:"));
        searchPanel.add(searchOwnerField);
        searchPanel.add(searchBtn);
        searchPanel.add(newAcc);
        top.add(searchPanel, BorderLayout.CENTER);
        frame.add(top, BorderLayout.NORTH);

        tableModel = new TransactionTableModel();
        table = new JTable(tableModel);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        var bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        amountField = new JTextField(8);
        descrField = new JTextField(16);
        var depositBtn = new JButton("Положить");
        depositBtn.addActionListener(e -> doOp(true));
        var withdrawBtn = new JButton("Снять");
        withdrawBtn.addActionListener(e -> doOp(false));
        var exportBtn = new JButton("Экспорт CSV");
        exportBtn.addActionListener(this::onExport);
        var saveBtn = new JButton("Сохранить");
        saveBtn.addActionListener(e -> store.save(repo));
        bottom.add(new JLabel("Сумма:")); bottom.add(amountField);
        bottom.add(new JLabel("Описание:")); bottom.add(descrField);
        bottom.add(depositBtn); bottom.add(withdrawBtn);
        bottom.add(exportBtn); bottom.add(saveBtn);
        frame.add(bottom, BorderLayout.SOUTH);

        accountCombo.addActionListener(e -> refreshTable());
        if (accountCombo.getItemCount() > 0) accountCombo.setSelectedIndex(0);
        refreshTable();

        frame.pack();
        frame.setSize(Math.max(900, frame.getWidth()), Math.max(500, frame.getHeight()));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        if (repo.all().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Список счетов пуст. Нажмите ‘Открыть счёт’, чтобы добавить первый.");
        }
    }

    private void onSearchAccounts(ActionEvent e) {
        String q = searchOwnerField.getText().trim();
        if (q.isBlank()) {
            accountCombo.setModel(new DefaultComboBoxModel<>(new ArrayList<>(repo.all()).toArray(new BankAccount[0])));
            if (accountCombo.getItemCount() > 0) accountCombo.setSelectedIndex(0);
            refreshTable();
            return;
        }
        String lq = q.toLowerCase(Locale.ROOT);
        // это OR-поиск по всем ключевым полям: владелец/банк/IBAN/BIC
        java.util.List<BankAccount> matches = new ArrayList<>();
        for (BankAccount acc : repo.all()) {
            if (acc.ownerName().toLowerCase(Locale.ROOT).contains(lq)
                    || acc.bankName().toLowerCase(Locale.ROOT).contains(lq)
                    || acc.iban().toLowerCase(Locale.ROOT).contains(lq)
                    || acc.bic().toLowerCase(Locale.ROOT).contains(lq)) {
                matches.add(acc);
            }
        }
        if (matches.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Ничего не найдено. Измените запрос или откройте новый счёт.");
            return;
        }
        accountCombo.setModel(new DefaultComboBoxModel<>(matches.toArray(new BankAccount[0])));
        accountCombo.setSelectedIndex(0);
        refreshTable();
    }

    private void onCreateAccount(ActionEvent e) {
        JTextField iban = new JTextField();
        JTextField bic = new JTextField();
        JTextField bank = new JTextField();
        JTextField owner = new JTextField();
        int opt = JOptionPane.showConfirmDialog(frame,
                new Object[]{"IBAN:", iban, "BIC:", bic, "Банк:", bank, "Владелец:", owner},
                "Новый счёт", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            BankAccount acc = new BankAccount(iban.getText().trim(), bic.getText().trim(),
                    bank.getText().trim(), owner.getText().trim());
            repo.add(acc);
            ComboBoxModel<BankAccount> model = accountCombo.getModel();
            if (model instanceof DefaultComboBoxModel<BankAccount> m) m.addElement(acc);
            else accountCombo.setModel(new DefaultComboBoxModel<>(new BankAccount[]{acc}));
            accountCombo.setSelectedItem(acc);
            refreshTable();
        }
    }

    private void doOp(boolean deposit) {
        BankAccount acc = (BankAccount) accountCombo.getSelectedItem();
        if (acc == null) return;
        String raw = amountField.getText().trim().replace(',', '.');
        if (raw.isBlank()) {
            JOptionPane.showMessageDialog(frame, "Введите сумму.", "Ошибка ввода", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            BigDecimal amt = new BigDecimal(raw);
            String d = descrField.getText();
            if (deposit) acc.deposit(amt, d); else acc.withdraw(amt, d);
            refreshTable();
            amountField.setText(""); descrField.setText("");
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(frame, "Сумма должна быть числом", "Ошибка ввода", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException iae) {
            JOptionPane.showMessageDialog(frame, iae.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onExport(ActionEvent e) {
        BankAccount acc = (BankAccount) accountCombo.getSelectedItem();
        if (acc == null) return;
        var ch = new JFileChooser();
        ch.setSelectedFile(new File("transactions.csv"));
        if (ch.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                repo.exportCsv(acc.id(), ch.getSelectedFile().toPath());
                JOptionPane.showMessageDialog(frame, "Экспорт завершён");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Ошибка экспорта", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshTable() {
        BankAccount acc = (BankAccount) accountCombo.getSelectedItem();
        tableModel.setData(acc == null ? java.util.List.of() : acc.transactions());
    }

    static class TransactionTableModel extends AbstractTableModel {
        private final String[] cols = {"Время", "Тип", "Сумма", "Описание"};
        private java.util.List<Transaction> data = new ArrayList<>();
        public void setData(java.util.List<Transaction> d) { this.data = new ArrayList<>(d); fireTableDataChanged(); }
        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Object getValueAt(int r, int c) {
            Transaction t = data.get(r);
            return switch (c) {
                case 0 -> t.timestamp();
                case 1 -> t.type();
                case 2 -> t.amount();
                case 3 -> t.description();
                default -> "";
            };
        }
    }
}
