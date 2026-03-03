package hu.tamas.szakdolgozatjava.repository;

import hu.tamas.szakdolgozatjava.model.EventFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface EventFavoriteRepository extends JpaRepository<EventFavorite, Long> {

    boolean existsByUser_IdAndEvent_Id(Long userId, Long eventId);

    void deleteByUser_IdAndEvent_Id(Long userId, Long eventId);

    void deleteByUser_Id(Long userId);

    void deleteByEvent_Id(Long eventId);

    @Query("select f.event.id from EventFavorite f where f.user.id = :userId")
    Set<Long> findFavoriteEventIdsByUserId(@Param("userId") Long userId);

    long countByEvent_Id(Long eventId);
}
