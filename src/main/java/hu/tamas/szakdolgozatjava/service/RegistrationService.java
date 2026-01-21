package hu.tamas.szakdolgozatjava.service;

import hu.tamas.szakdolgozatjava.model.EventRegistration;
import hu.tamas.szakdolgozatjava.repository.EventRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class RegistrationService {

    private final EventRegistrationRepository repo;

    public RegistrationService(EventRegistrationRepository repo) {
        this.repo = repo;
    }

    public long countByEventId(Long eventId) {
        return repo.countByEvent_Id(eventId);
    }

    public Set<Long> registeredEventIdsForUser(Long userId) {
        return repo.findRegisteredEventIdsByUserId(userId);
    }

    @Transactional
    public void register(Long eventId, Long userId) {
        if (repo.existsByEvent_IdAndUser_Id(eventId, userId)) return;

        EventRegistration er = new EventRegistration();
        // csak id-val “rámutatunk”, nem kell külön lekérdezni az egész Event/User objektumot
        var eRef = new hu.tamas.szakdolgozatjava.model.Event();
        eRef.setIdForRegistration(eventId);

        var uRef = new hu.tamas.szakdolgozatjava.model.User();
        uRef.setId(userId);

        er.setEvent(eRef);
        er.setUser(uRef);

        repo.save(er);
    }

    @Transactional
    public void unregister(Long eventId, Long userId) {
        repo.deleteByEvent_IdAndUser_Id(eventId, userId);
    }
}
