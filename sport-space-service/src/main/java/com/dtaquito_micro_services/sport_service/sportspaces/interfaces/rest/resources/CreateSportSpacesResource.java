package com.dtaquito_micro_services.sport_service.sportspaces.interfaces.rest.resources;

public record CreateSportSpacesResource(String name, Long sportId, String imageUrl, Double price, String district, String description, Long userId, String startTime, String endTime,
                                        String gamemode, Integer amount) {

    public CreateSportSpacesResource {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if (sportId == null) {
            throw new IllegalArgumentException("SportId cannot be null");
        }
        if (imageUrl == null) {
            throw new IllegalArgumentException("ImageUrl cannot be null");
        }
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        if (district == null) {
            throw new IllegalArgumentException("District cannot be null");
        }
        if (description == null) {
            throw new IllegalArgumentException("Description cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("StartTime cannot be null");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("EndTime cannot be null");
        }
        if (gamemode == null) {
            throw new IllegalArgumentException("Gamemode cannot be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
    }
}
