package hu.tamas.szakdolgozatjava.repository;

import hu.tamas.szakdolgozatjava.model.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    long countByEvent_Id(Long eventId);

    boolean existsByEvent_IdAndUser_Id(Long eventId, Long userId);

    void deleteByEvent_IdAndUser_Id(Long eventId, Long userId);

    @Query("select er.event.id from EventRegistration er where er.user.id = :userId")
    Set<Long> findRegisteredEventIdsByUserId(@Param("userId") Long userId);
}
