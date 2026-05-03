package com.project.bookingya.steps.bdd;

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
import com.project.bookingya.questions.ReservationQuestions;

public class ReservationSteps {

    private String baseUrl;
    private Response response;
    private String reservationId;
    private String requestBody;

    // ══════════════════════════════════════════════
    // BEFORE — Limpia y prepara datos antes de cada escenario
    // ══════════════════════════════════════════════

    @Before
    public void insertTestData() {
        String base = "http://localhost:8080/api";

        // ── PASO 1: Limpiar datos de ejecuciones anteriores ──

        Response reservations = SerenityRest.given().get(base + "/reservation");
        List<Map<String, Object>> reservationList = reservations.jsonPath().getList("$");
        if (reservationList != null) {
            for (Map<String, Object> r : reservationList) {
                SerenityRest.given().delete(base + "/reservation/" + r.get("id"));
            }
        }

        Response rooms = SerenityRest.given().get(base + "/room");
        List<Map<String, Object>> roomList = rooms.jsonPath().getList("$");
        if (roomList != null) {
            for (Map<String, Object> r : roomList) {
                SerenityRest.given().delete(base + "/room/" + r.get("id"));
            }
        }

        Response guests = SerenityRest.given().get(base + "/guest");
        List<Map<String, Object>> guestList = guests.jsonPath().getList("$");
        if (guestList != null) {
            for (Map<String, Object> g : guestList) {
                SerenityRest.given().delete(base + "/guest/" + g.get("id"));
            }
        }

        // ── PASO 2: Crear datos frescos para el escenario ────

        SerenityRest.given()
                .contentType("application/json")
                .body("""
                {
                    "identification": "123456789",
                    "name": "Test Guest",
                    "email": "testguest@test.com"
                }
                """)
                .post(base + "/guest");

        SerenityRest.given()
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
                .post(base + "/room");
    }

    // ══════════════════════════════════════════════
    // AFTER — Limpia reservas después de cada escenario
    // ══════════════════════════════════════════════

    @After
    public void cleanUpReservations() {
        String base = "http://localhost:8080/api";

        Response reservations = SerenityRest.given().get(base + "/reservation");
        List<Map<String, Object>> list = reservations.jsonPath().getList("$");
        if (list != null) {
            for (Map<String, Object> reservation : list) {
                String id = reservation.get("id").toString();
                SerenityRest.given().delete(base + "/reservation/" + id);
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

    @Given("I have the following reservation data:")
    public void iHaveTheFollowingReservationData(DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);

        String guestId = getCreatedGuestId();
        String roomId  = getCreatedRoomId();

        this.requestBody = String.format("""
            {
                "guestId": "%s",
                "roomId": "%s",
                "checkIn": "%s",
                "checkOut": "%s",
                "guestsCount": %s
            }
            """,
                guestId,
                roomId,
                data.get("checkIn"),
                data.get("checkOut"),
                data.get("guestsCount")
        );
    }

    @Given("a reservation exists with the following data:")
    public void aReservationExistsWithTheFollowingData(DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);

        String guestId = getCreatedGuestId();
        String roomId  = getCreatedRoomId();

        String body = String.format("""
            {
                "guestId": "%s",
                "roomId": "%s",
                "checkIn": "%s",
                "checkOut": "%s",
                "guestsCount": %s
            }
            """,
                guestId,
                roomId,
                data.get("checkIn"),
                data.get("checkOut"),
                data.get("guestsCount")
        );

        response = SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .post(baseUrl + "/reservation");

        reservationId = response.jsonPath().getString("id");
    }

    @Given("I use the ID {string}")
    public void iUseTheId(String id) {
        this.reservationId = id;
    }

    // ══════════════════════════════════════════════
    // WHEN
    // ══════════════════════════════════════════════

    @When("I send a POST request to {string}")
    public void iSendAPostRequestTo(String endpoint) {
        response = SerenityRest.given()
                .contentType("application/json")
                .body(requestBody)
                .post(baseUrl + endpoint);
    }

    @When("I send a GET request to {string}")
    public void iSendAGetRequestTo(String endpoint) {
        String url = endpoint.replace("{id}", reservationId != null ? reservationId : "");
        response = SerenityRest.given()
                .get(baseUrl + url);
    }

    @When("I update the reservation with the following data:")
    public void iUpdateTheReservationWithTheFollowingData(DataTable dataTable) {
        Map<String, String> data = dataTable.asMaps().get(0);

        String guestId = getCreatedGuestId();
        String roomId  = getCreatedRoomId();

        String body = String.format("""
            {
                "guestId": "%s",
                "roomId": "%s",
                "checkIn": "%s",
                "checkOut": "%s",
                "guestsCount": %s
            }
            """,
                guestId,
                roomId,
                data.get("checkIn"),
                data.get("checkOut"),
                data.get("guestsCount")
        );

        response = SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .put(baseUrl + "/reservation/" + reservationId);
    }

    @When("I send a DELETE request to {string}")
    public void iSendADeleteRequestTo(String endpoint) {
        String url = endpoint.replace("{id}", reservationId);
        response = SerenityRest.given()
                .delete(baseUrl + url);
    }

    // ══════════════════════════════════════════════
    // THEN / AND
    // ══════════════════════════════════════════════

    @Then("the system responds with status code {int}")
    public void theSystemRespondsWithStatusCode(int expectedCode) {
        new ReservationQuestions(response)
                .hasStatusCode(expectedCode);
    }

    @And("the response contains a generated reservation ID")
    public void theResponseContainsAGeneratedReservationId() {
        new ReservationQuestions(response)
                .hasGeneratedId();
        this.reservationId = response.jsonPath().getString("id");
    }

    // ← theResponseContainsRoomId  eliminado, ya no está en el feature
    // ← theResponseContainsGuestId eliminado, ya no está en el feature

    @And("the response contains guestsCount {int}")
    public void theResponseContainsGuestsCount(int guestsCount) {
        new ReservationQuestions(response)
                .hasGuestsCount(guestsCount);
    }

    @And("the response is a list of reservations")
    public void theResponseIsAListOfReservations() {
        new ReservationQuestions(response)
                .isAListOfReservations();
    }

    @And("the reservation no longer exists in the system")
    public void theReservationNoLongerExistsInTheSystem() {
        new ReservationQuestions(response)
                .reservationNoLongerExists(baseUrl, reservationId);
    }

    // ══════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════

    private String getCreatedGuestId() {
        Response guests = SerenityRest.given()
                .get("http://localhost:8080/api/guest");
        return guests.jsonPath().getString("[0].id");
    }

    private String getCreatedRoomId() {
        Response rooms = SerenityRest.given()
                .get("http://localhost:8080/api/room");
        return rooms.jsonPath().getString("[0].id");
    }
}