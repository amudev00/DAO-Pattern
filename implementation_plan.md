# Implementation Plan - Interactive Seller Management CLI

This implementation plan details the strategy to replace the demo `App.java` in the workspace with a robust, interactive, and user-friendly console application for managing sellers and departments.

---

## User Review Required

We are designing this application to run in a continuous interactive loop. The user inputs will be validated thoroughly, and all database interactions will be safely wrapped to prevent crashes.

> [!IMPORTANT]
> - **DAO Constraint**: We will respect the requirement to leave `SellerDao` and `SellerDaoJDBC` unmodified.
> - **No External Libraries**: All logic, including table generation and input parsing, will be implemented using core Java APIs (standard library classes like `Scanner`, `LocalDate`, `List`).

---

## Open Questions

We would like to get your confirmation on the following proposed details:

> [!NOTE]
> 1. **Search Seller by Name (In-Memory vs. Direct Query)**: Since `SellerDao` does not have a `findByName` method, we plan to retrieve all sellers via `sellerDao.findAll()` and filter them in-memory using a case-insensitive name matching helper. Is this approach acceptable, or would you prefer a direct JDBC query to the database?
> 2. **Show Departments**: There is no implementation of `DepartmentDao` (`DepartmentDaoJDBC` does not exist). To retrieve the list of departments, we plan to query the database directly using `DB.getConnection()` inside `App.java`. Do you approve of this approach?
> 3. **Salary Statistics**: We propose to calculate and display the following statistics from the seller list:
>    - Total payroll (sum of salaries)
>    - Average salary
>    - Minimum salary (and the seller's name)
>    - Maximum salary (and the seller's name)
>    - Average salary grouped by department
>    Do you want to add or change any of these statistics?
> 4. **Update Strategy**: For updating a seller, we propose showing the current value and allowing the user to press Enter without typing anything to keep the current value. Is this acceptable?

---

## Proposed Changes

We will modify `App.java` to implement the entire console menu application. No other files need to be modified.

### Main Console Application

#### [MODIFY] [App.java](file:///home/amu67/Programming/java/DaoProj/Java-DAO-JDBC-MySQL/src/application/App.java)

We will rewrite `App.java` to include:
1. **Interactive Loop**: An infinite `while` loop that calls `showMenu()`, reads the choice, and routes to helper methods.
2. **Helper Methods**:
   - `showWelcomeScreen()`: Prints the ASCII banner.
   - `showMenu()`: Prints options 1 to 10.
   - `viewAllSellers()`: Displays all sellers in a formatted table.
   - `searchSellerById()`: Prompts for ID, retrieves, and displays.
   - `findSellersByDepartment()`: Prompts for Department ID, retrieves, and displays.
   - `insertSeller()`: Prompts for name, email, birth date, base salary, department ID, validates, and inserts.
   - `updateSeller()`: Prompts for Seller ID, retrieves it, prompts for updates field-by-field (allowing Enter to skip), validates, and updates.
   - `deleteSeller()`: Prompts for Seller ID and deletes it.
   - `showDepartments()`: Queries `department` database table directly via JDBC and displays it.
   - `searchSellerByName()`: Prompts for name fragment, filters results of `findAll()` in-memory, and prints them.
   - `showSalaryStatistics()`: Computes payroll statistics on `findAll()` results and displays them.
3. **Input Utilities**:
   - `readString(String prompt)`: Prints prompt, reads a line of input.
   - `readPositiveInt(String prompt)`: Standardizes integer reading, handles invalid formats/values, ensures positive constraint.
   - `readNonNegativeDouble(String prompt)`: Standardizes double reading, handles invalid formats/values, ensures non-negative constraint.
   - `readLocalDate(String prompt)`: Validates dates in `yyyy-MM-dd` format.
4. **Table Printer**:
   - `printSellersTable(List<Seller> sellers)`: Formats and displays tabular seller lists.
   - `printDepartmentsTable(List<Department> departments)`: Formats and displays tabular department lists.

---

## Verification Plan

### Automated / Manual Verification
1. **Run the Application**: Compile and run the `App` class.
2. **Interactive Tests**:
   Manually can be done by me - just write the test examples down here 

#### Test Cases for Verification:
- **Test Case 1: View Sellers and Departments**
  - Select option `1` to display all sellers in a formatted grid.
  - Select option `7` to display all departments in a formatted grid.
  - Verify formatting, alignment, and decimal padding on salaries.

- **Test Case 2: Validation of Incorrect Inputs**
  - Select option `2` (Find by ID) and enter an invalid ID value like `-5` or `abc`. Verify that the application prints a validation warning and re-prompts for input without crashing.
  - Select option `4` (Add Seller) and enter an invalid date format like `12-12-1990`. Verify that the application blocks and asks for the correct format (`yyyy-MM-dd`).
  - During seller creation/update, input a non-existent Department ID (e.g., `99`). Verify that the application alerts you with `✗ Invalid department` and prompts you to try again or cancel.

- **Test Case 3: Seller Updates (Keeping Default Values)**
  - Select option `5` (Update Seller) and input a valid seller ID.
  - For `Name`, `Email`, and `Birth Date`, press `Enter` to keep existing values.
  - For `Base Salary`, input a new salary.
  - For `Department ID`, press `Enter` to keep the current one.
  - Verify that only the salary was updated.

- **Test Case 4: Search and Stats**
  - Select option `8` (Search Seller by Name) and input a case-insensitive search term (e.g., `alex`). Verify that both ID 3 and 7 (or any matching sellers) display in the search table.
  - Select option `9` (Salary Statistics). Verify that the calculation breakdown for total payroll, average, maximum earner, minimum earner, and department averages prints out.

- **Test Case 5: Graceful Exit**
  - Select option `10` (Exit). Verify the log messages confirming connection closure and successful application exit.
