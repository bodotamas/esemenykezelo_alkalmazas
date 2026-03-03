package hu.tamas.szakdolgozatjava.init;

import hu.tamas.szakdolgozatjava.model.Category;
import hu.tamas.szakdolgozatjava.model.Event;
import hu.tamas.szakdolgozatjava.repository.EventRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class DataLoader implements CommandLineRunner {

    private final EventRepository eventRepository;

    public DataLoader(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public void run(String... args) {
        if (eventRepository.count() < 3)  {

            Event e1 = new Event(
                    "Lakógyűlés",
                    "Közösségi terem",
                    LocalDate.now().plusDays(3),
                    "Fontos megbeszélés a lakókat érintő kérdésekről."
            );
            e1.setCategory(Category.KOZOSSEGI);
            e1.setStartTime(LocalDateTime.of(e1.getDate(), LocalTime.of(18, 0)));
            e1.setEndTime(LocalDateTime.of(e1.getDate(), LocalTime.of(19, 30)));
            eventRepository.save(e1);

            Event e2 = new Event(
                    "Tavaszi takarítás",
                    "Udvar",
                    LocalDate.now().plusDays(10),
                    "Közös udvar- és kert rendbetétele."
            );
            e2.setCategory(Category.KOZOSSEGI);
            e2.setStartTime(LocalDateTime.of(e2.getDate(), LocalTime.of(9, 0)));
            e2.setEndTime(LocalDateTime.of(e2.getDate(), LocalTime.of(12, 0)));
            eventRepository.save(e2);

            Event e3 = new Event(
                    "Közösségi grillezés",
                    "Kert",
                    LocalDate.now().plusDays(20),
                    "Ismerkedés, jó hangulat, közös sütés-főzés."
            );
            e3.setCategory(Category.KOZOSSEGI);
            e3.setStartTime(LocalDateTime.of(e3.getDate(), LocalTime.of(16, 0)));
            e3.setEndTime(LocalDateTime.of(e3.getDate(), LocalTime.of(20, 0)));
            eventRepository.save(e3);
        }
    }
}
