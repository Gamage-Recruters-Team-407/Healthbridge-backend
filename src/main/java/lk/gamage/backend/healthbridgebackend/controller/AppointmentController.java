package lk.gamage.backend.healthbridgebackend.controller;

import lk.gamage.backend.healthbridgebackend.dto.request.AppointmentRequest;
import lk.gamage.backend.healthbridgebackend.dto.response.AppointmentResponse;
import lk.gamage.backend.healthbridgebackend.model.Appointment;
import lk.gamage.backend.healthbridgebackend.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002", "http://localhost:3003"})
public class AppointmentController {
    private final AppointmentService service;
    public AppointmentController(AppointmentService service) { this.service = service; }
    @GetMapping public List<AppointmentResponse> getAll(@RequestParam(required = false) String patientId, @RequestParam(required = false) String status) { return service.find(patientId, status).stream().map(this::toResponse).toList(); }
    @GetMapping("/{id}") public AppointmentResponse get(@PathVariable String id) { return toResponse(service.findById(id)); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public AppointmentResponse create(@RequestBody AppointmentRequest request) { return toResponse(service.create(request)); }
    @PutMapping("/{id}") public AppointmentResponse reschedule(@PathVariable String id, @RequestBody AppointmentRequest request) { return toResponse(service.reschedule(id, request)); }
    @PatchMapping("/{id}/cancel") public AppointmentResponse cancel(@PathVariable String id, @RequestParam(required = false) String reason) { return toResponse(service.cancel(id, reason)); }
    private AppointmentResponse toResponse(Appointment a) { return new AppointmentResponse(a.getId(), a.getPatientId(), a.getDoctorId(), a.getDoctorName(), a.getDoctorSpecialization(), a.getHospital(), a.getAppointmentDate(), a.getAppointmentTime(), a.getAppointmentType(), a.getReason(), a.getStatus(), a.getCancellationReason(), a.getCreatedAt(), a.getUpdatedAt()); }
}
