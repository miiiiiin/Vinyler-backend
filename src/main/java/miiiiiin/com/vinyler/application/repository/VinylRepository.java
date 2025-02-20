package miiiiiin.com.vinyler.application.repository;

import miiiiiin.com.vinyler.application.entity.Vinyl;
import miiiiiin.com.vinyler.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VinylRepository extends JpaRepository<Vinyl, Long> {
    List<Vinyl> findByUser(User user);
    Optional<Vinyl> findByDiscogsId(Long discogsId);
}
