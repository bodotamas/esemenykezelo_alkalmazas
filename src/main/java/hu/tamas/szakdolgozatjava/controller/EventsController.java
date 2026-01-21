package hu.tamas.szakdolgozatjava.controller;

import hu.tamas.szakdolgozatjava.model.Category;
import hu.tamas.szakdolgozatjava.model.Event;
import hu.tamas.szakdolgozatjava.repository.EventRepository;
import hu.tamas.szakdolgozatjava.repository.UserRepository;
import hu.tamas.szakdolgozatjava.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import hu.tamas.szakdolgozatjava.service.FavoriteService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;



import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Controller
public class EventsController {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RegistrationService registrationService;
    private final FavoriteService favoriteService;


    public EventsController(EventRepository eventRepository,
                            UserRepository userRepository,
                            RegistrationService registrationService,
                            FavoriteService favoriteService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.registrationService = registrationService;
        this.favoriteService = favoriteService;
    }


    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads";

    @GetMapping("/events")
    public String listEvents(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate date,
            @RequestParam(required = false, defaultValue = "false") boolean upcoming,
            @RequestParam(required = false, defaultValue = "START_ASC") String sort,
            @RequestParam(required = false, defaultValue = "false") boolean onlyFavorites,
            Model model,
            Principal principal
    ) {
        // category string -> enum (ha üres vagy hibás, akkor nincs szűrés)
        Category categoryEnum = null;
        if (category != null && !category.isBlank()) {
            try {
                categoryEnum = Category.valueOf(category);
            } catch (IllegalArgumentException ignored) {
                categoryEnum = null;
            }
        }

        // date -> [startOfDay, endOfDay)
        LocalDateTime startOfDay = null;
        LocalDateTime endOfDay = null;
        if (date != null) {
            startOfDay = date.atStartOfDay();
            endOfDay = date.plusDays(1).atStartOfDay();
        }

        // upcoming filterhez "now"
        LocalDateTime now = LocalDateTime.now();

        // rendezés
        org.springframework.data.domain.Sort sortObj = switch (sort) {
            case "START_DESC" -> org.springframework.data.domain.Sort.by("startTime").descending();
            case "CREATED_ASC" -> org.springframework.data.domain.Sort.by("createdAt").ascending();
            case "CREATED_DESC" -> org.springframework.data.domain.Sort.by("createdAt").descending();
            default -> org.springframework.data.domain.Sort.by("startTime").ascending(); // START_ASC
        };

        // alap lekérdezés (q/category/date/upcoming/sort)
        List<Event> events = eventRepository.search(q, categoryEnum, startOfDay, endOfDay, upcoming, now, sortObj);

        // kedvencek id-k (csak ha belépve)
        Set<Long> favoriteEventIds = Collections.emptySet();
        Long userId = null;
        if (principal != null) {
            var user = userRepository.findByUsername(principal.getName()).orElseThrow();
            userId = user.getId();
            favoriteEventIds = favoriteService.favoriteEventIdsForUser(userId);
        }

        // csak kedvencek szűrés (MVP: Java oldalon)
        if (onlyFavorites) {
            if (userId == null) {
                events = List.of(); // nincs belépve -> üres lista
            } else {
                Set<Long> favIdsFinal = favoriteEventIds;
                events = events.stream()
                        .filter(e -> favIdsFinal.contains(e.getId()))
                        .toList();
            }
        }

        model.addAttribute("events", events);
        model.addAttribute("favoriteEventIds", favoriteEventIds);

        // létszámok (eventId -> count)
        Map<Long, Long> attendeeCounts = new HashMap<>();
        for (Event e : events) {
            attendeeCounts.put(e.getId(), registrationService.countByEventId(e.getId()));
        }
        model.addAttribute("attendeeCounts", attendeeCounts);

        // bejelentkezett user regisztrált event id-k (a gombokhoz)
        Set<Long> registeredEventIds = Collections.emptySet();
        if (principal != null) {
            var user = userRepository.findByUsername(principal.getName()).orElseThrow();
            registeredEventIds = registrationService.registeredEventIdsForUser(user.getId());
        }
        model.addAttribute("registeredEventIds", registeredEventIds);

        // categories a selecthez
        model.addAttribute("allCategories", Category.values());

        // filter mezők visszatöltése
        model.addAttribute("q", q != null ? q : "");
        model.addAttribute("category", categoryEnum != null ? categoryEnum.name() : "");
        model.addAttribute("date", date != null ? date.toString() : "");
        model.addAttribute("upcoming", upcoming);
        model.addAttribute("sort", sort);
        model.addAttribute("onlyFavorites", onlyFavorites);

        return "events/list";
    }

    @GetMapping("/events/new")
    public String newEventForm(Model model) {
        Event e = new Event();
        e.setDate(java.time.LocalDate.now());
        e.setCategory(Category.EGYEB);

        model.addAttribute("event", e);
        model.addAttribute("allCategories", Category.values());

        // idő előtöltés
        model.addAttribute("startTimeStr", "09:00");
        model.addAttribute("endTimeStr", "10:00");

        return "events/new";
    }

    @PostMapping("/events")
    public String createEvent(@Valid @ModelAttribute("event") Event event,
                              BindingResult bindingResult,
                              @RequestParam(value = "startTimeStr", required = false) String startTimeStr,
                              @RequestParam(value = "endTimeStr", required = false) String endTimeStr,
                              @RequestParam(value = "image", required = false) MultipartFile image,
                              Principal principal,
                              Model model) throws Exception {

        model.addAttribute("allCategories", Category.values());
        model.addAttribute("startTimeStr", startTimeStr);
        model.addAttribute("endTimeStr", endTimeStr);

        // dátum validáció hibák
        if (bindingResult.hasErrors()) {
            return "events/new";
        }

        // idő parse + logikai ellenőrzés
        try {
            if (startTimeStr == null || startTimeStr.isBlank() || endTimeStr == null || endTimeStr.isBlank()) {
                model.addAttribute("timeError", "Kérlek add meg a kezdő és záró időpontot.");
                return "events/new";
            }

            LocalTime start = LocalTime.parse(startTimeStr);
            LocalTime end = LocalTime.parse(endTimeStr);

            LocalDateTime startDt = LocalDateTime.of(event.getDate(), start);
            LocalDateTime endDt = LocalDateTime.of(event.getDate(), end);

            if (!endDt.isAfter(startDt)) {
                model.addAttribute("timeError", "A záró időpontnak későbbinek kell lennie, mint a kezdő időpont.");
                return "events/new";
            }

            event.setStartTime(startDt);
            event.setEndTime(endDt);

        } catch (Exception ex) {
            model.addAttribute("timeError", "Hibás időformátum. (HH:mm)");
            return "events/new";
        }

        var user = userRepository.findByUsername(principal.getName()).orElseThrow();
        event.setCreatedBy(user);

        eventRepository.save(event);

        // kép mentése
        if (image != null && !image.isEmpty()) {
            Path uploadRoot = Paths.get(UPLOAD_DIR, "events");
            Files.createDirectories(uploadRoot);

            String original = image.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }

            String fileName = event.getId() + "_" + UUID.randomUUID() + ext;
            Path target = uploadRoot.resolve(fileName);

            image.transferTo(target.toFile());

            event.setImagePath("events/" + fileName);
            eventRepository.save(event);
        }

        return "redirect:/events";
    }

    // ===== JELENTKEZÉS / LEMONDÁS =====

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/events/{id}/join")
    public String joinEvent(@PathVariable Long id, Principal principal) {
        var user = userRepository.findByUsername(principal.getName()).orElseThrow();

        if (!eventRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Esemény nem található");
        }

        registrationService.register(id, user.getId());
        return "redirect:/events";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/events/{id}/leave")
    public String leaveEvent(@PathVariable Long id, Principal principal) {
        var user = userRepository.findByUsername(principal.getName()).orElseThrow();

        if (!eventRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Esemény nem található");
        }

        registrationService.unregister(id, user.getId());
        return "redirect:/events";
    }

    // ===== EDIT / UPDATE / DELETE =====

    @PreAuthorize("hasRole('ADMIN') or @eventSecurity.isOwner(#id, authentication)")
    @GetMapping("/events/{id}/edit")
    public String editEventForm(@PathVariable Long id, Model model) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Esemény nem található"));

        model.addAttribute("event", event);
        model.addAttribute("allCategories", Category.values());

        model.addAttribute("startTimeStr",
                event.getStartTime() != null ? event.getStartTime().toLocalTime().toString() : "09:00");
        model.addAttribute("endTimeStr",
                event.getEndTime() != null ? event.getEndTime().toLocalTime().toString() : "10:00");

        return "events/edit";
    }

    @PreAuthorize("hasRole('ADMIN') or @eventSecurity.isOwner(#id, authentication)")
    @PostMapping("/events/{id}")
    public String updateEvent(@PathVariable Long id,
                              @Valid @ModelAttribute("event") Event event,
                              BindingResult bindingResult,
                              @RequestParam(value = "startTimeStr", required = false) String startTimeStr,
                              @RequestParam(value = "endTimeStr", required = false) String endTimeStr,
                              @RequestParam(value = "image", required = false) MultipartFile image,
                              Model model) throws Exception {

        model.addAttribute("allCategories", Category.values());
        model.addAttribute("startTimeStr", startTimeStr);
        model.addAttribute("endTimeStr", endTimeStr);

        var existing = eventRepository.findById(id).orElseThrow();

        if (bindingResult.hasErrors()) {
            return "events/edit";
        }

        // idő parse + logikai ellenőrzés
        try {
            if (startTimeStr == null || startTimeStr.isBlank() || endTimeStr == null || endTimeStr.isBlank()) {
                model.addAttribute("timeError", "Kérlek add meg a kezdő és záró időpontot.");
                return "events/edit";
            }

            LocalTime start = LocalTime.parse(startTimeStr);
            LocalTime end = LocalTime.parse(endTimeStr);

            LocalDateTime startDt = LocalDateTime.of(event.getDate(), start);
            LocalDateTime endDt = LocalDateTime.of(event.getDate(), end);

            if (!endDt.isAfter(startDt)) {
                model.addAttribute("timeError", "A záró időpontnak későbbinek kell lennie, mint a kezdő időpont.");
                return "events/edit";
            }

            // mezők másolása + idő beállítás
            existing.setStartTime(startDt);
            existing.setEndTime(endDt);

        } catch (Exception ex) {
            model.addAttribute("timeError", "Hibás időformátum. (HH:mm)");
            return "events/edit";
        }

        existing.setTitle(event.getTitle());
        existing.setLocation(event.getLocation());
        existing.setDate(event.getDate());
        existing.setDescription(event.getDescription());
        existing.setCategory(event.getCategory());

        // kép csere
        if (image != null && !image.isEmpty()) {
            Path uploadRoot = Paths.get(UPLOAD_DIR, "events");
            Files.createDirectories(uploadRoot);

            String ext = "";
            String original = image.getOriginalFilename();
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }

            String fileName = existing.getId() + "_" + UUID.randomUUID() + ext;
            Path target = uploadRoot.resolve(fileName);
            image.transferTo(target.toFile());

            existing.setImagePath("events/" + fileName);
        }

        eventRepository.save(existing);
        return "redirect:/events";
    }

    @PreAuthorize("hasRole('ADMIN') or @eventSecurity.isOwner(#id, authentication)")
    @PostMapping("/events/{id}/delete")
    public String deleteEvent(@PathVariable Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Esemény nem található");
        }
        eventRepository.deleteById(id);
        return "redirect:/events";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/events/{id}/favorite")
    public String toggleFavorite(
            @PathVariable Long id,
            Principal principal,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String upcoming,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String onlyFavorites
    ) {
        var user = userRepository.findByUsername(principal.getName()).orElseThrow();

        if (!eventRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Esemény nem található");
        }

        favoriteService.toggle(user.getId(), id);

        // vissza az /events-re úgy, hogy megtartsa a filtereket
        List<String> params = new ArrayList<>();

        if (q != null && !q.isBlank()) {
            params.add("q=" + URLEncoder.encode(q, StandardCharsets.UTF_8));
        }
        if (category != null && !category.isBlank()) {
            params.add("category=" + category);
        }
        if (date != null && !date.isBlank()) {
            params.add("date=" + date);
        }
        if ("true".equalsIgnoreCase(upcoming)) {
            params.add("upcoming=true");
        }
        if (sort != null && !sort.isBlank()) {
            params.add("sort=" + sort);
        }
        if ("true".equalsIgnoreCase(onlyFavorites)) {
            params.add("onlyFavorites=true");
        }

        if (params.isEmpty()) return "redirect:/events";
        return "redirect:/events?" + String.join("&", params);
    }

}
