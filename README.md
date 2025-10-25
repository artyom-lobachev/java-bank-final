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

<img width="700" alt="image" src="https://github.com/user-attachments/assets/26dbaeca-0c3a-4391-bc18-548cbe43adf7" />

---
### Пополнение/снятие и история транзакций

<img width="700" alt="image" src="https://github.com/user-attachments/assets/e2f7c7b7-c874-4480-a22c-204943c5b191" />

---
<img width="700" alt="image" src="https://github.com/user-attachments/assets/689f7b4f-73a4-47cd-b77e-e4625b7d0a90" />

---
<img width="700" alt="image" src="https://github.com/user-attachments/assets/2f9b21a7-7c42-40a3-bd2b-6d4c235774a1" />

---
### Проверка на корректность введённых данных

<img width="700" alt="image" src="https://github.com/user-attachments/assets/3fbe32c0-24a7-41ce-bf1e-51f197a41159" />

---
<img width="700" alt="image" src="https://github.com/user-attachments/assets/3dfe3bf0-04a5-45dc-ae23-9431b7b62794" />

---
### Поиск по атрибуту
  
<img width="700" alt="image" src="https://github.com/user-attachments/assets/79cc5b90-8622-48aa-812c-6d60ec028ab0" />

### Экспорт транзакций в CSV
  
<img width="700" alt="image" src="https://github.com/user-attachments/assets/f998afce-e66c-4769-a797-a0e8882d0482" />

