package hu.tamas.szakdolgozatjava.repository;

import hu.tamas.szakdolgozatjava.model.Category;
import hu.tamas.szakdolgozatjava.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("""
    SELECT e FROM Event e
    WHERE
        (:q IS NULL OR :q = '' OR
            LOWER(e.title) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(e.description) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(e.location) LIKE LOWER(CONCAT('%', :q, '%'))
        )
        AND (:category IS NULL OR e.category = :category)
        AND (:startOfDay IS NULL OR e.startTime >= :startOfDay)
        AND (:endOfDay IS NULL OR e.startTime < :endOfDay)
        AND (:upcoming = false OR e.startTime >= :now)
""")
    List<Event> search(
            @Param("q") String q,
            @Param("category") Category category,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("upcoming") boolean upcoming,
            @Param("now") LocalDateTime now,
            Sort sort
    );

    List<Event> findByCreatedBy_UsernameOrderByStartTimeAsc(String username);

}
