package hu.tamas.szakdolgozatjava.controller;

import hu.tamas.szakdolgozatjava.repository.EventFavoriteRepository;
import hu.tamas.szakdolgozatjava.repository.EventRepository;
import hu.tamas.szakdolgozatjava.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class EventStatsController {

    private final EventRepository eventRepository;
    private final RegistrationService registrationService;
    private final EventFavoriteRepository favoriteRepository;

    public EventStatsController(EventRepository eventRepository,
                                RegistrationService registrationService,
                                EventFavoriteRepository favoriteRepository) {
        this.eventRepository = eventRepository;
        this.registrationService = registrationService;
        this.favoriteRepository = favoriteRepository;
    }

    // ✅ ADMIN mindent, SZERVEZO csak sajátot
    @PreAuthorize("hasRole('ADMIN') or @eventSecurity.isOwner(#id, authentication)")
    @GetMapping("/events/{id}/stats")
    public String eventStats(@PathVariable Long id, Model model) {

        var event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Esemény nem található"));

        long attendees = registrationService.countByEventId(id);
        long favorites = favoriteRepository.countByEvent_Id(id);

        model.addAttribute("event", event);
        model.addAttribute("attendees", attendees);
        model.addAttribute("favorites", favorites);

        return "events/stats";
    }
}
