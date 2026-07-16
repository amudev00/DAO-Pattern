package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import db.DB;
import db.DbException;
import model.dao.DaoFactory;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

/**
 * Main application class implementing a professional, interactive CLI
 * for the Seller Management System using JDBC, DAO, and MySQL.
 */
public class App {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		SellerDao sellerDao = DaoFactory.createSellerDao();

		showWelcomeScreen();

		boolean running = true;
		while (running) {
			showMenu();
			int choice = readMenuChoice(scanner);

			try {
				switch (choice) {
					case 1:
						viewAllSellers(sellerDao);
						break;
					case 2:
						searchSellerById(scanner, sellerDao);
						break;
					case 3:
						findSellersByDepartment(scanner, sellerDao);
						break;
					case 4:
						insertSeller(scanner, sellerDao);
						break;
					case 5:
						updateSeller(scanner, sellerDao);
						break;
					case 6:
						deleteSeller(scanner, sellerDao);
						break;
					case 7:
						showDepartments();
						break;
					case 8:
						searchSellerByName(scanner, sellerDao);
						break;
					case 9:
						showSalaryStatistics(sellerDao);
						break;
					case 10:
						System.out.println("\nExiting and closing database connection gracefully...");
						running = false;
						break;
					default:
						System.out.println("✗ Invalid option. Please select a number between 1 and 10.");
				}
			} catch (DbException e) {
				System.out.println("\n✗ Database Error: " + e.getMessage());
			} catch (Exception e) {
				System.out.println("\n✗ An unexpected error occurred: " + e.getMessage());
			}
			
			if (running) {
				System.out.println("\nPress Enter to continue...");
				scanner.nextLine();
			}
		}

		// Gracefully close connection on exit
		try {
			DB.closeConnection();
			System.out.println("Database connection closed successfully. Goodbye!");
		} catch (Exception e) {
			System.out.println("✗ Error closing database connection: " + e.getMessage());
		} finally {
			scanner.close();
		}
	}

	/**
	 * Displays a clean ASCII welcome screen.
	 */
	private static void showWelcomeScreen() {
		System.out.println("====================================================");
		System.out.println("             SELLER MANAGEMENT SYSTEM");
		System.out.println("          Java + JDBC + DAO + MySQL");
		System.out.println("====================================================");
	}

	/**
	 * Displays the main menu options.
	 */
	private static void showMenu() {
		System.out.println("\n--- MAIN MENU ---");
		System.out.println("1. View All Sellers");
		System.out.println("2. Find Seller by ID");
		System.out.println("3. Find Sellers by Department");
		System.out.println("4. Add Seller");
		System.out.println("5. Update Seller");
		System.out.println("6. Delete Seller");
		System.out.println("7. Show Departments");
		System.out.println("8. Search Seller by Name");
		System.out.println("9. Salary Statistics");
		System.out.println("10. Exit");
	}

	/**
	 * Reads and validates the menu choice from the console.
	 */
	private static int readMenuChoice(Scanner scanner) {
		while (true) {
			System.out.print("Select an option (1-10): ");
			String input = scanner.nextLine().trim();
			try {
				return Integer.parseInt(input);
			} catch (NumberFormatException e) {
				System.out.println("✗ Invalid input. Please enter a valid menu number.");
			}
		}
	}

	/**
	 * Option 1: Displays all sellers in a formatted table.
	 */
	private static void viewAllSellers(SellerDao sellerDao) {
		System.out.println("\nFetching all sellers...");
		List<Seller> list = sellerDao.findAll();
		printSellersTable(list);
	}

	/**
	 * Option 2: Prompts for seller ID, fetches and displays that seller.
	 */
	private static void searchSellerById(Scanner scanner, SellerDao sellerDao) {
		System.out.println("\n--- FIND SELLER BY ID ---");
		int id = readPositiveInt(scanner, "Enter Seller ID: ");
		
		Seller seller = sellerDao.findById(id);
		if (seller != null) {
			List<Seller> list = new ArrayList<>();
			list.add(seller);
			printSellersTable(list);
		} else {
			System.out.println("✗ Seller not found.");
		}
	}

	/**
	 * Option 3: Prompts for department ID, lists all sellers belonging to it.
	 */
	private static void findSellersByDepartment(Scanner scanner, SellerDao sellerDao) {
		System.out.println("\n--- FIND SELLERS BY DEPARTMENT ---");
		int depId = readPositiveInt(scanner, "Enter Department ID: ");

		Department dep = findDepartmentById(depId);
		if (dep == null) {
			System.out.println("✗ Invalid department.");
			return;
		}

		System.out.println("Fetching sellers for department: " + dep.getName() + "...");
		List<Seller> list = sellerDao.findByDepartment(dep);
		if (list.isEmpty()) {
			System.out.println("No sellers found in this department.");
		} else {
			printSellersTable(list);
		}
	}

	/**
	 * Option 4: Interactively inserts a new seller.
	 */
	private static void insertSeller(Scanner scanner, SellerDao sellerDao) {
		System.out.println("\n--- ADD NEW SELLER ---");
		
		String name = readString(scanner, "Enter Seller Name: ");
		String email = readString(scanner, "Enter Email: ");
		LocalDate birthDate = readLocalDate(scanner, "Enter Birth Date (yyyy-MM-dd): ");
		double baseSalary = readNonNegativeDouble(scanner, "Enter Base Salary: ");
		
		Department dep = null;
		while (dep == null) {
			int depId = readPositiveInt(scanner, "Enter Department ID (or enter 0 to cancel): ");
			if (depId == 0) {
				System.out.println("Operation cancelled.");
				return;
			}
			dep = findDepartmentById(depId);
			if (dep == null) {
				System.out.println("✗ Invalid department. Please enter a valid Department ID.");
			}
		}

		// Convert LocalDate to java.util.Date
		Date date = java.sql.Date.valueOf(birthDate);
		Seller newSeller = new Seller(null, name, email, date, baseSalary, dep);

		sellerDao.insert(newSeller);
		System.out.println("\n✓ Seller inserted successfully.");
		System.out.println("Generated ID: " + newSeller.getId());
	}

	/**
	 * Option 5: Interactively updates an existing seller.
	 */
	private static void updateSeller(Scanner scanner, SellerDao sellerDao) {
		System.out.println("\n--- UPDATE SELLER ---");
		int id = readPositiveInt(scanner, "Enter Seller ID: ");

		Seller seller = sellerDao.findById(id);
		if (seller == null) {
			System.out.println("✗ Seller not found.");
			return;
		}

		System.out.println("\nCurrent Seller Info:");
		List<Seller> singleList = new ArrayList<>();
		singleList.add(seller);
		printSellersTable(singleList);

		System.out.println("\nEnter new values (or press Enter to keep current values):");

		String name = readOptionalString(scanner, "Enter Name [" + seller.getName() + "]: ", seller.getName());
		String email = readOptionalString(scanner, "Enter Email [" + seller.getEmail() + "]: ", seller.getEmail());
		
		// Parse birthDate
		LocalDate currentBirthLocalDate = new java.sql.Date(seller.getBirthDate().getTime()).toLocalDate();
		LocalDate birthDate = readOptionalLocalDate(scanner, "Enter Birth Date (yyyy-MM-dd) [" + currentBirthLocalDate + "]: ", currentBirthLocalDate);
		
		double baseSalary = readOptionalNonNegativeDouble(scanner, "Enter Base Salary [" + String.format("%.2f", seller.getBaseSalary()) + "]: ", seller.getBaseSalary());

		Department dep = seller.getDepartment();
		while (true) {
			System.out.print("Enter Department ID [" + dep.getId() + "]: ");
			String depInput = scanner.nextLine().trim();
			if (depInput.isEmpty()) {
				break;
			}
			try {
				int depId = Integer.parseInt(depInput);
				if (depId <= 0) {
					System.out.println("✗ ID must be positive.");
					continue;
				}
				Department newDep = findDepartmentById(depId);
				if (newDep == null) {
					System.out.println("✗ Invalid department. Please enter a valid Department ID.");
				} else {
					dep = newDep;
					break;
				}
			} catch (NumberFormatException e) {
				System.out.println("✗ Invalid integer. Please enter a valid positive number.");
			}
		}

		seller.setName(name);
		seller.setEmail(email);
		seller.setBirthDate(java.sql.Date.valueOf(birthDate));
		seller.setBaseSalary(baseSalary);
		seller.setDepartment(dep);

		sellerDao.update(seller);
		System.out.println("\n✓ Seller updated successfully.");
	}

	/**
	 * Option 6: Deletes a seller by ID.
	 */
	private static void deleteSeller(Scanner scanner, SellerDao sellerDao) {
		System.out.println("\n--- DELETE SELLER ---");
		int id = readPositiveInt(scanner, "Enter Seller ID: ");

		Seller seller = sellerDao.findById(id);
		if (seller == null) {
			System.out.println("✗ Seller not found.");
			return;
		}

		System.out.print("Are you sure you want to delete seller '" + seller.getName() + "' (ID: " + seller.getId() + ")? (y/N): ");
		String confirm = scanner.nextLine().trim().toLowerCase();
		if (confirm.equals("y") || confirm.equals("yes")) {
			sellerDao.deleteById(id);
			System.out.println("\n✓ Seller deleted successfully.");
		} else {
			System.out.println("Deletion cancelled.");
		}
	}

	/**
	 * Option 7: Queries department database table directly via JDBC and displays it.
	 */
	private static void showDepartments() {
		System.out.println("\nFetching all departments...");
		List<Department> departments = getAllDepartments();
		printDepartmentsTable(departments);
	}

	/**
	 * Option 8: Filters the sellers in-memory by name fragment.
	 */
	private static void searchSellerByName(Scanner scanner, SellerDao sellerDao) {
		System.out.println("\n--- SEARCH SELLER BY NAME ---");
		String searchName = readString(scanner, "Enter search keyword: ").toLowerCase();

		List<Seller> allSellers = sellerDao.findAll();
		List<Seller> matches = new ArrayList<>();
		for (Seller s : allSellers) {
			if (s.getName() != null && s.getName().toLowerCase().contains(searchName)) {
				matches.add(s);
			}
		}

		if (matches.isEmpty()) {
			System.out.println("No sellers matched the query: " + searchName);
		} else {
			printSellersTable(matches);
		}
	}

	/**
	 * Option 9: Computes salary statistics on the list of all sellers.
	 */
	private static void showSalaryStatistics(SellerDao sellerDao) {
		List<Seller> sellers = sellerDao.findAll();
		if (sellers.isEmpty()) {
			System.out.println("No sellers found to calculate statistics.");
			return;
		}

		double total = 0.0;
		double minSalary = Double.MAX_VALUE;
		double maxSalary = -Double.MAX_VALUE;
		List<Seller> minSellers = new ArrayList<>();
		List<Seller> maxSellers = new ArrayList<>();

		Map<String, List<Double>> deptSalaries = new HashMap<>();

		for (Seller s : sellers) {
			double sal = s.getBaseSalary() != null ? s.getBaseSalary() : 0.0;
			total += sal;

			if (sal < minSalary) {
				minSalary = sal;
				minSellers.clear();
				minSellers.add(s);
			} else if (sal == minSalary) {
				minSellers.add(s);
			}

			if (sal > maxSalary) {
				maxSalary = sal;
				maxSellers.clear();
				maxSellers.add(s);
			} else if (sal == maxSalary) {
				maxSellers.add(s);
			}

			String depName = (s.getDepartment() != null && s.getDepartment().getName() != null)
					? s.getDepartment().getName()
					: "Unknown";
			deptSalaries.putIfAbsent(depName, new ArrayList<>());
			deptSalaries.get(depName).add(sal);
		}

		double average = total / sellers.size();

		System.out.println("\n====================================================");
		System.out.println("                 SALARY STATISTICS                  ");
		System.out.println("====================================================");
		System.out.printf("Total Payroll:        $%,.2f%n", total);
		System.out.printf("Average Salary:       $%,.2f (among %d sellers)%n", average, sellers.size());
		System.out.printf("Minimum Salary:       $%,.2f%n", minSalary);
		System.out.print("  Earned by:          ");
		for (int i = 0; i < minSellers.size(); i++) {
			if (i > 0) System.out.print(", ");
			System.out.print(minSellers.get(i).getName() + " (ID: " + minSellers.get(i).getId() + ")");
		}
		System.out.println();

		System.out.printf("Maximum Salary:       $%,.2f%n", maxSalary);
		System.out.print("  Earned by:          ");
		for (int i = 0; i < maxSellers.size(); i++) {
			if (i > 0) System.out.print(", ");
			System.out.print(maxSellers.get(i).getName() + " (ID: " + maxSellers.get(i).getId() + ")");
		}
		System.out.println();

		System.out.println("----------------------------------------------------");
		System.out.println("Average Salary by Department:");
		for (Map.Entry<String, List<Double>> entry : deptSalaries.entrySet()) {
			double dTotal = 0.0;
			for (double val : entry.getValue()) {
				dTotal += val;
			}
			double dAvg = dTotal / entry.getValue().size();
			System.out.printf("  %-18s : $%,.2f (count: %d)%n", entry.getKey(), dAvg, entry.getValue().size());
		}
		System.out.println("====================================================");
	}

	// ==========================================
	// HELPER DATABASE / METADATA METHODS
	// ==========================================

	/**
	 * Queries the database directly to fetch all departments.
	 */
	private static List<Department> getAllDepartments() {
		List<Department> list = new ArrayList<>();
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			conn = DB.getConnection();
			st = conn.prepareStatement("SELECT * FROM department ORDER BY Name");
			rs = st.executeQuery();
			while (rs.next()) {
				Department dep = new Department();
				dep.setId(rs.getInt("Id"));
				dep.setName(rs.getString("Name"));
				list.add(dep);
			}
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
		return list;
	}

	/**
	 * Queries the database to find a specific department by ID.
	 */
	private static Department findDepartmentById(int id) {
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			conn = DB.getConnection();
			st = conn.prepareStatement("SELECT * FROM department WHERE Id = ?");
			st.setInt(1, id);
			rs = st.executeQuery();
			if (rs.next()) {
				Department dep = new Department();
				dep.setId(rs.getInt("Id"));
				dep.setName(rs.getString("Name"));
				return dep;
			}
			return null;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

	// ==========================================
	// TABLE FORMATTING UTILITIES
	// ==========================================

	/**
	 * Prints a formatted text table of sellers to the console.
	 */
	private static void printSellersTable(List<Seller> sellers) {
		if (sellers == null || sellers.isEmpty()) {
			System.out.println("No sellers found to display.");
			return;
		}

		// Determine dynamic column widths for professional formatting
		int maxIdLen = 2; // "ID"
		int maxNameLen = 4; // "Name"
		int maxEmailLen = 5; // "Email"
		int maxSalaryLen = 6; // "Salary"
		int maxDepLen = 10; // "Department"

		for (Seller s : sellers) {
			maxIdLen = Math.max(maxIdLen, String.valueOf(s.getId()).length());
			maxNameLen = Math.max(maxNameLen, s.getName() != null ? s.getName().length() : 0);
			maxEmailLen = Math.max(maxEmailLen, s.getEmail() != null ? s.getEmail().length() : 0);

			String salaryStr = s.getBaseSalary() != null ? String.format("%,.2f", s.getBaseSalary()) : "0.00";
			maxSalaryLen = Math.max(maxSalaryLen, salaryStr.length());

			String depName = (s.getDepartment() != null && s.getDepartment().getName() != null)
					? s.getDepartment().getName()
					: "None";
			maxDepLen = Math.max(maxDepLen, depName.length());
		}

		// Print formatted row layout
		String format = "| %-" + maxIdLen + "s | %-" + maxNameLen + "s | %-" + maxEmailLen + "s | %-" + maxSalaryLen + "s | %-" + maxDepLen + "s |%n";

		// Draw horizontal line dividers
		StringBuilder dividerBuilder = new StringBuilder();
		dividerBuilder.append("+");
		for (int i = 0; i < maxIdLen + 2; i++) dividerBuilder.append("-");
		dividerBuilder.append("+");
		for (int i = 0; i < maxNameLen + 2; i++) dividerBuilder.append("-");
		dividerBuilder.append("+");
		for (int i = 0; i < maxEmailLen + 2; i++) dividerBuilder.append("-");
		dividerBuilder.append("+");
		for (int i = 0; i < maxSalaryLen + 2; i++) dividerBuilder.append("-");
		dividerBuilder.append("+");
		for (int i = 0; i < maxDepLen + 2; i++) dividerBuilder.append("-");
		dividerBuilder.append("+");
		String divider = dividerBuilder.toString();

		System.out.println(divider);
		System.out.printf(format, "ID", "Name", "Email", "Salary", "Department");
		System.out.println(divider);
		for (Seller s : sellers) {
			String salaryStr = s.getBaseSalary() != null ? String.format("%,.2f", s.getBaseSalary()) : "0.00";
			String depName = (s.getDepartment() != null && s.getDepartment().getName() != null)
					? s.getDepartment().getName()
					: "None";
			System.out.printf(format,
					String.valueOf(s.getId()),
					s.getName() != null ? s.getName() : "",
					s.getEmail() != null ? s.getEmail() : "",
					salaryStr,
					depName
			);
		}
		System.out.println(divider);
	}

	/**
	 * Prints a formatted text table of departments to the console.
	 */
	private static void printDepartmentsTable(List<Department> departments) {
		if (departments == null || departments.isEmpty()) {
			System.out.println("No departments found to display.");
			return;
		}

		int maxIdLen = 2; // "ID"
		int maxNameLen = 4; // "Name"

		for (Department d : departments) {
			maxIdLen = Math.max(maxIdLen, String.valueOf(d.getId()).length());
			maxNameLen = Math.max(maxNameLen, d.getName() != null ? d.getName().length() : 0);
		}

		String format = "| %-" + maxIdLen + "s | %-" + maxNameLen + "s |%n";

		StringBuilder dividerBuilder = new StringBuilder();
		dividerBuilder.append("+");
		for (int i = 0; i < maxIdLen + 2; i++) dividerBuilder.append("-");
		dividerBuilder.append("+");
		for (int i = 0; i < maxNameLen + 2; i++) dividerBuilder.append("-");
		dividerBuilder.append("+");
		String divider = dividerBuilder.toString();

		System.out.println(divider);
		System.out.printf(format, "ID", "Name");
		System.out.println(divider);
		for (Department d : departments) {
			System.out.printf(format, String.valueOf(d.getId()), d.getName() != null ? d.getName() : "");
		}
		System.out.println(divider);
	}

	// ==========================================
	// INPUT VALIDATION & READING UTILITIES
	// ==========================================

	/**
	 * Reads a non-empty string.
	 */
	private static String readString(Scanner scanner, String prompt) {
		while (true) {
			System.out.print(prompt);
			String input = scanner.nextLine().trim();
			if (!input.isEmpty()) {
				return input;
			}
			System.out.println("✗ Input cannot be empty. Please enter a valid value.");
		}
	}

	/**
	 * Reads an optional string. Returns defaultValue if input is empty.
	 */
	private static String readOptionalString(Scanner scanner, String prompt, String defaultValue) {
		System.out.print(prompt);
		String input = scanner.nextLine().trim();
		if (input.isEmpty()) {
			return defaultValue;
		}
		return input;
	}

	/**
	 * Reads a positive integer (greater than 0).
	 */
	private static int readPositiveInt(Scanner scanner, String prompt) {
		while (true) {
			System.out.print(prompt);
			String input = scanner.nextLine().trim();
			try {
				int value = Integer.parseInt(input);
				if (value >= 0) { // Support 0 for cancellation or specific prompts
					return value;
				}
				System.out.println("✗ Number must be positive.");
			} catch (NumberFormatException e) {
				System.out.println("✗ Invalid integer. Please enter a valid number.");
			}
		}
	}

	/**
	 * Reads a non-negative double (greater than or equal to 0.0).
	 */
	private static double readNonNegativeDouble(Scanner scanner, String prompt) {
		while (true) {
			System.out.print(prompt);
			String input = scanner.nextLine().trim();
			try {
				double value = Double.parseDouble(input);
				if (value >= 0.0) {
					return value;
				}
				System.out.println("✗ Value cannot be negative.");
			} catch (NumberFormatException e) {
				System.out.println("✗ Invalid decimal number. Please enter a valid decimal number.");
			}
		}
	}

	/**
	 * Reads an optional non-negative double. Returns defaultValue if input is empty.
	 */
	private static double readOptionalNonNegativeDouble(Scanner scanner, String prompt, double defaultValue) {
		while (true) {
			System.out.print(prompt);
			String input = scanner.nextLine().trim();
			if (input.isEmpty()) {
				return defaultValue;
			}
			try {
				double value = Double.parseDouble(input);
				if (value >= 0.0) {
					return value;
				}
				System.out.println("✗ Value cannot be negative.");
			} catch (NumberFormatException e) {
				System.out.println("✗ Invalid decimal number. Please enter a valid decimal number.");
			}
		}
	}

	/**
	 * Reads a valid LocalDate in yyyy-MM-dd format.
	 */
	private static LocalDate readLocalDate(Scanner scanner, String prompt) {
		while (true) {
			System.out.print(prompt);
			String input = scanner.nextLine().trim();
			try {
				return LocalDate.parse(input);
			} catch (DateTimeParseException e) {
				System.out.println("✗ Invalid date format. Please use yyyy-MM-dd (e.g., 2023-12-31).");
			}
		}
	}

	/**
	 * Reads an optional LocalDate in yyyy-MM-dd format. Returns defaultValue if input is empty.
	 */
	private static LocalDate readOptionalLocalDate(Scanner scanner, String prompt, LocalDate defaultValue) {
		while (true) {
			System.out.print(prompt);
			String input = scanner.nextLine().trim();
			if (input.isEmpty()) {
				return defaultValue;
			}
			try {
				return LocalDate.parse(input);
			} catch (DateTimeParseException e) {
				System.out.println("✗ Invalid date format. Please use yyyy-MM-dd (e.g., 2023-12-31).");
			}
		}
	}
}
