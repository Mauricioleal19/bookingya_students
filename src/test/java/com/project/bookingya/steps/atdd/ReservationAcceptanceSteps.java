package com.project.bookingya.steps.atdd;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import java.util.List;
import java.util.Map;
import com.project.bookingya.questions.ReservationAcceptanceQuestions;

public class ReservationAcceptanceSteps {

    private Response response;
    private String guestId;
    private String roomId;
    private String reservationId;
    private String secondReservationId;

    private String baseUrl = "http://localhost:8080/api";  // ✦ inicializar aquí
    // ══════════════════════════════════════════════
    // BEFORE / AFTER
    // ══════════════════════════════════════════════

    @Before
    public void cleanData() {
        String base = baseUrl;

        // Limpiar reservas primero (dependen de room y guest)
        Response reservations = SerenityRest.given().get(base + "/reservation");
        List<Map<String, Object>> reservationList = reservations.jsonPath().getList("$");
        if (reservationList != null) {
            for (Map<String, Object> r : reservationList) {
                SerenityRest.given().delete(base + "/reservation/" + r.get("id"));
            }
        }

        // Limpiar rooms
        Response rooms = SerenityRest.given().get(base + "/room");
        List<Map<String, Object>> roomList = rooms.jsonPath().getList("$");
        if (roomList != null) {
            for (Map<String, Object> r : roomList) {
                SerenityRest.given().delete(base + "/room/" + r.get("id"));
            }
        }

        // Limpiar guests
        Response guests = SerenityRest.given().get(base + "/guest");
        List<Map<String, Object>> guestList = guests.jsonPath().getList("$");
        if (guestList != null) {
            for (Map<String, Object> g : guestList) {
                SerenityRest.given().delete(base + "/guest/" + g.get("id"));
            }
        }
    }

    @After
    public void cleanReservations() {
        String base = "http://localhost:8080/api";
        Response reservations = SerenityRest.given().get(base + "/reservation");
        List<Map<String, Object>> list = reservations.jsonPath().getList("$");
        if (list != null) {
            for (Map<String, Object> r : list) {
                SerenityRest.given().delete(base + "/reservation/" + r.get("id"));
            }
        }
    }

    // ══════════════════════════════════════════════
    // GIVEN
    // ══════════════════════════════════════════════

    @Given("the system is available at {string}")
    public void theSystemIsAvailableAt(String url) {
        this.baseUrl = url;
    }

    @Given("a guest and a room are available in the system")
    public void aGuestAndARoomAreAvailableInTheSystem() {
        // Crear guest de prueba
        Response guestResponse = SerenityRest.given()
                .contentType("application/json")
                .body("""
                {
                    "identification": "123456789",
                    "name": "Test Guest",
                    "email": "testguest@test.com"
                }
                """)
                .post(baseUrl + "/guest");
        guestId = guestResponse.jsonPath().getString("id");

        // Crear room de prueba disponible
        Response roomResponse = SerenityRest.given()
                .contentType("application/json")
                .body("""
                {
                    "code": "ROOM-001",
                    "name": "Test Room",
                    "city": "Bogota",
                    "maxGuests": 4,
                    "nightlyPrice": 150000.00,
                    "available": true
                }
                """)
                .post(baseUrl + "/room");
        roomId = roomResponse.jsonPath().getString("id");
    }

    // ── AC-12: guest registrado sin room ─────────────────────

    @Given("a guest is registered in the system")
    public void aGuestIsRegisteredInTheSystem() {
        Response guestResponse = SerenityRest.given()
                .contentType("application/json")
                .body("""
                {
                    "identification": "123456789",
                    "name": "Test Guest",
                    "email": "testguest@test.com"
                }
                """)
                .post(baseUrl + "/guest");
        guestId = guestResponse.jsonPath().getString("id");
    }

    // ── AC-12: room marcado como no disponible ────────────────

    @And("a room exists but is marked as unavailable")
    public void aRoomExistsButIsMarkedAsUnavailable() {
        Response roomResponse = SerenityRest.given()
                .contentType("application/json")
                .body("""
                {
                    "code": "ROOM-002",
                    "name": "Unavailable Room",
                    "city": "Bogota",
                    "maxGuests": 4,
                    "nightlyPrice": 150000.00,
                    "available": false
                }
                """)
                .post(baseUrl + "/room");
        roomId = roomResponse.jsonPath().getString("id");
    }

    // ── AC-13: room disponible sin guest ─────────────────────

    @Given("a room is available in the system")
    public void aRoomIsAvailableInTheSystem() {
        Response roomResponse = SerenityRest.given()
                .contentType("application/json")
                .body("""
                {
                    "code": "ROOM-001",
                    "name": "Test Room",
                    "city": "Bogota",
                    "maxGuests": 4,
                    "nightlyPrice": 150000.00,
                    "available": true
                }
                """)
                .post(baseUrl + "/room");
        roomId = roomResponse.jsonPath().getString("id");
    }

    // ── AC-15: sin reservas en el sistema ────────────────────

    @Given("there are no reservations in the system")
    public void thereAreNoReservationsInTheSystem() {
        // El @Before ya limpió todo — este paso solo documenta la condición
    }

    // ── AC-16: cliente con múltiples reservas ────────────────

    @And("the client has {int} active reservations:")
    public void theClientHasActiveReservations(int count, DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();

        // Crear un segundo room para evitar solapamiento entre reservas
        Response roomResponse2 = SerenityRest.given()
                .contentType("application/json")
                .body("""
                {
                    "code": "ROOM-002",
                    "name": "Test Room 2",
                    "city": "Bogota",
                    "maxGuests": 4,
                    "nightlyPrice": 150000.00,
                    "available": true
                }
                """)
                .post(baseUrl + "/room");
        String roomId2 = roomResponse2.jsonPath().getString("id");
        String[] roomIds = { roomId, roomId2 };

        // Crear cada reserva con su room correspondiente
        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            SerenityRest.given()
                    .contentType("application/json")
                    .body(String.format("""
                    {
                        "guestId": "%s",
                        "roomId": "%s",
                        "checkIn": "%s",
                        "checkOut": "%s",
                        "guestsCount": %s
                    }
                    """, guestId, roomIds[i],
                            row.get("checkIn"),
                            row.get("checkOut"),
                            row.get("guestsCount")))
                    .post(baseUrl + "/reservation");
        }
    }

    // ── AC-17: room con reserva activa ───────────────────────

    @And("the room has an active reservation from {string} to {string}")
    public void theRoomHasAnActiveReservationFromTo(String checkIn, String checkOut) {
        Response res = SerenityRest.given()
                .contentType("application/json")
                .body(String.format("""
                {
                    "guestId": "%s",
                    "roomId": "%s",
                    "checkIn": "%s",
                    "checkOut": "%s",
                    "guestsCount": 2
                }
                """, guestId, roomId, checkIn, checkOut))
                .post(baseUrl + "/reservation");
        reservationId = res.jsonPath().getString("id");
    }

    // ── AC-18 / AC-20: reserva activa simple ─────────────────

    @And("the client has an active reservation from {string} to {string}")
    public void theClientHasAnActiveReservationFromTo(String checkIn, String checkOut) {
        Response res = SerenityRest.given()
                .contentType("application/json")
                .body(String.format("""
                {
                    "guestId": "%s",
                    "roomId": "%s",
                    "checkIn": "%s",
                    "checkOut": "%s",
                    "guestsCount": 2
                }
                """, guestId, roomId, checkIn, checkOut))
                .post(baseUrl + "/reservation");
        reservationId = res.jsonPath().getString("id");
    }

    // ── AC-22: room bloqueado + reserva del cliente ──────────

    @And("the room is already reserved from {string} to {string}")
    public void theRoomIsAlreadyReservedFromTo(String checkIn, String checkOut) {
        // Reserva existente que bloqueará las fechas del room
        Response res = SerenityRest.given()
                .contentType("application/json")
                .body(String.format("""
                {
                    "guestId": "%s",
                    "roomId": "%s",
                    "checkIn": "%s",
                    "checkOut": "%s",
                    "guestsCount": 2
                }
                """, guestId, roomId, checkIn, checkOut))
                .post(baseUrl + "/reservation");

        // Guardamos como segunda reserva para no pisar reservationId
        secondReservationId = res.jsonPath().getString("id");
    }

    // ── AC-23: reserva con N huéspedes ───────────────────────

    @And("the client has an active reservation for {int} guests from {string} to {string}")
    public void theClientHasAnActiveReservationForGuests(int guestsCount, String checkIn, String checkOut) {
        Response res = SerenityRest.given()
                .contentType("application/json")
                .body(String.format("""
                {
                    "guestId": "%s",
                    "roomId": "%s",
                    "checkIn": "%s",
                    "checkOut": "%s",
                    "guestsCount": %d
                }
                """, guestId, roomId, checkIn, checkOut, guestsCount))
                .post(baseUrl + "/reservation");
        reservationId = res.jsonPath().getString("id");
    }

    // ══════════════════════════════════════════════
    // WHEN
    // ══════════════════════════════════════════════

    // ── AC-12: intento con room no disponible ────────────────

    @When("the client tries to create a reservation for that room")
    public void theClientTriesToCreateAReservationForThatRoom() {
        response = SerenityRest.given()
                .contentType("application/json")
                .body(String.format("""
                {
                    "guestId": "%s",
                    "roomId": "%s",
                    "checkIn": "2026-09-01T14:00:00",
                    "checkOut": "2026-09-05T11:00:00",
                    "guestsCount": 2
                }
                """, guestId, roomId))
                .post(baseUrl + "/reservation");
    }

    // ── AC-13: guest inexistente ─────────────────────────────

    @When("the client tries to create a reservation with a non-existent guest ID")
    public void theClientTriesToCreateAReservationWithNonExistentGuestId() {
        String nonExistentGuestId = "00000000-0000-0000-0000-000000000000";
        response = SerenityRest.given()
                .contentType("application/json")
                .body(String.format("""
                {
                    "guestId": "%s",
                    "roomId": "%s",
                    "checkIn": "2026-09-01T14:00:00",
                    "checkOut": "2026-09-05T11:00:00",
                    "guestsCount": 2
                }
                """, nonExistentGuestId, roomId))
                .post(baseUrl + "/reservation");
    }

    // ── AC-14: room inexistente ──────────────────────────────

    @When("the client tries to create a reservation with a non-existent room ID")
    public void theClientTriesToCreateAReservationWithNonExistentRoomId() {
        String nonExistentRoomId = "00000000-0000-0000-0000-000000000000";
        response = SerenityRest.given()
                .contentType("application/json")
                .body(String.format("""
                {
                    "guestId": "%s",
                    "roomId": "%s",
                    "checkIn": "2026-09-01T14:00:00",
                    "checkOut": "2026-09-05T11:00:00",
                    "guestsCount": 2
                }
                """, guestId, nonExistentRoomId))
                .post(baseUrl + "/reservation");
    }

    // ── AC-16: consulta por guest ────────────────────────────

    @When("the client queries their reservations by guest ID")
    public void theClientQueriesTheirReservationsByGuestId() {
        response = SerenityRest.given()
                .get(baseUrl + "/reservation/guest/" + guestId);
    }

    // ── AC-17: consulta por room ─────────────────────────────

    @When("the administrator queries reservations by room ID")
    public void theAdministratorQueriesReservationsByRoomId() {
        response = SerenityRest.given()
                .get(baseUrl + "/reservation/room/" + roomId);
    }

    // ── AC-18 / AC-20: obtener reserva por ID ────────────────

    @When("the client retrieves the reservation by its ID")
    public void theClientRetrievesTheReservationByItsId() {
        response = SerenityRest.given()
                .get(baseUrl + "/reservation/" + reservationId);
    }

    // ── AC-19: ID inexistente ────────────────────────────────

    @When("the client tries to retrieve reservation with ID {string}")
    public void theClientTriesToRetrieveReservationWithId(String id) {
        response = SerenityRest.given()
                .get(baseUrl + "/reservation/" + id);
    }

    // ── AC-21: actualizar reserva inexistente ────────────────

    @When("the client tries to update reservation with ID {string}")
    public void theClientTriesToUpdateReservationWithId(String id) {
        response = SerenityRest.given()
                .contentType("application/json")
                .body(String.format("""
                {
                    "guestId": "%s",
                    "roomId": "%s",
                    "checkIn": "2026-09-01T14:00:00",
                    "checkOut": "2026-09-05T11:00:00",
                    "guestsCount": 2
                }
                """, guestId, roomId))
                .put(baseUrl + "/reservation/" + id);
    }

    // ── AC-22: actualizar con fechas solapadas ───────────────

    @When("the client updates the reservation to dates {string} to {string}")
    public void theClientUpdatesTheReservationToDates(String checkIn, String checkOut) {
        response = SerenityRest.given()
                .contentType("application/json")
                .body(String.format("""
                {
                    "guestId": "%s",
                    "roomId": "%s",
                    "checkIn": "%s",
                    "checkOut": "%s",
                    "guestsCount": 2
                }
                """, guestId, roomId, checkIn, checkOut))
                .put(baseUrl + "/reservation/" + reservationId);
    }

    // ── AC-23: actualizar número de huéspedes ────────────────

    @When("the client updates the reservation to {int} guests")
    public void theClientUpdatesTheReservationToGuests(int guestsCount) {
        response = SerenityRest.given()
                .contentType("application/json")
                .body(String.format("""
                {
                    "guestId": "%s",
                    "roomId": "%s",
                    "checkIn": "2026-09-01T14:00:00",
                    "checkOut": "2026-09-05T11:00:00",
                    "guestsCount": %d
                }
                """, guestId, roomId, guestsCount))
                .put(baseUrl + "/reservation/" + reservationId);
    }

    // ── AC-24: cancelar reserva ──────────────────────────────

    @When("the client cancels the reservation")
    public void theClientCancelsTheReservation() {
        response = SerenityRest.given()
                .delete(baseUrl + "/reservation/" + reservationId);
    }

    // ══════════════════════════════════════════════
    // THEN / AND
    // ══════════════════════════════════════════════

    @Then("the system rejects the reservation with status {int}")
    public void theSystemRejectsTheReservationWithStatus(int status) {
        new ReservationAcceptanceQuestions(response)
                .hasStatusCode(status);
    }

    @Then("the system rejects the request with status {int}")
    public void theSystemRejectsTheRequestWithStatus(int status) {
        new ReservationAcceptanceQuestions(response)
                .hasStatusCode(status);
    }

    @And("the error indicates the room is not available")
    public void theErrorIndicatesTheRoomIsNotAvailable() {
        new ReservationAcceptanceQuestions(response)
                .hasErrorMessage();
    }

    @And("the error indicates the room is not available in that time range")
    public void theErrorIndicatesTheRoomIsNotAvailableInThatTimeRange() {
        new ReservationAcceptanceQuestions(response)
                .hasErrorMessage();
    }

    @Then("the system returns a list with {int} reservations")
    public void theSystemReturnsAListWithReservations(int expectedCount) {
        new ReservationAcceptanceQuestions(response)
                .hasStatusCode(200)
                .hasListSize(expectedCount);
    }

    @And("all reservations belong to the same guest")
    public void allReservationsBelongToTheSameGuest() {
        new ReservationAcceptanceQuestions(response)
                .allReservationsBelongToGuest(guestId);
    }

    @And("all reservations belong to the same room")
    public void allReservationsBelongToTheSameRoom() {
        new ReservationAcceptanceQuestions(response)
                .allReservationsBelongToRoom(roomId);
    }

    @Then("the system returns status {int}")
    public void theSystemReturnsStatus(int status) {
        new ReservationAcceptanceQuestions(response)
                .hasStatusCode(status);
    }

    @And("the reservation details are complete and match the original data")
    public void theReservationDetailsAreCompleteAndMatchTheOriginalData() {
        new ReservationAcceptanceQuestions(response)
                .hasGeneratedId()
                .hasGuestId(guestId)
                .hasRoomId(roomId);
    }

    @Then("the response contains all required reservation fields")
    public void theResponseContainsAllRequiredReservationFields() {
        new ReservationAcceptanceQuestions(response)
                .hasAllRequiredFields();
    }

    @And("no field in the response is null or empty")
    public void noFieldInTheResponseIsNullOrEmpty() {
        new ReservationAcceptanceQuestions(response)
                .hasNoNullFields();
    }

    @Then("the system confirms the update with status {int}")
    public void theSystemConfirmsTheUpdateWithStatus(int status) {
        new ReservationAcceptanceQuestions(response)
                .hasStatusCode(status);
    }

    @And("the reservation now shows {int} guests")
    public void theReservationNowShowsGuests(int guestsCount) {
        new ReservationAcceptanceQuestions(response)
                .hasGuestsCount(guestsCount);
    }

    @And("the reservation ID remains the same")
    public void theReservationIdRemainsTheSame() {
        new ReservationAcceptanceQuestions(response)
                .hasSpecificId(reservationId);
    }

    @Then("the system confirms the cancellation with status {int}")
    public void theSystemConfirmsTheCancellationWithStatus(int status) {
        new ReservationAcceptanceQuestions(response)
                .hasStatusCode(status);
    }

    @And("another client can book the same room for the same dates successfully")
    public void anotherClientCanBookTheSameRoomForTheSameDatesSuccessfully() {
        // Intentar crear una nueva reserva en el mismo room y fechas
        Response newReservation = SerenityRest.given()
                .contentType("application/json")
                .body(String.format("""
                {
                    "guestId": "%s",
                    "roomId": "%s",
                    "checkIn": "2026-09-01T14:00:00",
                    "checkOut": "2026-09-05T11:00:00",
                    "guestsCount": 2
                }
                """, guestId, roomId))
                .post(baseUrl + "/reservation");

        // El room debe estar libre tras la cancelación
        new ReservationAcceptanceQuestions(newReservation)
                .hasStatusCode(200)
                .hasGeneratedId();
    }
}