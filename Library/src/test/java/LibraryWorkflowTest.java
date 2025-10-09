import org.library.model.Book;
import org.library.model.Result;
import org.library.model.User;
import org.library.service.Library;
import org.library.service.UserManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LibraryWorkflowTest {

    public static void main(String[] args) {
        UserManager userManager = new UserManager();
        Library library = new Library(initializeBooks(), userManager);
        initializeUsers(userManager);

        testLoginUser(userManager);
        testLogoutUser(userManager);
        testBorrowBook(library, userManager);
        testReturnBook(library, userManager);

    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static void testReturnBook(Library library, UserManager userManager) {
        System.out.println("\n=== TEST: Borrow Book ===");

        User anna = userManager.getUserByEmail("anna.kowalska@example.com").get();
        Book book1 = anna.getBorrowedBooks().getFirst();

//        Scenariusz 1: Niezalogowany użytkownik próbuje oddać książkę
        anna.setLoggedIn(false);
        Result result1 = library.returnBook(anna, library.getBooks().getLast());
        System.out.println("1. " + result1.getMessage());

//        Scenariusz 2: Poprawne zwrócenie książki
        anna.setLoggedIn(true);
        Result result2 = library.returnBook(anna, book1);
        System.out.println("2. " + result2.getMessage());

//        Scenariusz 3: Zwrot książki, której użytkownik nie wypożyczył
        Result result3 = library.returnBook(anna, library.getBooks().get(8));
        System.out.println("3. " + result3.getMessage());

//        Scenariusz 4: Zwrot książki, która nie istnieje w systemie
        Result result4 = library.returnBook(
                anna,
                new Book(999, "Random Tittle", "Random Author", 1294, "Random Publisher")
        );
        System.out.println("4. " + result4.getMessage());

//        Scenariusz 5: Zwrot książki przez nieistniejącego użytkownika
        Optional<User> random = userManager.getUserByEmail("random@email.com");
        if (random.isEmpty()) {
            System.out.println("5. User not found, cannot return book");
        } else {
            Result result5 = library.returnBook(random.get(), library.getBooks().getLast());
            System.out.println("5. " + result5.getMessage());
        }

//        Scenariusz 6: Zwrot książki, która jest już dostępna
        Result result6 = library.returnBook(anna, library.getBooks().getFirst());
        System.out.println("6. " + result6.getMessage());

//        Scenariusz 7: Zwracanie wielu książek po kolei
        System.out.println("7. Returning few books");
        System.out.printf
                ("Amount of borrowed books before returned any book by user: %s = %d%n", anna.getFullName(), anna.getBorrowedBooks().size());
        for (int i = 0; i < 3; i++) {
            Result result = library.returnBook(anna, anna.getBorrowedBooks().getFirst());
            System.out.println(result.getMessage());
            System.out.printf
                    ("Amount of borrowed books by user: %s = %d%n", anna.getFullName(), anna.getBorrowedBooks().size());
        }

//        Scenariusz 8: Zwrot książki, która jest już dostępna
        Result result8 = library.returnBook(anna, null);
        System.out.println("8. " + result8.getMessage());

//        Scenariusz 9: Zwrot książki, która jest już dostępna
        Result result9 = library.returnBook(null, book1);
        System.out.println("9. " + result9.getMessage());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static void testBorrowBook(Library library, UserManager userManager) {
        System.out.println("\n=== TEST: Borrow Book ===");

        User anna = userManager.getUserByEmail("anna.kowalska@example.com").get();
        Book book1 = library.getBooks().getFirst();

//        Scenariusz 1: Poprawne wypożyczenie
        Result result1 = library.borrowBook(anna, book1);
        System.out.println("1. " + result1.getMessage());

//        Scenariusz 2: Ta sama książka już wypożyczona
        Result result2 = library.borrowBook(anna, book1);
        System.out.println("2. " + result2.getMessage());

//        Scenariusz 3: Przekroczony limit
        for (int i = 1; i <= 5; i++) {
            library.borrowBook(anna, library.getBooks().get(i));
        }
        Result result3 = library.borrowBook(anna, library.getBooks().get(6));
        System.out.println("3. " + result3.getMessage());

//        Scenariusz 4: Próba wypożyczenia książki nieistniejącej w systemie
        Result result4 = library.borrowBook(
                anna,
                new Book(999, "Random Tittle", "Random Author", 1294, "Random Publisher")
        );
        System.out.println("4. " + result4.getMessage());

//        Scenariusz 5: Próba wypożyczenia książki przez nieistniejącego użytkownika
        Optional<User> random = userManager.getUserByEmail("random@email.com");
        if (random.isEmpty()) {
            System.out.println("5. User not found, cannot borrow book");
        } else {
            Result result5 = library.borrowBook(random.get(), library.getBooks().getLast());
            System.out.println("5. " + result5.getMessage());
        }
    }

    private static void testLogoutUser(UserManager userManager) {
        System.out.println("\n=== TEST: Logout User ===");

        String[] testEmails = {
                "john.doe@example.com",
                "invalid-email",
                "nonexistent@example.com"
        };

        for (String email : testEmails) {
            Optional<User> optionalUser = userManager.getUserByEmail(email);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                Result result = userManager.logoutUser(user);
                System.out.println(result.getMessage());
            } else {
                System.out.println("Not user found with email: " + email);
            }
        }
    }

    private static void testLoginUser(UserManager userManager) {
        System.out.println("\n=== TEST: Login User ===");

//        Scenariusz 1: Poprawne logowanie
        performLogin(userManager, "john.doe@example.com", "password123", 1);
//        Scenariusz 2: Złe Hasło
        performLogin(userManager, "john.doe@example.com", "wrongPass!", 2);
//        Scenariusz 3: Nieistniejący użytkownik
        performLogin(userManager, "john.doe1234@example.com", "anything1234", 3);
//        Scenariusz 4: Email niezawierający znaku @
        performLogin(userManager, "john.doe1234example.com", "anything1234", 4);
//        Scenariusz 5: Ponowne logowanie użytkownika
        performLogin(userManager, "john.doe@example.com", "password123", 5);
    }

    private static void performLogin(UserManager userManager, String email, String password, int scenarioNumber) {
        Result result = userManager.loginUser(email, password);
        System.out.println(scenarioNumber + ". " + result.getMessage());
    }

    private static void initializeUsers(UserManager userManager) {
        Result user1 =
                userManager.registerUser("john.doe@example.com", "John Doe", "password123");
        System.out.println(user1.getMessage());
        Result user2 =
                userManager.registerUser("anna.kowalska@example.com", "Anna Kowalska", "securePass1");
        System.out.println(user2.getMessage());
        Result user3 =
                userManager.registerUser("mike.smith@example.com", "Mike Smith", "javaRocks9");
        System.out.println(user3.getMessage());
        Result user4 =
                userManager.registerUser("ela.nowak@example.com", "Ela Nowak", "Pa$$word88");
        System.out.println(user4.getMessage());
    }

    private static List<Book> initializeBooks() {
        List<Book> books = new ArrayList<>(List.of(
                new Book(1, "Clean Code", "Robert C. Martin", 2008, "Prentice Hall"),
                new Book(2, "Effective Java", "Joshua Bloch", 2018, "Addison-Wesley"),
                new Book(3, "The Pragmatic Programmer", "Andrew Hunt", 1999, "Addison-Wesley"),
                new Book(4, "1984", "George Orwell", 1949, "Secker & Warburg"),
                new Book(5, "To Kill a Mockingbird", "Harper Lee", 1960, "J.B. Lippincott & Co."),
                new Book(6, "The Hobbit", "J.R.R. Tolkien", 1937, "George Allen & Unwin"),
                new Book(7, "Java: The Complete Reference", "Herbert Schildt", 2018, "McGraw-Hill"),
                new Book(8, "Design Patterns", "Erich Gamma", 1994, "Addison-Wesley"),
                new Book(9, "Crime and Punishment", "Fyodor Dostoevsky", 1866, "The Russian Messenger"),
                new Book(10, "Thinking in Java", "Bruce Eckel", 2006, "Prentice Hall"),
                new Book(11, "The Catcher in the Rye", "J.D. Salinger", 1951, "Little, Brown and Company"),
                new Book(12, "Sapiens: A Brief History of Humankind", "Yuval Noah Harari", 2011, "Harvill Secker")
        ));
        System.out.println("Books initialized: " + books.size());
        return books;
    }
}