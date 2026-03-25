package com.careerplanning.backend.modules.career.service;

import com.careerplanning.backend.modules.career.repository.CareerGoalTemplateRepository;
import com.careerplanning.backend.modules.career.repository.CareerPhaseRepository;
import com.careerplanning.backend.modules.users.entity.CareerTrack;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class CareerTrackCatalogService {

    private static final List<String> TECHNICAL_TRACKS = List.of(
            CareerTrack.JAVA_BACKEND_DEVELOPER.name(),
            CareerTrack.FRONTEND_DEVELOPER.name(),
            CareerTrack.FULL_STACK_DEVELOPER.name(),
            CareerTrack.DATA_SCIENTIST.name(),
            CareerTrack.DEVOPS_ENGINEER.name()
    );

    private final CareerPhaseRepository careerPhaseRepository;
    private final CareerGoalTemplateRepository careerGoalTemplateRepository;

    public CareerTrackCatalogService(CareerPhaseRepository careerPhaseRepository,
                                     CareerGoalTemplateRepository careerGoalTemplateRepository) {
        this.careerPhaseRepository = careerPhaseRepository;
        this.careerGoalTemplateRepository = careerGoalTemplateRepository;
    }

    public String defaultCareerTrack() {
        return CareerTrack.FULL_STACK_DEVELOPER.name();
    }

    public List<String> listTechnicalTracks() {
        return TECHNICAL_TRACKS;
    }

    public List<String> listAvailableTracks() {
        Set<String> tracks = new LinkedHashSet<>(CareerTrack.options());
        careerPhaseRepository.findDistinctCareerTracks().stream()
                .map(this::normalizeTrackKey)
                .filter(value -> !value.isBlank())
                .forEach(tracks::add);
        careerGoalTemplateRepository.findDistinctCareerTracks().stream()
                .map(this::normalizeTrackKey)
                .filter(value -> !value.isBlank())
                .forEach(tracks::add);
        return List.copyOf(tracks);
    }

    public String normalizeTrackKey(String careerTrack) {
        if (careerTrack == null) {
            return "";
        }

        return careerTrack.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+", "")
                .replaceAll("_+$", "");
    }

    public String validateKnownCareerTrack(String careerTrack) {
        String normalized = normalizeTrackKey(careerTrack);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("careerTrack must not be blank");
        }
        if (!isKnownCareerTrack(normalized)) {
            throw new IllegalArgumentException("Invalid careerTrack. Use one of: " + String.join(", ", listAvailableTracks()));
        }
        return normalized;
    }

    public boolean isKnownCareerTrack(String careerTrack) {
        String normalized = normalizeTrackKey(careerTrack);
        if (normalized.isBlank()) {
            return false;
        }
        return listAvailableTracks().contains(normalized);
    }

    public String validateTechnicalCareerTrack(String careerTrack) {
        String normalized = normalizeTrackKey(careerTrack);
        if (!TECHNICAL_TRACKS.contains(normalized)) {
            throw new IllegalArgumentException("Technical benchmark supports: " + String.join(", ", TECHNICAL_TRACKS));
        }
        return normalized;
    }
}
