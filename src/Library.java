import java.time.LocalDate;
import java.util.Scanner;

public class Library {
    private DatabaseHelper databaseHelper;
    private User currentUser;

    public Library(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void startLibrary() {
        if (databaseHelper.isTableEmpty("users")) {
            User admin = new User("admin", "09123456789", "admin", true);
            databaseHelper.addUser(admin);
        }

        while (true) {
            if (currentUser == null) {
                loginMenu();
            } else {
                mainMenu();
            }
        }
    }

    private void loginMenu() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("1. Login");
        System.out.println("2. Exit");

        int ch = scanner.nextInt();

        switch (ch) {
            case 1:
                login();
                break;
            case 2:
                System.out.println("Thank you for using our application. Goodbye");
                scanner.close();
                System.exit(0);
            default:
                System.out.println("Invalid Choice");
                break;
        }
    }

    private void login() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter your username: ");
        String name = scanner.nextLine();

        System.out.println("Enter your password: ");
        String password = scanner.nextLine();

        User user = databaseHelper.getUserAndPassword(name, password);

        if (user != null) {
            currentUser = user;
            System.out.println("Login successful. Welcome " + currentUser.getName());
        }

        else {
            System.out.println("Login failed.");
        }

    }

    private void mainMenu() {
        Scanner scanner = new Scanner(System.in);

        do {
            System.out.println("\n\n\t===================================================================");
            System.out.println("\t\t  L I B R A R Y  M A N A G E M E N T  S Y S T E M");
            System.out.println("\t===================================================================");

            System.out.println("\n\t1. User");
            System.out.println("\n\t2. Admin");
            System.out.println("\n\t3. Exit");
            System.out.print("\n\tChoose an Option: ");

            int ch = scanner.nextInt();
            scanner.nextLine();

            switch (ch) {
                case 1:
                    userSection(scanner);
                    break;

                case 2:
                    if (currentUser.isAdmin()) adminSection(scanner);
                    else System.out.println("You do not have admin privileges");

                    break;

                case 3:
                    System.out.println("\n\n\n\tThank you for using our program!\n\n\n");
                    System.exit(0);
                    break;

                default:
                    System.out.println("\n\tInvalid Choice!");
                    break;
            }

        } while (true);
    }

    private void userSection(Scanner scanner) {
        do {
            System.out.println("\t===================================================================\n" +
                    "\t\t\t      U S E R  M E N U\n" +
                    "\t===================================================================");

            System.out.println("\n\t[1] Borrow Book");
            System.out.println("\t[2] My Borrowed Books");
            System.out.println("\t[3] Return Book/s");
            System.out.println("\t[4] Display Library");
            System.out.println("\t[5] Modify Account");
            System.out.println("\t[6] Exit");
            System.out.print("\n\tEnter your Choice: ");

            int userChoice = scanner.nextInt();
            scanner.nextLine();

            switch (userChoice) {
                case 1:
                    borrowBook();
                    break;
                case 2:
                    break;
                case 3:
                    returnBook();
                    break;
                case 4:
                    databaseHelper.displayBooks();
                    break;
                case 5:
                    break;
                case 6:
                    return;
                default:
                    break;
            }
        } while (true);
    }

    private void adminSection(Scanner scanner) {
        do {
            System.out.println("\t===================================================================\n" +
                    "\t\t\t      A D M I N   M E N U\n" +
                    "\t===================================================================");

            System.out.println("\n\t[1] Add books");
            System.out.println("\t[2] Modify books");
            System.out.println("\t[3] Delete books");
            System.out.println("\t[4] Display all books");
            System.out.println("\t[5] Create user account");
            System.out.println("\t[6] Modify user account");
            System.out.println("\t[7] Delete user account");
            System.out.println("\t[8] Display user accounts");
            System.out.println("\t[9] Back to main menu");
            System.out.print("\n\tChoose an option: ");

            int adminChoice = scanner.nextInt();
            scanner.nextLine();

            switch (adminChoice) {
                case 1:
                    addBook();
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    databaseHelper.displayBooks();
                    break;
                case 5:
                    addUser();
                    break;
                case 6:
                    break;
                case 7:
                    break;
                case 8:
                    databaseHelper.displayUsers();
                    break;
                case 9:
                    return;
                default:
                    break;
            }
        } while (true);
    }

    //Book functions
    public void addBook() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter ISBN: ");
        String id = scanner.nextLine();

        System.out.println("Enter Title: ");
        String title = scanner.nextLine();

        System.out.println("Enter Author: ");
        String author = scanner.nextLine();

        Book newBook = new Book(id, title, author, true, null, null);
        databaseHelper.addBook(newBook);

        System.out.println("Book added to the library: " + newBook.getTitle());
    }

    public Book searchBook() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter Book Title: ");
        String searchCriterion = scanner.nextLine();

        Book foundBook = databaseHelper.getBookByTitle(searchCriterion);

        return foundBook;
    }

    public void borrowBook() {
        Scanner scanner = new Scanner(System.in);
        Book book = searchBook();

        System.out.println("Is this the book you want to borrow?(y/n): ");
        boolean ch = scanner.nextLine().charAt(0) == 'y' ? true : false;

        if (book != null && ch && book.isAvailable()) databaseHelper.borrowBook(currentUser.getId(), book.getId());
        else System.out.println("Book is currently not available");
    }

    public void returnBook() {
        Book book = searchBook();

        if (book != null) {
            databaseHelper.returnBook(currentUser.getId(), book.getId());
        }
    }

    //User functions
    public void addUser() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter Name: ");
        String name = scanner.nextLine();

        System.out.println("Enter Contact Number: ");
        String contact = scanner.nextLine();

        System.out.println("Enter Password: ");
        String password = scanner.nextLine();

        System.out.println("Give Admin Privileges?(y/n): ");
        Boolean isAdmin = scanner.nextLine().charAt(0) == 'y' ? true : false;

        User newUser = new User(name, contact, password, isAdmin);
        databaseHelper.addUser(newUser);

        System.out.println("User added to the library: " + newUser.getName());
    }
}
