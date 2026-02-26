package hu.tamas.szakdolgozatjava.service;

import hu.tamas.szakdolgozatjava.model.Event;
import hu.tamas.szakdolgozatjava.model.EventRegistration;
import hu.tamas.szakdolgozatjava.model.RegistrationStatus;
import hu.tamas.szakdolgozatjava.repository.EventRegistrationRepository;
import hu.tamas.szakdolgozatjava.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class RegistrationService {

    private final EventRegistrationRepository repo;
    private final EventRepository eventRepository;

    public RegistrationService(EventRegistrationRepository repo, EventRepository eventRepository) {
        this.repo = repo;
        this.eventRepository = eventRepository;
    }

    public long countByEventId(Long eventId) {
        return repo.countByEvent_Id(eventId);
    }

    public long countConfirmedByEventId(Long eventId) {
        return repo.countByEvent_IdAndStatus(eventId, RegistrationStatus.CONFIRMED);
    }

    public long countWaitlistedByEventId(Long eventId) {
        return repo.countByEvent_IdAndStatus(eventId, RegistrationStatus.WAITLISTED);
    }

    public Set<Long> registeredEventIdsForUser(Long userId) {
        return repo.findRegisteredEventIdsByUserId(userId);
    }

    /**
     * Regisztráció:
     * - ha van capacity és betelt => WAITLISTED
     * - különben CONFIRMED
     */
    @Transactional
    public RegistrationStatus register(Long eventId, Long userId) {
        if (repo.existsByEvent_IdAndUser_Id(eventId, userId)) {
            // már van (confirmed vagy waitlist)
            return repo.findByEvent_IdAndUser_Id(eventId, userId).map(EventRegistration::getStatus)
                    .orElse(RegistrationStatus.CONFIRMED);
        }

        Event event = eventRepository.findById(eventId).orElseThrow();

        long confirmed = repo.countByEvent_IdAndStatus(eventId, RegistrationStatus.CONFIRMED);
        Integer cap = event.getCapacity();

        RegistrationStatus status;
        if (cap != null && cap > 0 && confirmed >= cap) {
            status = RegistrationStatus.WAITLISTED;
        } else {
            status = RegistrationStatus.CONFIRMED;
        }

        EventRegistration er = new EventRegistration();

        // csak id-val “rámutatunk”
        var eRef = new Event();
        eRef.setIdForRegistration(eventId);

        var uRef = new hu.tamas.szakdolgozatjava.model.User();
        uRef.setId(userId);

        er.setEvent(eRef);
        er.setUser(uRef);
        er.setStatus(status);

        repo.save(er);
        return status;
    }

    /**
     * Lemondás:
     * - ha CONFIRMED törlődik és volt várólista => a legrégebbi WAITLISTED -> CONFIRMED
     */
    @Transactional
    public void unregister(Long eventId, Long userId) {
        var existingOpt = repo.findByEvent_IdAndUser_Id(eventId, userId);
        if (existingOpt.isEmpty()) return;

        var existing = existingOpt.get();
        RegistrationStatus removedStatus = existing.getStatus();

        repo.delete(existing);

        if (removedStatus == RegistrationStatus.CONFIRMED) {
            // próbálunk felmozgatni
            var nextOpt = repo.findFirstByEvent_IdAndStatusOrderByRegisteredAtAsc(eventId, RegistrationStatus.WAITLISTED);
            if (nextOpt.isPresent()) {
                var next = nextOpt.get();
                next.setStatus(RegistrationStatus.CONFIRMED);
                repo.save(next);
            }
        }
    }
}