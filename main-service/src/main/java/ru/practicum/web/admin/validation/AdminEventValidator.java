package ru.practicum.web.admin.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.web.event.entity.Event;
import ru.practicum.web.exception.BadRequestException;
import ru.practicum.web.exception.ConflictException;
import ru.practicum.web.validation.ValidationConstants;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AdminEventValidator {

    public void validateTitle(String title) {
        if (title != null) {
            if (title.length() < ValidationConstants.EVENT_TITLE_MIN ||
                    title.length() > ValidationConstants.EVENT_TITLE_MAX) {
                throw new BadRequestException("Title length must be between " +
                        ValidationConstants.EVENT_TITLE_MIN + " and " + ValidationConstants.EVENT_TITLE_MAX + " characters");
            }
        }
    }

    public void validateAnnotation(String annotation) {
        if (annotation != null) {
            if (annotation.length() < ValidationConstants.EVENT_ANNOTATION_MIN ||
                    annotation.length() > ValidationConstants.EVENT_ANNOTATION_MAX) {
                throw new BadRequestException("Annotation length must be between " +
                        ValidationConstants.EVENT_ANNOTATION_MIN + " and " + ValidationConstants.EVENT_ANNOTATION_MAX + " characters");
            }
        }
    }

    public void validateDescription(String description) {
        if (description != null) {
            if (description.length() < ValidationConstants.EVENT_DESCRIPTION_MIN ||
                    description.length() > ValidationConstants.EVENT_DESCRIPTION_MAX) {
                throw new BadRequestException("Description length must be between " +
                        ValidationConstants.EVENT_DESCRIPTION_MIN + " and " + ValidationConstants.EVENT_DESCRIPTION_MAX + " characters");
            }
        }
    }

    public void validateEventDate(LocalDateTime eventDate, String originalDateStr) {
        if (eventDate == null) return;

        if (eventDate.isBefore(LocalDateTime.now().plusHours(ValidationConstants.EVENT_HOURS_BEFORE_START))) {
            throw new BadRequestException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + originalDateStr);
        }
    }

    public void validateParticipantLimit(Integer limit) {
        if (limit != null && limit < ValidationConstants.EVENT_PARTICIPANT_LIMIT_MIN) {
            throw new BadRequestException("Participant limit must be non-negative");
        }
    }

    public void validatePublishEvent(Event event) {
        if (event.getStatus() != Event.Status.PENDING) {
            throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getStatus());
        }
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(ValidationConstants.EVENT_PUBLISH_HOURS_BEFORE))) {
            throw new ConflictException("Cannot publish event because event date is too soon");
        }
    }

    public void validateRejectEvent(Event event) {
        if (event.getStatus() == Event.Status.PUBLISHED) {
            throw new ConflictException("Cannot reject already published event");
        }
    }
}