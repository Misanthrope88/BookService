package mate.academy.bookservice;

import java.math.BigDecimal;
import mate.academy.bookservice.book.model.Book;
import mate.academy.bookservice.book.service.BookService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BookServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(BookService bookService) {
        return args -> {
            Book book = new Book();
            book.setTitle("Effective Java");
            book.setAuthor("Joshua Bloch");
            book.setIsbn("9780134685991");
            book.setPrice(BigDecimal.valueOf(45.99));
            book.setDescription("Java best practices");
            book.setCoverImage("effective-java.jpg");

            bookService.save(book);

            bookService.findAll().forEach(System.out::println);
        };
    }
}
