package com.dtaquito_micro_services.sport_service.sportspaces.domain.model.commands;

public record CreateSportSpacesCommand(String name, Long sportId, String imageUrl, Double price, String district, String description, Long userId, String startTime, String endTime, String gamemode, Integer amount
) {

    public CreateSportSpacesCommand {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (sportId == null) {
            throw new IllegalArgumentException("Sport ID is required");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL is required");
        }
        if (price == null) {
            throw new IllegalArgumentException("Price is required");
        }
        if (district == null || district.isBlank()) {
            throw new IllegalArgumentException("District is required");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (startTime == null || startTime.isBlank()) {
            throw new IllegalArgumentException("Start time is required");
        }
        if (endTime == null || endTime.isBlank()) {
            throw new IllegalArgumentException("End time is required");
        }
        if (gamemode == null || gamemode.isBlank()) {
            throw new IllegalArgumentException("Gamemode is required");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required");
        }
    }
}