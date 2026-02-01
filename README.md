# Student-Expense-Tracker
Student Expense Tracker: A JavaFX/SQLite app solving student financial stress. Features categorized tracking (meals, transport), a Budget Corrector to suggest spending cuts, and visual analytics via real-time charts. Built using SOLID and OOP, it ensures financial discipline through smart, modular architecture and data-driven insights.
## 👥 Team Roles & Responsibilities
| Member | Role               | Responsibility |
| :--- |:-------------------| :--- |
| **Hanna** | **Project Lead**               | Model classes, Git management, Code Review |
| **Member 2** | **Database Lead**  | Repository Layer (SQLite integration) |
| **Member 3** | **LLogic Developer**     | Service Layer (Budget math & Correction logic) |
| **Member 4** | **UI/UX Developer** | View Layer (JavaFX layouts & CSS) |
| **Member 5** | **IIntegration Developer**    | Controller Layer (Wiring UI to Logic) |
Follow the link below to see our full class diagram and system design:
[https://lucid.app/lucidchart/1553a258-017f-4bd9-b2ab-3ea33a1654a5/view?page=0_0&invitationId=inv_ebdeef3b-2f95-4993-a667-4b3f4d4f7096#]

## How to run

**Requirements:** Java 17+, Maven (optional).

- **With Maven:**  
  `mvn javafx:run`  
  Or: `mvn compile exec:java -Dexec.mainClass="com.aau.se.expensetracker.StudentExpenseTrackerApp"`  
  (For JavaFX 11+, you may need: `mvn compile exec:java -Dexec.mainClass="com.aau.se.expensetracker.StudentExpenseTrackerApp" -Dexec.args="--module-path <path-to-javafx-sdk>/lib --add-modules javafx.controls,javafx.fxml"`)

- **From IDE:**  
  Run the main class `com.aau.se.expensetracker.StudentExpenseTrackerApp` and add JavaFX to the module path / classpath if required by your JDK.

Expenses are stored in `expenses.txt` in the working directory.