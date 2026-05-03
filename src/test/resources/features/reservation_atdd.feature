Feature: Reservation acceptance criteria (ATDD)

  # ══════════════════════════════════════════════════════
  # HU-01 AC-01.3 — Habitación no disponible
  # ══════════════════════════════════════════════════════

  Scenario: Client cannot book a room marked as unavailable
    Given a guest is registered in the system
    And a room exists but is marked as unavailable
    When the client tries to create a reservation for that room
    Then the system rejects the reservation with status 400
    And the error indicates the room is not available

  # ══════════════════════════════════════════════════════
  # HU-01 AC-01.6 — Huésped o habitación inexistente
  # ══════════════════════════════════════════════════════

  Scenario: Client cannot create a reservation with a non-existent guest
    Given a room is available in the system
    When the client tries to create a reservation with a non-existent guest ID
    Then the system rejects the reservation with status 404

  Scenario: Client cannot create a reservation with a non-existent room
    Given a guest is registered in the system
    When the client tries to create a reservation with a non-existent room ID
    Then the system rejects the reservation with status 404

  # ══════════════════════════════════════════════════════
  # HU-02 AC-02.3 — Filtro por huésped (completo)
  # ══════════════════════════════════════════════════════

  Scenario: Administrator can filter reservations by guest
    Given a guest and a room are available in the system
    And the client has 2 active reservations:
      | checkIn             | checkOut            | guestsCount |
      | 2026-09-01T14:00:00 | 2026-09-05T11:00:00 | 2           |
      | 2026-10-01T14:00:00 | 2026-10-05T11:00:00 | 3           |
    When the client queries their reservations by guest ID
    Then the system returns a list with 2 reservations
    And all reservations belong to the same guest

  # ══════════════════════════════════════════════════════
  # HU-02 AC-02.4 — Filtro por habitación
  # ══════════════════════════════════════════════════════

  Scenario: Administrator can filter reservations by room
    Given a guest and a room are available in the system
    And the room has an active reservation from "2026-09-01T14:00:00" to "2026-09-05T11:00:00"
    When the administrator queries reservations by room ID
    Then the system returns a list with 1 reservations
    And all reservations belong to the same room

  # ══════════════════════════════════════════════════════
  # HU-03 AC-03.2 — Error 404 ID inexistente
  # ══════════════════════════════════════════════════════

  Scenario: Client receives 404 when querying a non-existent reservation ID
    Given there are no reservations in the system
    When the client tries to retrieve reservation with ID "00000000-0000-0000-0000-000000000000"
    Then the system rejects the request with status 404


  # ══════════════════════════════════════════════════════
  # HU-04 AC-04.4 — Solapamiento en actualización
  # ══════════════════════════════════════════════════════

  Scenario: Client cannot update a reservation to dates that overlap another reservation
    Given a guest and a room are available in the system
    And the room is already reserved from "2026-10-01T14:00:00" to "2026-10-05T11:00:00"
    And the client has an active reservation from "2026-09-01T14:00:00" to "2026-09-05T11:00:00"
    When the client updates the reservation to dates "2026-10-03T14:00:00" to "2026-10-07T11:00:00"
    Then the system rejects the reservation with status 400
    And the error indicates the room is not available in that time range

  # ══════════════════════════════════════════════════════
  # HU-04 AC-04.5 — No solapa consigo misma
  # ══════════════════════════════════════════════════════

  Scenario: Client can update guest count keeping the same dates without overlap error
    Given a guest and a room are available in the system
    And the client has an active reservation for 2 guests from "2026-09-01T14:00:00" to "2026-09-05T11:00:00"
    When the client updates the reservation to 3 guests
    Then the system confirms the update with status 200
    And the reservation now shows 3 guests
    And the reservation ID remains the same

  # ══════════════════════════════════════════════════════
  # HU-05 AC-05.3 — Room disponible tras cancelación
  # ══════════════════════════════════════════════════════

  Scenario: Room becomes available for new reservations after cancellation
    Given a guest and a room are available in the system
    And the client has an active reservation from "2026-09-01T14:00:00" to "2026-09-05T11:00:00"
    When the client cancels the reservation
    Then the system confirms the cancellation with status 200
    And another client can book the same room for the same dates successfully
