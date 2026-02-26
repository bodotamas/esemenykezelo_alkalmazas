package hu.tamas.szakdolgozatjava.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Publikus azonosító (URL-ben, megjelenítéshez)
     */
    @Column(nullable = false, unique = true, length = 36)
    private String publicId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String location;

    @NotNull(message = "Kérlek válaszd ki az esemény dátumát.")
    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Category category = Category.EGYEB;

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String imagePath;

    // ===== ÚJ: láthatóság + kapacitás =====

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EventVisibility visibility = EventVisibility.PUBLIC;

    /**
     * Max létszám. Ha null, akkor korlátlan.
     */
    @Min(value = 1, message = "A maximális létszám minimum 1 lehet.")
    @Column
    private Integer capacity;

    // ===== KONSTRUKTOROK =====

    public Event() {}

    public Event(String title, String location, LocalDate date, String description) {
        this.title = title;
        this.location = location;
        this.date = date;
        this.description = description;
    }

    // ===== GETTEREK =====

    public Long getId() { return id; }

    public String getPublicId() { return publicId; }

    public String getTitle() { return title; }

    public String getLocation() { return location; }

    public LocalDate getDate() { return date; }

    public LocalDateTime getStartTime() { return startTime; }

    public LocalDateTime getEndTime() { return endTime; }

    public Category getCategory() { return category; }

    public String getDescription() { return description; }

    public User getCreatedBy() { return createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getImagePath() { return imagePath; }

    public EventVisibility getVisibility() { return visibility; }

    public Integer getCapacity() { return capacity; }

    // ===== SETTEREK =====

    public void setTitle(String title) { this.title = title; }

    public void setLocation(String location) { this.location = location; }

    public void setDate(LocalDate date) { this.date = date; }

    public void setIdForRegistration(Long id) { this.id = id; }

    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public void setCategory(Category category) { this.category = category; }

    public void setDescription(String description) { this.description = description; }

    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public void setVisibility(EventVisibility visibility) { this.visibility = visibility; }

    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    // ===== AUTOMATIKUS MEZŐK =====

    @PrePersist
    public void prePersist() {
        if (this.publicId == null) this.publicId = UUID.randomUUID().toString();
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.category == null) this.category = Category.EGYEB;
        if (this.visibility == null) this.visibility = EventVisibility.PUBLIC;

        if (this.startTime == null && this.date != null)
            this.startTime = LocalDateTime.of(this.date, LocalTime.of(9, 0));
        if (this.endTime == null && this.date != null)
            this.endTime = LocalDateTime.of(this.date, LocalTime.of(10, 0));

        // capacity: null = korlátlan, nem kényszerítjük
    }
}