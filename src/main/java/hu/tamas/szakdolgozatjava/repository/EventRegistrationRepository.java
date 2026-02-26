package hu.tamas.szakdolgozatjava.repository;

import hu.tamas.szakdolgozatjava.model.EventRegistration;
import hu.tamas.szakdolgozatjava.model.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    long countByEvent_Id(Long eventId);

    long countByEvent_IdAndStatus(Long eventId, RegistrationStatus status);

    boolean existsByEvent_IdAndUser_Id(Long eventId, Long userId);

    Optional<EventRegistration> findByEvent_IdAndUser_Id(Long eventId, Long userId);

    void deleteByEvent_IdAndUser_Id(Long eventId, Long userId);

    void deleteByUser_Id(Long userId);

    void deleteByEvent_Id(Long eventId);

    Optional<EventRegistration> findFirstByEvent_IdAndStatusOrderByRegisteredAtAsc(Long eventId, RegistrationStatus status);

    @Query("select er.event.id from EventRegistration er where er.user.id = :userId")
    Set<Long> findRegisteredEventIdsByUserId(@Param("userId") Long userId);
}