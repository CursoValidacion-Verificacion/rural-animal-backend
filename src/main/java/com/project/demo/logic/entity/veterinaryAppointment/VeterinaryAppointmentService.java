package com.project.demo.logic.entity.veterinaryAppointment;

import com.project.demo.logic.entity.calendar.CalendarService;
import com.project.demo.logic.entity.calendar.EventDTO;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.entity.veterinary.TblVeterinary;
import com.project.demo.logic.entity.veterinary.TblVeterinaryRepository;
import com.project.demo.logic.entity.veterinary.VeterinaryAvailabilityDto;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@Service
public class VeterinaryAppointmentService {

    private final TblVeterinaryAppointmentRepository veterinaryAppointmentRepository;
    private final TblVeterinaryRepository veterinaryRepository;
    private static final int APPOINTMENT_DURATION = 30;
    private static final LocalTime START_TIME = LocalTime.of(8, 0);
    private static final LocalTime END_TIME = LocalTime.of(17, 0);
    private final CalendarService calendarService;
    private final UserRepository userRepository;

    public VeterinaryAppointmentService(
            TblVeterinaryAppointmentRepository veterinaryAppointmentRepository,
            TblVeterinaryRepository veterinaryRepository,
            CalendarService calendarService,
            UserRepository userRepository) {
        this.veterinaryAppointmentRepository = veterinaryAppointmentRepository;
        this.veterinaryRepository = veterinaryRepository;
        this.calendarService = calendarService;
        this.userRepository = userRepository;
    }

    /**
     * Convierte un objeto de tipo TblVeterinaryAppointment en un objeto de tipo VeterinaryAppointmentDto.
     *
     * @param appointment Objeto de tipo TblVeterinaryAppointment que contiene la información de la cita veterinaria.
     * @return Objeto de tipo VeterinaryAppointmentDto que representa los datos de la cita convertidos.
     */
    private VeterinaryAppointmentDto mapToDto(TblVeterinaryAppointment appointment) {
        var dto = new VeterinaryAppointmentDto();
        dto.setId(appointment.getId());
        dto.setVeterinaryName(appointment.getVeterinary().getName());
        dto.setFirstSurname(appointment.getVeterinary().getLastName1());
        dto.setSecondSurname(appointment.getVeterinary().getLastName2());
        dto.setEmail(appointment.getVeterinary().getEmail());
        dto.setSpeciality(appointment.getVeterinary().getSpeciallity());
        dto.setStatus(appointment.getState());
        dto.setStartDate(appointment.getStartDate());
        dto.setEndDate(appointment.getEndDate());
        dto.setFullName(String.format("%s %s %s",
                appointment.getUser().getName(),
                appointment.getUser().getLastName1(),
                appointment.getUser().getLastName2()));
        return dto;
    }

    /**
     * Genera una lista de TimeSlotDto para un día específico. Los horarios se dividen
     * en intervalos según la duración especificada de las citas, comenzando desde una
     * hora de inicio definida hasta una hora de finalización definida.
     *
     * @param date La fecha que representa el día para el cual se quieren generar los intervalos de tiempo.
     *             Se utiliza la parte del día para determinar la fecha, mientras que la hora se adapta
     *             al horario de inicio y final predefinido.
     * @return Una lista de objetos TimeSlotDto que representan los intervalos de tiempo diarios disponibles.
     */
    private List<TimeSlotDto> generateDailySlots(LocalDateTime date) {
        List<TimeSlotDto> slots = new ArrayList<>();
        LocalDateTime currentSlot = date.with(START_TIME);
        LocalDateTime endSlot = date.with(END_TIME);

        while (currentSlot.isBefore(endSlot)) {
            LocalDateTime slotEnd = currentSlot.plusMinutes(APPOINTMENT_DURATION);

            TimeSlotDto slot = new TimeSlotDto();
            slot.setStartTime(currentSlot);
            slot.setEndTime(slotEnd);
            slots.add(slot);

            currentSlot = slotEnd;
        }

        return slots;
    }

    /**
     * Convierte una entidad veterinaria al DTO usado por la vista de disponibilidad.
     *
     * @param veterinary La entidad veterinaria a convertir.
     * @return DTO con la información pública del veterinario.
     */
    private VeterinaryAvailabilityDto mapToVeterinaryAvailabilityDto(
            TblVeterinary veterinary) {
        var dto = new VeterinaryAvailabilityDto();
        dto.setId(veterinary.getId());
        dto.setName(veterinary.getName());
        dto.setLastName1(veterinary.getLastName1());
        dto.setLastName2(veterinary.getLastName2());
        dto.setSpeciality(veterinary.getSpeciallity());
        return dto;
    }

    /**
     * Obtiene los intervalos de tiempo disponibles para un día específico, considerando la disponibilidad de veterinarios.
     * La disponibilidad se calcula en memoria con las citas ya cargadas para el rango completo,
     * evitando consultas repetidas por slot y por veterinario.
     *
     * @param date La fecha en la que se desea conocer los intervalos de tiempo disponibles.
     * @param allVeterinarians Lista completa de veterinarios disponibles en el sistema.
     * @param appointmentsInRange Lista de citas dentro del rango solicitado.
     * @return Una lista de objetos TimeSlotDto que representan los intervalos de tiempo disponibles, cada uno con una lista
     * de veterinarios disponibles durante ese intervalo.
     */
    private List<TimeSlotDto> getAvailableTimeSlots(
            LocalDateTime date,
            List<TblVeterinary> allVeterinarians,
            List<TblVeterinaryAppointment> appointmentsInRange) {
        List<TimeSlotDto> timeSlots = generateDailySlots(date);

        for (TimeSlotDto slot : timeSlots) {
            Set<Long> occupiedVeterinaryIds = appointmentsInRange.stream()
                    .filter(appointment ->
                            appointment.getStartDate().isBefore(slot.getEndTime()) &&
                            appointment.getEndDate().isAfter(slot.getStartTime()))
                    .map(appointment -> appointment.getVeterinary().getId())
                    .collect(Collectors.toSet());

            List<VeterinaryAvailabilityDto> availableVets = allVeterinarians.stream()
                    .filter(veterinary -> !occupiedVeterinaryIds.contains(veterinary.getId()))
                    .map(this::mapToVeterinaryAvailabilityDto)
                    .collect(Collectors.toList());

            slot.setAvailableVeterinarians(availableVets);
        }

        return timeSlots.stream()
                .filter(slot -> !slot.getAvailableVeterinarians().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Verifica si un veterinario está disponible para un nuevo turno
     * en un intervalo de tiempo especificado.
     *
     * @param vetId     El identificador único del veterinario.
     * @param startTime La fecha y hora de inicio del intervalo.
     * @param endTime   La fecha y hora de fin del intervalo.
     * @return true si el veterinario está disponible, false en caso contrario.
     */
    private boolean isVeterinaryAvailable(Long vetId, LocalDateTime startTime, LocalDateTime endTime) {
        List<TblVeterinaryAppointment> overlappingAppointments =
                veterinaryAppointmentRepository.findOverlappingAppointments(vetId, startTime, endTime);
        return overlappingAppointments.isEmpty();
    }

    /**
     * Determina si un día dado es un día laboral de acuerdo con la semana estándar.
     *
     * @param date La fecha que se desea evaluar como un objeto LocalDateTime.
     * @return true si el día proporcionado es un día laboral (de lunes a viernes), false si es un fin de semana (sábado o domingo).
     */
    private boolean isWorkingDay(LocalDateTime date) {
        return date.getDayOfWeek().getValue() < 6;
    }

    /**
     * Recupera una página de citas veterinarias asociadas a un usuario específico.
     *
     * @param userId   El identificador único del usuario cuyas citas se desean recuperar. No puede ser nulo.
     * @param pageable Objeto que especifica la información de paginación, como el tamaño de la página y el número.
     * @return Un objeto Page que contiene las citas veterinarias del usuario especificado en formato DTO.
     * @throws ResponseStatusException Si el userId es nulo o si el número de página solicitado excede el número total de páginas disponibles.
     */
    public Page<VeterinaryAppointmentDto> getUserAppointments(Long userId, Pageable pageable) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID cannot be null");
        }

        Page<TblVeterinaryAppointment> appointments = veterinaryAppointmentRepository.findByUserId(userId, pageable);

        if (appointments.getTotalElements() > 0 && appointments.getContent().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page number exceeds available pages");
        }

        return appointments.map(this::mapToDto);
    }

    /**
     * Obtiene una lista de fechas disponibles dentro de un rango específico.
     * Para cada día laborable en el rango, se crea un objeto AvailabilityDto que contiene
     * los horarios disponibles para ese día.
     *
     * @param startDate La fecha y hora de inicio del rango. No se incluirán las fechas anteriores a esta.
     * @param endDate   La fecha y hora de fin del rango. No se incluirán las fechas posteriores a esta.
     * @return Una lista de objetos AvailabilityDto donde cada uno representa un día laborable junto
     * con sus horarios disponibles dentro del rango especificado.
     */
    public List<AvailabilityDto> getAvailableDates(LocalDateTime startDate, LocalDateTime endDate) {
        List<AvailabilityDto> availabilities = new ArrayList<>();
        var allVeterinarians = veterinaryRepository.findAll();
        List<TblVeterinaryAppointment> appointmentsInRange =
                veterinaryAppointmentRepository.findAppointmentsInRange(startDate, endDate);
        LocalDateTime currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            if (isWorkingDay(currentDate)) {
                AvailabilityDto availability = new AvailabilityDto();
                availability.setDate(currentDate);
                availability.setAvailableSlots(
                        getAvailableTimeSlots(currentDate, allVeterinarians, appointmentsInRange)
                );
                availabilities.add(availability);
            }
            currentDate = currentDate.plusDays(1);
        }

        return availabilities;
    }

    /**
     * Crea una nueva cita veterinaria en el sistema.
     *
     * @param appointmentDTO Un objeto CreateAppointmentDto que contiene los detalles de la cita a crear,
     *                       incluyendo el ID del veterinario, la fecha de inicio y la fecha de fin.
     * @param userId         ID del usuario para el cual se creará la cita.
     * @return Un objeto VeterinaryAppointmentDto que representa la cita veterinaria creada.
     * @throws ResponseStatusException Si el veterinario no está disponible en el horario seleccionado
     *                                 o si ocurre un error durante la creación de la cita.
     */
    @Transactional
    public VeterinaryAppointmentDto createAppointment(CreateAppointmentDto appointmentDTO, Long userId) {
        if (!isVeterinaryAvailable(
                appointmentDTO.getVeterinaryId(),
                appointmentDTO.getStartDate(),
                appointmentDTO.getEndDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El veterinario no está disponible en ese horario"
            );
        }

        try {
            TblUser user = userRepository.getReferenceById(userId);
            TblVeterinaryAppointment appointment = new TblVeterinaryAppointment();
            appointment.setVeterinary(veterinaryRepository.getReferenceById(appointmentDTO.getVeterinaryId()));
            appointment.setUser(user);
            appointment.setStartDate(appointmentDTO.getStartDate());
            appointment.setEndDate(appointmentDTO.getEndDate());
            appointment.setState("Confirmada");

            appointment = veterinaryAppointmentRepository.save(appointment);

            EventDTO eventDTO = new EventDTO();
            eventDTO.setSummary("Cita Veterinaria - " + appointment.getVeterinary().getName());
            eventDTO.setDescription(String.format(
                    "Cita con %s Dr. %s %s",
                    appointment.getVeterinary().getSpeciallity(),
                    appointment.getVeterinary().getName(),
                    appointment.getVeterinary().getLastName1()
            ));
            eventDTO.setStartTime(appointment.getStartDate());
            eventDTO.setEndTime(appointment.getEndDate());

            if (user.getGoogleAccessToken() != null && user.getGoogleRefreshToken() != null) {
                calendarService.createEventInUserCalendar(userId, eventDTO);
                calendarService.createEventInSystemCalendar(eventDTO);
            }

            return mapToDto(appointment);

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al crear la cita: " + e.getMessage()
            );
        }
    }

    /**
     * Recupera todas las citas veterinarias almacenadas en el repositorio.
     *
     * @return una lista de objetos VeterinaryAppointmentDto que representan todas las citas veterinarias
     * almacenadas en el sistema.
     */
    public List<VeterinaryAppointmentDto> getAll() {
        return veterinaryAppointmentRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
}
