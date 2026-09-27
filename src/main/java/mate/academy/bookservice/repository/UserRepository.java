package mate.academy.bookservice.repository;

import java.util.Optional;
import mate.academy.bookservice.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)", nativeQuery = true)
    boolean existsByEmailIncludingDeleted(@Param("email") String email);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);
}
