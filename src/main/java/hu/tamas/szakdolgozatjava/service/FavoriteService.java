package hu.tamas.szakdolgozatjava.service;

import hu.tamas.szakdolgozatjava.repository.EventFavoriteRepository;
import hu.tamas.szakdolgozatjava.repository.EventRepository;
import hu.tamas.szakdolgozatjava.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import hu.tamas.szakdolgozatjava.model.EventFavorite;


import java.util.Set;

@Service
public class FavoriteService {

    private final EventFavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public FavoriteService(EventFavoriteRepository favoriteRepository,
                           UserRepository userRepository,
                           EventRepository eventRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    public Set<Long> favoriteEventIdsForUser(Long userId) {
        return favoriteRepository.findFavoriteEventIdsByUserId(userId);
    }

    public boolean isFavorite(Long userId, Long eventId) {
        return favoriteRepository.existsByUser_IdAndEvent_Id(userId, eventId);
    }

    @Transactional
    public boolean toggle(Long userId, Long eventId) {
        if (favoriteRepository.existsByUser_IdAndEvent_Id(userId, eventId)) {
            favoriteRepository.deleteByUser_IdAndEvent_Id(userId, eventId);
            return false;
        }

        var user = userRepository.findById(userId).orElseThrow();
        var event = eventRepository.findById(eventId).orElseThrow();
        favoriteRepository.save(new EventFavorite(user, event));
        return true;
    }
}
