package com.project.bookingya.questions;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ReservationQuestions {

    private final Response response;

    public ReservationQuestions(Response response) {
        this.response = response;
    }

    // ── Usado en TODOS los escenarios ──────────────────────────────────────
    public ReservationQuestions hasStatusCode(int expectedCode) {
        assertThat(response.getStatusCode())
                .as("Response status code should be " + expectedCode)
                .isEqualTo(expectedCode);
        return this;
    }

    // ── Usado en: crear reserva, obtener por ID ────────────────────────────
    public ReservationQuestions hasGeneratedId() {
        assertThat(response.jsonPath().getString("id"))
                .as("The response must contain a generated reservation ID")
                .isNotNull()
                .isNotEmpty();
        return this;
    }

    // ── Usado en: crear reserva ────────────────────────────────────────────
    public ReservationQuestions hasRoomId(String expectedRoomId) {
        assertThat(response.jsonPath().getString("roomId"))
                .as("The roomId in the response should match")
                .isEqualTo(expectedRoomId);
        return this;
    }

    // ── Usado en: crear reserva ────────────────────────────────────────────
    public ReservationQuestions hasGuestId(String expectedGuestId) {
        assertThat(response.jsonPath().getString("guestId"))
                .as("The guestId in the response should match")
                .isEqualTo(expectedGuestId);
        return this;
    }

    // ── Usado en: crear reserva, actualizar reserva ────────────────────────
    public ReservationQuestions hasGuestsCount(int expectedCount) {
        assertThat(response.jsonPath().getInt("guestsCount"))
                .as("The guestsCount in the response should match")
                .isEqualTo(expectedCount);
        return this;
    }

    // ── Usado en: consultar todas las reservas ─────────────────────────────
    public ReservationQuestions isAListOfReservations() {
        assertThat(response.jsonPath().getList("$"))
                .as("The response should be a non-null list of reservations")
                .isNotNull();
        return this;
    }

    // ── Usado en: eliminar reserva ─────────────────────────────────────────
    public ReservationQuestions reservationNoLongerExists(String baseUrl, String reservationId) {
        Response getResponse = SerenityRest.given()
                .get(baseUrl + "/reservation/" + reservationId);

        assertThat(getResponse.getStatusCode())
                .as("The deleted reservation should return 404")
                .isEqualTo(404);
        return this;
    }
}