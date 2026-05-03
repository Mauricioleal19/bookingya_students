package com.project.bookingya.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.project.bookingya.dtos.ReservationDto;
import com.project.bookingya.entities.GuestEntity;
import com.project.bookingya.entities.ReservationEntity;
import com.project.bookingya.entities.RoomEntity;
import com.project.bookingya.exceptions.BusinessRuleException;
import com.project.bookingya.exceptions.EntityNotExistsException;
import com.project.bookingya.models.Reservation;
import com.project.bookingya.repositories.IGuestRepository;
import com.project.bookingya.repositories.IReservationRepository;
import com.project.bookingya.repositories.IRoomRepository;
import com.project.bookingya.shared.Constants;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias - ReservationService")
class ReservationServiceTest {

    // ── Mocks ────────────────────────────────────────────────────────────────
    @Mock private IReservationRepository reservationRepository;
    @Mock private IRoomRepository        roomRepository;
    @Mock private IGuestRepository       guestRepository;
    @Mock private ModelMapper            mapper;

    @InjectMocks
    private ReservationService reservationService;

    // ── Datos comunes ────────────────────────────────────────────────────────
    private UUID reservationId1;
    private UUID reservationId2;
    private UUID roomId;
    private UUID guestId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private ReservationDto dto;
    private RoomEntity room;

    @BeforeEach
    void setUp() {
        reservationId1 = UUID.randomUUID();
        reservationId2 = UUID.randomUUID();
        roomId  = UUID.randomUUID();
        guestId = UUID.randomUUID();
        checkIn  = LocalDateTime.of(2026, 6, 1, 14, 0);
        checkOut = LocalDateTime.of(2026, 6, 5, 11, 0);

        // DTO compartido para pruebas de create/update
        dto = new ReservationDto();
        dto.setRoomId(roomId);
        dto.setGuestId(guestId);
        dto.setCheckIn(checkIn);
        dto.setCheckOut(checkOut);
        dto.setGuestsCount(2);

        // Habitación disponible con capacidad suficiente
        room = new RoomEntity();
        room.setId(roomId);
        room.setAvailable(true);
        room.setMaxGuests(4);
    }

    // ===================================
    // getAll() - Consulta de todas las reservas
    // ===================================

    @Test
    void getAll() {
        ReservationEntity entity1 = new ReservationEntity();
        entity1.setId(reservationId1);
        entity1.setGuestId(guestId);
        entity1.setRoomId(roomId);
        entity1.setCheckIn(checkIn);
        entity1.setCheckOut(checkOut);
        entity1.setGuestsCount(2);
        entity1.setNotes("Reserva 1");

        ReservationEntity entity2 = new ReservationEntity();
        entity2.setId(reservationId2);
        entity2.setGuestId(guestId);
        entity2.setRoomId(roomId);
        entity2.setCheckIn(checkIn);
        entity2.setCheckOut(checkOut);
        entity2.setGuestsCount(3);
        entity2.setNotes("Reserva 2");

        Reservation model1 = new Reservation();
        model1.setId(reservationId1);
        model1.setGuestId(guestId);
        model1.setRoomId(roomId);

        Reservation model2 = new Reservation();
        model2.setId(reservationId2);
        model2.setGuestId(guestId);
        model2.setRoomId(roomId);

        List<ReservationEntity> entities = Arrays.asList(entity1, entity2);
        List<Reservation> models = Arrays.asList(model1, model2);

        when(reservationRepository.findAll()).thenReturn(entities);
        when(mapper.map(eq(entities), any(Type.class))).thenReturn(models);

        List<Reservation> result = reservationService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(reservationId1, result.get(0).getId());
        assertEquals(reservationId2, result.get(1).getId());
        verify(reservationRepository).findAll();
    }

    // ===================================
    // create() - Creación de una reserva
    // ===================================

    @Test
    void create() {

            // Objeto que simula lo que se va a guardar en BD
            ReservationEntity entityToSave = new ReservationEntity();

            // Objeto que simula lo que la BD devuelve tras guardar (ya con ID asignado)
            ReservationEntity savedEntity = new ReservationEntity();
            savedEntity.setId(UUID.randomUUID());

            // Objeto final que esperamos recibir del servicio
            Reservation expected = new Reservation();
            expected.setId(savedEntity.getId());
            expected.setRoomId(roomId);
            expected.setGuestId(guestId);

            // "Cuando busquen el cuarto por ID → devuelve el cuarto de prueba"
            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(room));

            // "Cuando busquen el huésped por ID → devuelve un huésped de prueba"
            when(guestRepository.findById(guestId))
                    .thenReturn(Optional.of(new GuestEntity()));

            // "Cuando verifiquen solapamiento del cuarto → no hay conflicto"
            when(reservationRepository.existsOverlappingReservationForRoom(
                    eq(roomId), eq(checkIn), eq(checkOut), isNull()))
                    .thenReturn(false);

            // "Cuando verifiquen solapamiento del huésped → no hay conflicto"
            when(reservationRepository.existsOverlappingReservationForGuest(
                    eq(guestId), eq(checkIn), eq(checkOut), isNull()))
                    .thenReturn(false);

            // "Cuando el mapper convierta el DTO a entidad → devuelve entityToSave"
            when(mapper.map(dto, ReservationEntity.class))
                    .thenReturn(entityToSave);

            // "Cuando guarden en BD → devuelve savedEntity (con ID generado)"
            when(reservationRepository.saveAndFlush(entityToSave))
                    .thenReturn(savedEntity);

            // "Cuando el mapper convierta la entidad guardada a modelo → devuelve expected"
            when(mapper.map(savedEntity, Reservation.class))
                    .thenReturn(expected);

            // ══════════════════════════════════════════════════
            // 2. ACT (Actuar)
            //    Llamamos al método real que queremos probar.
            // ══════════════════════════════════════════════════

            Reservation result = reservationService.create(dto);

            // ══════════════════════════════════════════════════
            // 3. ASSERT (Verificar)
            //    Comprobamos que el resultado sea el esperado
            //    y que se llamaron los métodos correctos.
            // ══════════════════════════════════════════════════

            // ¿El resultado no es nulo?
            assertThat(result).isNotNull();

            // ¿El ID del resultado coincide con el que devolvió la BD?
            assertThat(result.getId()).isEqualTo(savedEntity.getId());

            // ¿El cuarto y el huésped son los correctos?
            assertThat(result.getRoomId()).isEqualTo(roomId);
            assertThat(result.getGuestId()).isEqualTo(guestId);

            // ¿El servicio realmente intentó guardar en la BD?
            verify(reservationRepository).saveAndFlush(entityToSave);
    }

    // ==================================================
    // update() - Actualización de una reserva existente
    // ==================================================
    @Test
    void update() {

        // ID de la reserva que ya existe en la BD
        UUID existingReservationId = UUID.randomUUID();

        // Simulamos la reserva que ya está guardada en BD
        ReservationEntity existingEntity = new ReservationEntity();
        existingEntity.setId(existingReservationId);
        existingEntity.setRoomId(roomId);
        existingEntity.setGuestId(guestId);
        existingEntity.setCheckIn(checkIn);
        existingEntity.setCheckOut(checkOut);
        existingEntity.setGuestsCount(2);

        // Simulamos cómo queda la entidad después de guardar los cambios
        ReservationEntity updatedEntity = new ReservationEntity();
        updatedEntity.setId(existingReservationId);
        updatedEntity.setRoomId(roomId);       // ← ahora con todos los campos
        updatedEntity.setGuestId(guestId);     // ← igual que existingEntity
        updatedEntity.setCheckIn(checkIn);     // ← para que el mock de saveAndFlush coincida
        updatedEntity.setCheckOut(checkOut);
        updatedEntity.setGuestsCount(4);       // ← solo este dato cambia

        // Resultado final que esperamos recibir del servicio
        Reservation expected = new Reservation();
        expected.setId(existingReservationId);
        expected.setRoomId(roomId);
        expected.setGuestId(guestId);
        expected.setGuestsCount(4); // refleja el cambio

        // "Cuando busquen la reserva por ID → devuelve la reserva existente"
        when(reservationRepository.findById(existingReservationId))
                .thenReturn(Optional.of(existingEntity));

        // "Cuando busquen el cuarto → devuelve el cuarto disponible"
        when(roomRepository.findById(roomId))
                .thenReturn(Optional.of(room));

        // "Cuando busquen el huésped → devuelve un huésped válido"
        when(guestRepository.findById(guestId))
                .thenReturn(Optional.of(new GuestEntity()));

        // "Cuando verifiquen solapamiento del cuarto → no hay conflicto"
        // Nota: se pasa existingReservationId para excluir la reserva actual de la búsqueda
        when(reservationRepository.existsOverlappingReservationForRoom(
                eq(roomId), eq(checkIn), eq(checkOut), eq(existingReservationId)))
                .thenReturn(false);

        // "Cuando verifiquen solapamiento del huésped → no hay conflicto"
        when(reservationRepository.existsOverlappingReservationForGuest(
                eq(guestId), eq(checkIn), eq(checkOut), eq(existingReservationId)))
                .thenReturn(false);

        // ✅ LÍNEA NUEVA: mockea la primera llamada al mapper (DTO → entidad existente)
        // El servicio usa esta llamada para copiar los nuevos datos sobre la entidad
        doNothing().when(mapper).map(dto, existingEntity);

        // "Cuando guarden los cambios → devuelve la entidad actualizada"
        when(reservationRepository.saveAndFlush(existingEntity))
                .thenReturn(updatedEntity);

        // "Cuando el mapper convierta la entidad actualizada → devuelve el modelo esperado"
        when(mapper.map(updatedEntity, Reservation.class))
                .thenReturn(expected);

        // ══════════════════════════════════════════════════
        // 2. ACT (Actuar)
        // ══════════════════════════════════════════════════

        Reservation result = reservationService.update(dto, existingReservationId);

        // ══════════════════════════════════════════════════
        // 3. ASSERT (Verificar)
        // ══════════════════════════════════════════════════

        // ¿El resultado no es nulo?
        assertThat(result).isNotNull();

        // ¿El ID sigue siendo el mismo? (no se debe crear una reserva nueva)
        assertThat(result.getId()).isEqualTo(existingReservationId);

        // ¿Los datos actualizados se reflejan correctamente?
        assertThat(result.getGuestsCount()).isEqualTo(4);
        assertThat(result.getRoomId()).isEqualTo(roomId);
        assertThat(result.getGuestId()).isEqualTo(guestId);

        // ¿El servicio realmente guardó los cambios en BD?
        verify(reservationRepository).saveAndFlush(existingEntity);
    }

    // ==================================================
    // delete() - Eliminación (cancelación) de una reserva
    // ==================================================
    @Test
    void delete() {

        // ID de la reserva que queremos eliminar
        UUID existingReservationId = UUID.randomUUID();

        // Simulamos la reserva que está guardada en BD
        ReservationEntity existingEntity = new ReservationEntity();
        existingEntity.setId(existingReservationId);
        existingEntity.setRoomId(roomId);
        existingEntity.setGuestId(guestId);
        existingEntity.setCheckIn(checkIn);
        existingEntity.setCheckOut(checkOut);
        existingEntity.setGuestsCount(2);

        // "Cuando busquen la reserva por ID → devuelve la reserva existente"
        when(reservationRepository.findById(existingReservationId))
                .thenReturn(Optional.of(existingEntity));

        // "Cuando intenten eliminar → no hace nada (es void)"
        doNothing().when(reservationRepository).delete(existingEntity);

        // "Cuando llamen flush → no hace nada (es void)"
        doNothing().when(reservationRepository).flush();

        // ══════════════════════════════════════════════════
        // 2. ACT (Actuar)
        // ══════════════════════════════════════════════════

        // delete() no retorna nada, solo ejecutamos el método
        reservationService.delete(existingReservationId);

        // ══════════════════════════════════════════════════
        // 3. ASSERT (Verificar)
        // ══════════════════════════════════════════════════

        // ¿El servicio realmente eliminó la reserva de la BD?
        verify(reservationRepository).delete(existingEntity);

    }
    // ==================================================
    // getById(() - Obtención de una reserva por ID
    // ==================================================
    @Test
    void getById() {

        // ID de la reserva que queremos buscar
        UUID existingReservationId = UUID.randomUUID();

        // Simulamos la reserva que está guardada en BD
        ReservationEntity existingEntity = new ReservationEntity();
        existingEntity.setId(existingReservationId);
        existingEntity.setRoomId(roomId);
        existingEntity.setGuestId(guestId);
        existingEntity.setCheckIn(checkIn);
        existingEntity.setCheckOut(checkOut);
        existingEntity.setGuestsCount(2);

        // Resultado final que esperamos recibir del servicio
        Reservation expected = new Reservation();
        expected.setId(existingReservationId);
        expected.setRoomId(roomId);
        expected.setGuestId(guestId);
        expected.setCheckIn(checkIn);
        expected.setCheckOut(checkOut);
        expected.setGuestsCount(2);

        // "Cuando busquen la reserva por ID → devuelve la reserva existente"
        when(reservationRepository.findById(existingReservationId))
                .thenReturn(Optional.of(existingEntity));

        // "Cuando el mapper convierta la entidad al modelo → devuelve el modelo esperado"
        when(mapper.map(existingEntity, Reservation.class))
                .thenReturn(expected);

        // ══════════════════════════════════════════════════
        // 2. ACT (Actuar)
        // ══════════════════════════════════════════════════

        Reservation result = reservationService.getById(existingReservationId);

        // ══════════════════════════════════════════════════
        // 3. ASSERT (Verificar)
        // ══════════════════════════════════════════════════

        // ¿El resultado no es nulo?
        assertThat(result).isNotNull();

        // ¿El ID retornado coincide con el que buscamos?
        assertThat(result.getId()).isEqualTo(existingReservationId);

        // ¿Los datos de la reserva son los correctos?
        assertThat(result.getRoomId()).isEqualTo(roomId);
        assertThat(result.getGuestId()).isEqualTo(guestId);
        assertThat(result.getCheckIn()).isEqualTo(checkIn);
        assertThat(result.getCheckOut()).isEqualTo(checkOut);
        assertThat(result.getGuestsCount()).isEqualTo(2);

        // ¿El servicio buscó la reserva en BD con el ID correcto?
        verify(reservationRepository).findById(existingReservationId);

    }
    // ==================================================
    // getByGuestId(() - Consulta de reservas por usuario
    // ==================================================
    @Test
    void getByGuestId() {

        // ══════════════════════════════════════════════════
        // 1. ARRANGE (Preparar)
        // ══════════════════════════════════════════════════

        // Simulamos dos reservas que tiene el huésped en BD
        ReservationEntity entity1 = new ReservationEntity();
        entity1.setId(UUID.randomUUID());
        entity1.setGuestId(guestId);
        entity1.setRoomId(roomId);
        entity1.setCheckIn(checkIn);
        entity1.setCheckOut(checkOut);
        entity1.setGuestsCount(2);

        ReservationEntity entity2 = new ReservationEntity();
        entity2.setId(UUID.randomUUID());
        entity2.setGuestId(guestId);
        entity2.setRoomId(UUID.randomUUID()); // diferente cuarto
        entity2.setCheckIn(checkIn.plusDays(10));
        entity2.setCheckOut(checkOut.plusDays(10));
        entity2.setGuestsCount(3);

        List<ReservationEntity> entities = Arrays.asList(entity1, entity2);

        // Modelos que esperamos recibir tras la conversión del mapper
        Reservation model1 = new Reservation();
        model1.setId(entity1.getId());
        model1.setGuestId(guestId);
        model1.setRoomId(entity1.getRoomId());

        Reservation model2 = new Reservation();
        model2.setId(entity2.getId());
        model2.setGuestId(guestId);
        model2.setRoomId(entity2.getRoomId());

        List<Reservation> expectedModels = Arrays.asList(model1, model2);

        // "Cuando busquen reservas por guestId → devuelve las dos entidades"
        when(reservationRepository.findByGuestId(guestId))
                .thenReturn(entities);

        // "Cuando el mapper convierta la lista de entidades → devuelve la lista de modelos"
        when(mapper.map(eq(entities), any(Type.class)))
                .thenReturn(expectedModels);

        // ══════════════════════════════════════════════════
        // 2. ACT (Actuar)
        // ══════════════════════════════════════════════════

        List<Reservation> result = reservationService.getByGuestId(guestId);

        // ══════════════════════════════════════════════════
        // 3. ASSERT (Verificar)
        // ══════════════════════════════════════════════════

        // ¿El resultado no es nulo?
        assertThat(result).isNotNull();

        // ¿Se retornaron exactamente las reservas del huésped?
        assertThat(result).hasSize(2);

        // ¿Los IDs de cada reserva son los correctos?
        assertThat(result.get(0).getId()).isEqualTo(entity1.getId());
        assertThat(result.get(1).getId()).isEqualTo(entity2.getId());

        // ¿El servicio consultó por el guestId correcto?
        verify(reservationRepository).findByGuestId(guestId);

    }
}