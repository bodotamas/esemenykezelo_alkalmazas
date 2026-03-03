package hu.tamas.szakdolgozatjava.service;

import hu.tamas.szakdolgozatjava.model.Event;
import hu.tamas.szakdolgozatjava.repository.EventFavoriteRepository;
import hu.tamas.szakdolgozatjava.repository.EventRegistrationRepository;
import hu.tamas.szakdolgozatjava.repository.EventRepository;
import hu.tamas.szakdolgozatjava.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventFavoriteRepository eventFavoriteRepository;

    public AdminUserService(
            UserRepository userRepository,
            EventRepository eventRepository,
            EventRegistrationRepository eventRegistrationRepository,
            EventFavoriteRepository eventFavoriteRepository
    ) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.eventFavoriteRepository = eventFavoriteRepository;
    }

    @Transactional
    public void deleteUserHard(Long userId) {
        if (!userRepository.existsById(userId)) return;

        List<Event> createdEvents = eventRepository.findByCreatedBy_Id(userId);
        for (Event e : createdEvents) {
            Long eventId = e.getId();
            eventRegistrationRepository.deleteByEvent_Id(eventId);
            eventFavoriteRepository.deleteByEvent_Id(eventId);
            eventRepository.deleteById(eventId);
        }

        eventRegistrationRepository.deleteByUser_Id(userId);
        eventFavoriteRepository.deleteByUser_Id(userId);

        userRepository.deleteById(userId);
    }
}
