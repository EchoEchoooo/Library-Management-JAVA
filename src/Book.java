import java.time.LocalDate;

public class Book {
    private String id;
    private String title;
    private String author;
    private boolean isAvailable;
    private LocalDate borrowDate;
    private LocalDate returnDate;

    public Book(String id, String title, String author, boolean isAvailable, LocalDate borrowDate, LocalDate returnDate) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isAvailable = isAvailable;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String tile) {
        this.title = tile;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public boolean hasBorrowDate() {
        return borrowDate != null;
    }

    public boolean hasReturnDate() {
        return returnDate != null;
    }
}
