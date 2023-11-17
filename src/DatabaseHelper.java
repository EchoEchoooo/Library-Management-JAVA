import java.sql.*;
import java.time.LocalDate;

public class DatabaseHelper {
    private Connection connection;

    //Database Functions
    public DatabaseHelper(String dbName) {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbName);
            System.out.println("Connection to database established");
            createTables();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        try (Statement statement = connection.createStatement()) {
            String createBooksTable = "CREATE TABLE IF NOT EXISTS books " +
                    "(id TEXT PRIMARY KEY," +
                    "title TEXT NOT NULL," +
                    "author TEXT NOT NULL," +
                    "is_available BOOLEAN NOT NULL," +
                    "borrow_date DATE," +
                    "return_date DATE)";
            statement.executeUpdate(createBooksTable);

            String createUsersTable = "CREATE TABLE IF NOT EXISTS users " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "contact TEXT NOT NULL," +
                    "password TEXT NOT NULL," +
                    "is_admin BOOLEAN NOT NULL)";
            statement.executeUpdate(createUsersTable);

            String createUserBooksTable = "CREATE TABLE IF NOT EXISTS userbooks " +
                    "(user_id INTEGER," +
                    "book_id TEXT," +
                    "PRIMARY KEY (user_id, book_id)," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)," +
                    "FOREIGN KEY (book_id) REFERENCES books(id))";
            statement.executeUpdate(createUserBooksTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isTableEmpty(String tableName) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM " + tableName);
        ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                int rowCount = resultSet.getInt(1);
                return rowCount == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    public User getUserAndPassword(String name, String password) {
        User user = null;

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE name = ? AND password = ?")) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String storedName = resultSet.getString("name");
                    String contact = resultSet.getString("contact");
                    String storedPassword = resultSet.getString("password");
                    boolean isAdmin = resultSet.getBoolean("is_admin");

                    user = new User(storedName, contact, storedPassword, isAdmin);
                    user.setId(id);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    public void closeConnection() {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Book Functions
    public void addBook(Book book) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO books (id, title, author, is_available) VALUES (?, ?, ?, ?)")) {
            preparedStatement.setString(1, book.getId());
            preparedStatement.setString(2, book.getTitle());
            preparedStatement.setString(3, book.getAuthor());
            preparedStatement.setBoolean(4, book.isAvailable());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void editRecord(String tableName, String colName, Object newValue, Object recordId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + tableName + " SET " + colName + " = ? WHERE id = ?")) {
            preparedStatement.setObject(1, newValue);
            preparedStatement.setObject(2, recordId);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) System.out.println(tableName + " record with ID " + recordId + " edited successfully");
            else System.out.println(tableName + " record with ID " + recordId + " not found or not edited");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteBook(Book book) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM books WHERE id = ?")) {
            preparedStatement.setString(1, book.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void borrowBook(int userId, String bookId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE books SET is_available = 0, borrow_date = ?, return_date = ? WHERE id = ?")) {
            preparedStatement.setString(1, LocalDate.now().toString());
            preparedStatement.setString(2, LocalDate.now().plusDays(14).toString());
            preparedStatement.setString(3, bookId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO userbooks (user_id, book_id) VALUES (?, ?)")) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, bookId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void returnBook(int userId, String bookId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE books SET is_available = 1, borrow_date = NULL, return_date = NULL WHERE id = ?")) {
            preparedStatement.setString(1, bookId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM userbooks WHERE user_id = ? AND book_id = ?")) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, bookId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void displayBooks() {
        try (Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM books")) {
            System.out.println("Library Books:");
            System.out.printf("%-30s %-30s %-20s %-12s %-12s %-12s\n", "ID", "Title", "Author", "Available", "Borrow Date", "Return Date");
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                boolean isAvailable = resultSet.getBoolean("is_available");
                String borrowDate = resultSet.getString("borrow_date");
                String returnDate = resultSet.getString("return_date");

                System.out.printf("%-30s %-30s %-20s %-12b %-12s %-12s\n",
                        id, title, author, isAvailable, borrowDate, returnDate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Book getBookByCriterion(String searchCriterion, String sqlQuery) {
        Book book = null;

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, "%" + searchCriterion + "%");

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if(resultSet.next()) {
                    String id = resultSet.getString("id");
                    String title = resultSet.getString("title");
                    String author = resultSet.getString("author");
                    boolean isAvailable = resultSet.getBoolean("is_available");
                    String borrowDate = getLocalDateorNull(resultSet, "borrow_date");
                    String returnDate = getLocalDateorNull(resultSet, "return_date");

                    book = new Book(id, title, author, isAvailable, null, null);

                    System.out.printf("%-30s %-30s %-20s %-12s %-12s %-12s\n", "ID", "Title", "Author", "Available", "Borrow Date", "Return Date");
                    System.out.printf("%-30s %-30s %-20s %-12b %-12s %-12s\n",
                            id, title, author, isAvailable, formatLocalDate(borrowDate), formatLocalDate(returnDate));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (book == null) System.out.println("Book not found");

        return book;
    }

    public Book getBookByTitle(String title) {
        String sqlQuery = "SELECT * FROM books WHERE title LIKE ?";
        return getBookByCriterion(title, sqlQuery);
    }

    public Book getBookByAuthor(String author) {
        String sqlQuery = "SELECT * FROM books WHERE author LIKE ?";
        return getBookByCriterion(author, sqlQuery);
    }

    public Book getBookById(String bookId) {
        String sqlQuery = "SELECT * FROM books WHERE id LIKE ?";
        return getBookByCriterion(bookId, sqlQuery);
    }

    private String getLocalDateorNull(ResultSet resultSet, String dateColumn) throws SQLException {
        String date = resultSet.getString(dateColumn);
        return (date != null) ? date : null;
    }
    private String formatLocalDate(String localDate) {
        return localDate != null ? localDate.toString() : "N/A";
    }

    public void getBorrowedBooks(int userId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM userbooks WHERE user_id = ?")) {
            preparedStatement.setInt(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()){
                if (resultSet.isBeforeFirst()) {
                    System.out.println("Borrowed Books for User with ID " + userId + ":");
                    while (resultSet.next()) {
                        String bookId = resultSet.getString("book_id");
                        System.out.printf("%-30s %-12s %-12s\n", "Title", "Borrow Date", "Return Date");
                        displayBookDetails(bookId);
                    }
                }
                else System.out.println("No borrowed books yet");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void displayBookDetails(String bookId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT title, borrow_date, return_date FROM books WHERE id LIKE ?")) {
            preparedStatement.setString(1, bookId);

            try (ResultSet resultSet = preparedStatement.executeQuery()){
                if (resultSet.next()) {
                    String title = resultSet.getString("title");
                    String borrow_date = getLocalDateorNull(resultSet, "borrow_date");
                    String returnDate = getLocalDateorNull(resultSet, "return_date");

                    System.out.printf("%-30s %-12s %-12s\n", title, borrow_date, returnDate);
                }
                else System.out.println("Book with ID " + bookId + " not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //User Functions
    public void addUser(User user) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users (name, contact, password, is_admin) VALUES (?, ?, ?, ?)")) {
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getContact());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.setBoolean(4, user.isAdmin());

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                try (Statement statement = connection.createStatement()) {
                    try (ResultSet resultSet = statement.executeQuery("SELECT last_insert_rowid()")) {
                        if (resultSet.next()) {
                            int userId = resultSet.getInt(1);
                            System.out.println("User ID generated: " + userId);
                        } else {
                            System.out.println("No user ID generated");
                        }
                    }
                }
            } else {
                System.out.println("No users inserted");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteUser(User user) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM users WHERE name LIKE ?")) {
            preparedStatement.setString(1, user.getName());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User searchUser(String name) {
        User user = null;

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE name LIKE ?")) {
            preparedStatement.setString(1, name);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String storedName = resultSet.getString("name");
                    String contact = resultSet.getString("contact");
                    String password = resultSet.getString("password");
                    boolean isAdmin = resultSet.getBoolean("is_admin");

                    user = new User(storedName, contact, password, isAdmin);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (user == null) System.out.println("User not found");

        return user;
    }

    public User searchUser(int id) {
        User user = null;

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String storedName = resultSet.getString("name");
                    String contact = resultSet.getString("contact");
                    String password = resultSet.getString("password");
                    boolean isAdmin = resultSet.getBoolean("is_admin");

                    user = new User(storedName, contact, password, isAdmin);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (user == null) System.out.println("User not found");

        return user;
    }

    public void displayUsers() {
        try (Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM users")) {
            System.out.println("Library Users:");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String contact = resultSet.getString("contact");
                String password = resultSet.getString("password");
                boolean isAdmin = resultSet.getBoolean("is_admin");

                System.out.println(id + " | " + name + " | " + contact + " | " + password + " | " + (isAdmin ? "Admin" : "User"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
