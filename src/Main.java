public class Main {
    public static void main(String[] args) {
        DatabaseHelper databaseHelper = new DatabaseHelper("library.db");
        Library library = new Library(databaseHelper);

        library.startLibrary();
    }
}