import org.library.model.Book;
import org.library.service.BookService;
import org.library.util.Result;
import org.library.model.User;
import org.library.service.LibraryService;
import org.library.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LibraryWorkflowTest {

    public static void main(String[] args) {
        UserService userService = new UserService();
        BookService bookService = new BookService();
        LibraryService libraryService = new LibraryService(userService, bookService);

        libraryService.getBooks().addAll(initializeBooks());
        initializeUsers(userService);

        testLoginUser(userService);
        testLogoutUser(userService);
        testBorrowBook(libraryService, userService);
        testReturnBook(libraryService, userService);

    }


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static void testReturnBook(LibraryService libraryService, UserService userService) {
        System.out.println("\n=== TEST: Return Book ===");

        User anna = userService.getUserByEmail("anna.kowalska@example.com").get();
        Book book1 = libraryService.getBooks().getFirst();

//        Scenariusz 1: Niezalogowany użytkownik próbuje oddać książkę
        anna.setLoggedIn(false);
        Result result1 = libraryService.returnBook(anna, book1);
        System.out.println("1. " + result1.getMessage());

//        Scenariusz 2: Poprawne zwrócenie książki
        anna.setLoggedIn(true);
        Result result2 = libraryService.returnBook(anna, book1);
        System.out.println("2. " + result2.getMessage());

//        Scenariusz 3: Zwrot książki, której użytkownik nie wypożyczył
        Result result3 = libraryService.returnBook(anna, libraryService.getBooks().get(8));
        System.out.println("3. " + result3.getMessage());

//        Scenariusz 4: Zwrot książki, która nie istnieje w systemie
        Result result4 = libraryService.returnBook(
                anna,
                new Book("Random Tittle", "Random Author", 1294, "Random Publisher")
        );
        System.out.println("4. " + result4.getMessage());

//        Scenariusz 5: Zwrot książki przez nieistniejącego użytkownika
        Optional<User> random = userService.getUserByEmail("random@email.com");
        if (random.isEmpty()) {
            System.out.println("5. User not found, cannot return book");
        } else {
            Result result5 = libraryService.returnBook(random.get(), libraryService.getBooks().getLast());
            System.out.println("5. " + result5.getMessage());
        }

//        Scenariusz 6: Zwrot książki, która jest już dostępna
        Result result6 = libraryService.returnBook(anna, libraryService.getBooks().getFirst());
        System.out.println("6. " + result6.getMessage());

//        Scenariusz 7: Zwracanie wielu książek po kolei
        System.out.println("7. Returning few books");
        System.out.printf
                ("Amount of borrowed books before returned any book by user: %s = %d%n",
                        anna.getFullName(), anna.getBorrowedBooks().size());
        for (int i = 0; i < 3; i++) {
            Result result = libraryService.returnBook(anna, anna.getBorrowedBooks().getFirst());
            System.out.println(result.getMessage());
            System.out.printf
                    ("Amount of borrowed books by user: %s = %d%n", anna.getFullName(), anna.getBorrowedBooks().size());
        }

//        Scenariusz 8: Książka jako null
        Result result8 = libraryService.returnBook(anna, null);
        System.out.println("8. " + result8.getMessage());

//        Scenariusz 9: Użytkownik jako null
        Result result9 = libraryService.returnBook(null, book1);
        System.out.println("9. " + result9.getMessage());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static void testBorrowBook(LibraryService libraryService, UserService userService) {
        System.out.println("\n=== TEST: Borrow Book ===");

        User anna = userService.getUserByEmail("anna.kowalska@example.com").get();
        Book book1 = libraryService.getBooks().getFirst();

//        Scenariusz 1: Poprawne wypożyczenie
        anna.setLoggedIn(true);
        Result result1 = libraryService.borrowBook(anna, book1);
        System.out.println("1. " + result1.getMessage());

//        Scenariusz 2: Ta sama książka już wypożyczona
        Result result2 = libraryService.borrowBook(anna, book1);
        System.out.println("2. " + result2.getMessage());

//        Scenariusz 3: Przekroczony limit
        for (int i = 1; i <= 5; i++) {
            libraryService.borrowBook(anna, libraryService.getBooks().get(i));
        }
        Result result3 = libraryService.borrowBook(anna, libraryService.getBooks().get(6));
        System.out.println("3. " + result3.getMessage());

//        Scenariusz 4: Próba wypożyczenia książki nieistniejącej w systemie
        Result result4 = libraryService.borrowBook(
                anna,
                new Book("Random Tittle", "Random Author", 1294, "Random Publisher")
        );
        System.out.println("4. " + result4.getMessage());

//        Scenariusz 5: Próba wypożyczenia książki przez nieistniejącego użytkownika
        Optional<User> random = userService.getUserByEmail("random@email.com");
        if (random.isEmpty()) {
            System.out.println("5. User not found, cannot borrow book.");
        } else {
            Result result5 = libraryService.borrowBook(random.get(), libraryService.getBooks().getLast());
            System.out.println("5. " + result5.getMessage());
        }
//        Scenariusz 6: Użytkownik jako null
        Result result6 = libraryService.borrowBook(null, book1);
        System.out.println("6. " + result6.getMessage());

//        Scenariusz 7: Książka jako null
        Result result7 = libraryService.borrowBook(anna, null);
        System.out.println("7. " + result7.getMessage());
    }

    private static void testLogoutUser(UserService userService) {
        System.out.println("\n=== TEST: Logout User ===");

        String[] testEmails = {
                "john.doe@example.com",
                "invalid-email",
                "nonexistent@example.com"
        };

        for (String email : testEmails) {
            Optional<User> optionalUser = userService.getUserByEmail(email);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                Result result = userService.logoutUser(user);
                System.out.println(result.getMessage());
            } else {
                System.out.println("Not user found with email: " + email);
            }
        }
    }

    private static void testLoginUser(UserService userService) {
        System.out.println("\n=== TEST: Login User ===");

//        Scenariusz 1: Poprawne logowanie
        performLogin(userService, "john.doe@example.com", "password123", 1);
//        Scenariusz 2: Złe Hasło
        performLogin(userService, "john.doe@example.com", "wrongPass!", 2);
//        Scenariusz 3: Nieistniejący użytkownik
        performLogin(userService, "john.doe1234@example.com", "anything1234", 3);
//        Scenariusz 4: Email niezawierający znaku @
        performLogin(userService, "john.doe1234example.com", "anything1234", 4);
//        Scenariusz 5: Ponowne logowanie użytkownika
        performLogin(userService, "john.doe@example.com", "password123", 5);
    }

    private static void performLogin(UserService userService, String email, String password, int scenarioNumber) {
        Result result = userService.loginUser(email, password);
        System.out.println(scenarioNumber + ". " + result.getMessage());
    }

    private static void initializeUsers(UserService userService) {
        Result user1 =
                userService.registerUser("john.doe@example.com", "John Doe", "password123");
        System.out.println(user1.getMessage());
        Result user2 =
                userService.registerUser("anna.kowalska@example.com", "Anna Kowalska", "securePass1");
        System.out.println(user2.getMessage());
        Result user3 =
                userService.registerUser("mike.smith@example.com", "Mike Smith", "javaRocks9");
        System.out.println(user3.getMessage());
        Result user4 =
                userService.registerUser("ela.nowak@example.com", "Ela Nowak", "Pa$$word88");
        System.out.println(user4.getMessage());
    }

    private static List<Book> initializeBooks() {
        List<Book> books = new ArrayList<>(List.of(
                new Book("Clean Code", "Robert C. Martin", 2008, "Prentice Hall"),
                new Book("Effective Java", "Joshua Bloch", 2018, "Addison-Wesley"),
                new Book("The Pragmatic Programmer", "Andrew Hunt", 1999, "Addison-Wesley"),
                new Book("1984", "George Orwell", 1949, "Secker & Warburg"),
                new Book("To Kill a Mockingbird", "Harper Lee", 1960, "J.B. Lippincott & Co."),
                new Book("The Hobbit", "J.R.R. Tolkien", 1937, "George Allen & Unwin"),
                new Book("Java: The Complete Reference", "Herbert Schildt", 2018, "McGraw-Hill"),
                new Book("Design Patterns", "Erich Gamma", 1994, "Addison-Wesley"),
                new Book("Crime and Punishment", "Fyodor Dostoevsky", 1866, "The Russian Messenger"),
                new Book("Thinking in Java", "Bruce Eckel", 2006, "Prentice Hall"),
                new Book("The Catcher in the Rye", "J.D. Salinger", 1951, "Little, Brown and Company"),
                new Book("Sapiens: A Brief History of Humankind", "Yuval Noah Harari", 2011, "Harvill Secker")
        ));
        System.out.println("Books initialized: " + books.size());
        return books;
    }
}