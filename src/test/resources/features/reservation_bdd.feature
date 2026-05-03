#language: en
Feature: Reservation Management (BDD)

  Background:
    Given the system is available at "http://localhost:8080/api"

  # ══════════════════════════════════════════════
  # SCENARIO 1: Create a reservation
  # ══════════════════════════════════════════════

  Scenario: Successfully create a reservation with valid data
    Given I have the following reservation data:
      | checkIn             | checkOut            | guestsCount |
      | 2026-06-01T14:00:00 | 2026-06-05T11:00:00 | 2           |
    When I send a POST request to "/reservation"
    Then the system responds with status code 200
    And the response contains a generated reservation ID

  # ══════════════════════════════════════════════
  # SCENARIO 2: Get all reservations
  # ══════════════════════════════════════════════

  Scenario: Successfully retrieve all reservations
    When I send a GET request to "/reservation"
    Then the system responds with status code 200
    And the response is a list of reservations

  # ══════════════════════════════════════════════
  # SCENARIO 3: Get a reservation by ID
  # ══════════════════════════════════════════════

  Scenario: Successfully retrieve a reservation by existing ID
    Given a reservation exists with the following data:
      | checkIn             | checkOut            | guestsCount |
      | 2026-06-10T14:00:00 | 2026-06-15T11:00:00 | 2           |
    When I send a GET request to "/reservation/{id}"
    Then the system responds with status code 200
    And the response contains a generated reservation ID

  # ══════════════════════════════════════════════
  # SCENARIO 4: Update a reservation
  # ══════════════════════════════════════════════

  Scenario: Successfully update an existing reservation
    Given a reservation exists with the following data:
      | checkIn             | checkOut            | guestsCount |
      | 2026-07-01T14:00:00 | 2026-07-05T11:00:00 | 2           |
    When I update the reservation with the following data:
      | checkIn             | checkOut            | guestsCount |
      | 2026-07-01T14:00:00 | 2026-07-05T11:00:00 | 4           |
    Then the system responds with status code 200
    And the response contains guestsCount 4

  # ══════════════════════════════════════════════
  # SCENARIO 5: Delete a reservation
  # ══════════════════════════════════════════════

  Scenario: Successfully delete an existing reservation
    Given a reservation exists with the following data:
      | checkIn             | checkOut            | guestsCount |
      | 2026-08-01T14:00:00 | 2026-08-05T11:00:00 | 2           |
    When I send a DELETE request to "/reservation/{id}"
    Then the system responds with status code 200
    And the reservation no longer exists in the system