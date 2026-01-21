package hu.tamas.szakdolgozatjava.controller;

import hu.tamas.szakdolgozatjava.repository.EventRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MyEventStatsController {

    private final EventRepository eventRepository;

    public MyEventStatsController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * SZERVEZO: saját események
     * ADMIN: összes esemény
     */
    @PreAuthorize("hasAnyRole('ADMIN','SZERVEZO')")
    @GetMapping("/my/events/stats")
    public String myEventStats(Authentication authentication, Model model) {

        String username = authentication.getName();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        var events = isAdmin
                ? eventRepository.findAll()
                : eventRepository.findByCreatedBy_UsernameOrderByStartTimeAsc(username);

        model.addAttribute("events", events);
        model.addAttribute("isAdmin", isAdmin);

        return "events/my-stats";
    }
}
