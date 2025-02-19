package miiiiiin.com.vinyler.application.repository;

import miiiiiin.com.vinyler.application.entity.Vinyl;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VinylRepository extends JpaRepository<Vinyl, Long> {
    List<Vinyl> findByUser(User user);
    Optional<Vinyl> findByVinylId(Long vinylId);
}
