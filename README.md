# Java lab 1. Лобачев Артём 24КНТ-3.

## Вариант 2:
Задача: **Банковский счёт**.  
Консольное меню: открыть счёт, положить деньги, снять деньги, показать баланс, вывести список транзакций, искать по атрибутам.

## Файлы
- `src/.../bank/Main.java` — точка входа: загрузка/сохранение данных, запуск GUI или консоли.
- `src/.../bank/Transaction.java` — модель транзакции и тип операции (DEPOSIT/WITHDRAWAL).
- `src/.../bank/BankAccount.java` — модель счёта: баланс, операции пополнения/снятия, список транзакций.
- `src/.../bank/RepositoryAndStore.java` — репозиторий (поиск по атрибутам, экспорт CSV) + файловое хранилище (`bank.dat`).
- `src/.../bank/ConsoleApp.java` — консольное меню и команды.
- `src/.../bank/SwingApp.java` — графический интерфейс (поиск, операции, экспорт, сохранение).

## Дополнительные функции

- **Графический интерфейс:** реализовал через Swing, полнофункциональное окно (поиск, операции, экспорт, сохранение).
- **Сохранение между запусками:** сериализация репозитория в файл `bank.dat` и автоматическая загрузка при старте.
- **Экспорт в CSV:** выгрузка истории транзакций выбранного счёта.

## Демонстрация функционала
### Открытие счёта

<img width="700" alt="image" src="https://github.com/user-attachments/assets/787793f5-8df4-48fb-bbf9-583ac21ffdf5" />

---
### Пополнение/снятие и история транзакций

<img width="700" alt="image" src="https://github.com/user-attachments/assets/af458ef1-5858-4586-8e66-77a7e6ba5d1c" />

---
<img width="700" alt="image" src="https://github.com/user-attachments/assets/d9e219da-7843-42e1-8df2-71ca1762333a" />

---
<img width="700" alt="image" src="https://github.com/user-attachments/assets/cea9486b-5aec-44cc-bf1a-bf5969543b3e" />

---
### Проверка на корректность введённых данных

<img width="700" alt="image" src="https://github.com/user-attachments/assets/a18a6916-c86d-4410-81bc-a0ca141fd586" />

---
<img width="700" alt="image" src="https://github.com/user-attachments/assets/d8af0a2b-f4b9-4b27-934a-a017fec1eab5" />

---
### Поиск по атрибуту
  
<img width="700" alt="image" src="https://github.com/user-attachments/assets/f1cb54b4-924d-42dd-bb6a-a50174410d65" />

### Экспорт транзакций в CSV
  
<img width="700" alt="image" src="https://github.com/user-attachments/assets/9b6b9ede-68aa-47f2-9451-566cc2d318af" />

