package mate.academy.bookservice.repository;

import java.util.Optional;
import mate.academy.bookservice.model.Role;
import mate.academy.bookservice.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
