package com.project.bookingya.questions;

import java.util.List;
import java.util.Map;
import io.restassured.response.Response;
import static org.assertj.core.api.Assertions.assertThat;

public class ReservationAcceptanceQuestions {

    private final Response response;

    public ReservationAcceptanceQuestions(Response response) {
        this.response = response;
    }

    public ReservationAcceptanceQuestions hasStatusCode(int expectedStatus) {
        assertThat(response.getStatusCode())
                .as("Expected HTTP status " + expectedStatus)
                .isEqualTo(expectedStatus);
        return this;
    }

    public ReservationAcceptanceQuestions hasErrorMessage() {
        String body = response.getBody().asString();
        assertThat(body)
                .as("Response body should contain an error message")
                .isNotBlank();
        return this;
    }

    public ReservationAcceptanceQuestions hasListSize(int expectedSize) {
        List<?> list = response.jsonPath().getList("$");
        assertThat(list)
                .as("Expected list size " + expectedSize)
                .hasSize(expectedSize);
        return this;
    }

    public ReservationAcceptanceQuestions allReservationsBelongToGuest(String guestId) {
        List<String> guestIds = response.jsonPath().getList("guestId");
        assertThat(guestIds)
                .as("All reservations must belong to guest " + guestId)
                .allMatch(id -> id.equals(guestId));
        return this;
    }

    public ReservationAcceptanceQuestions allReservationsBelongToRoom(String roomId) {
        List<String> roomIds = response.jsonPath().getList("roomId");
        assertThat(roomIds)
                .as("All reservations must belong to room " + roomId)
                .allMatch(id -> id.equals(roomId));
        return this;
    }

    public ReservationAcceptanceQuestions hasGeneratedId() {
        assertThat(response.jsonPath().getString("id"))
                .as("Reservation must have a generated ID")
                .isNotBlank();
        return this;
    }

    public ReservationAcceptanceQuestions hasSpecificId(String expectedId) {
        assertThat(response.jsonPath().getString("id"))
                .as("Reservation ID must remain unchanged")
                .isEqualTo(expectedId);
        return this;
    }

    public ReservationAcceptanceQuestions hasGuestId(String expectedGuestId) {
        assertThat(response.jsonPath().getString("guestId"))
                .as("guestId must match " + expectedGuestId)
                .isEqualTo(expectedGuestId);
        return this;
    }

    public ReservationAcceptanceQuestions hasRoomId(String expectedRoomId) {
        assertThat(response.jsonPath().getString("roomId"))
                .as("roomId must match " + expectedRoomId)
                .isEqualTo(expectedRoomId);
        return this;
    }

    public ReservationAcceptanceQuestions hasGuestsCount(int expectedCount) {
        assertThat(response.jsonPath().getInt("guestsCount"))
                .as("guestsCount must be " + expectedCount)
                .isEqualTo(expectedCount);
        return this;
    }

    public ReservationAcceptanceQuestions hasAllRequiredFields() {
        assertThat(response.jsonPath().getString("id")).isNotNull();
        assertThat(response.jsonPath().getString("guestId")).isNotNull();
        assertThat(response.jsonPath().getString("roomId")).isNotNull();
        assertThat(response.jsonPath().getString("checkIn")).isNotNull();
        assertThat(response.jsonPath().getString("checkOut")).isNotNull();
        assertThat(response.jsonPath().getInt("guestsCount")).isPositive();
        return this;
    }

    public ReservationAcceptanceQuestions hasNoNullFields() {
        Map<String, Object> body = response.jsonPath().getMap("$");
        body.forEach((key, value) ->
                assertThat(value)
                        .as("Field '" + key + "' must not be null")
                        .isNotNull()
        );
        return this;
    }
}