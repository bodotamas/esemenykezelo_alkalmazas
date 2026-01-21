package hu.tamas.szakdolgozatjava.security;

import hu.tamas.szakdolgozatjava.repository.EventRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("eventSecurity")
public class EventSecurity {

    private final EventRepository eventRepository;

    public EventSecurity(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public boolean isOwner(Long eventId, Authentication authentication) {
        var username = authentication.getName();
        var event = eventRepository.findById(eventId).orElse(null);
        if (event == null || event.getCreatedBy() == null) return false;
        return username.equals(event.getCreatedBy().getUsername());
    }
}
