package com.pm.patientservice.controller;

import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.dto.validators.CreatePatientValidationGroup;
import com.pm.patientservice.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients") // http://localhost:4000/patients
@Tag(name = "Patient", description = "API for managing Patients")
public class PatientController {
    private final PatientService patientService;

    @Autowired
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @Operation(summary = "Get Patients", description = "Retrieve a list of all patients.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of patients.",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = "[{\"id\": \"123e4567-e89b-12d3-a456-426614174000\", \"name\": \"John Doe\", \"email\": \"johndoe@example.com\", \"address\": \"123 Main Street, Springfield\", \"dateOfBirth\": \"1990-05-15\"}]"))
            ),
    })
    public ResponseEntity<List<PatientResponseDTO>> getPatients() {
        List<PatientResponseDTO> patients = patientService.getPatients();
        return ResponseEntity
                .status(HttpStatus.OK).
                body(patients);
    }

    @PostMapping
    @Operation(summary = "Create a new Patient", description = "Creates a new patient record. To create a patient, ensure all required fields are valid.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Patient successfully created.",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = "{\"id\": \"123e4567-e89b-12d3-a456-426614174000\", \"name\": \"John Doe\", \"email\": \"johndoe@example.com\", \"address\": \"123 Main Street, Springfield\", \"dateOfBirth\": \"1990-05-15\"}"))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or validation error."),
            @ApiResponse(responseCode = "400", description = "Email already exists.",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = "{\"message\": \"Email already exists\"}"))
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Example of PatientRequestDTO",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = "{\"name\": \"John Doe\", \"email\": \"johndoe@example.com\", \"address\": \"123 Main Street, Springfield\", \"dateOfBirth\": \"1990-05-15\", \"registeredDate\": \"2023-10-20\"}")))
    public ResponseEntity<PatientResponseDTO> createPatient(
            @Validated({Default.class, CreatePatientValidationGroup.class})
            @RequestBody PatientRequestDTO patientRequestDTO) {

        PatientResponseDTO patientResponseDTO = patientService.createPatient(patientRequestDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(patientResponseDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a Patient", description = "Updates an existing patient record. Ensures the patient exists before performing updates.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patient successfully updated.",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = "{\"id\": \"123e4567-e89b-12d3-a456-426614174000\", \"name\": \"John Doe Up\", \"email\": \"john.doe.updated@example.com\", \"address\": \"456 Elm Street, Springfield\", \"dateOfBirth\": \"1985-10-25\"}"))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or validation error."),
            @ApiResponse(responseCode = "400", description = "Patient not found.",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = "{\"message\": \"Patient not found\"}"))
            ),
            @ApiResponse(responseCode = "400", description = "Email already exists.",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = "{\"message\": \"Email already exists\"}"))
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Example of PatientRequestDTO for update",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = "{\"name\": \"John Updated\", \"email\": \"john.updated@example.com\", \"address\": \"789 Pine Street\", \"dateOfBirth\": \"1992-12-20\"}")))
    public ResponseEntity<PatientResponseDTO> updatePatient(
            @Validated({Default.class})
            @RequestBody PatientRequestDTO patientRequestDTO,
            @Parameter(description = "Unique identifier of the patient to update.", required = true)
            @PathVariable UUID id) {

        PatientResponseDTO patientResponseDTO = patientService.updatePatient(id, patientRequestDTO);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(patientResponseDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a Patient", description = "Deletes a patient by their unique ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Patient successfully deleted."),
            @ApiResponse(responseCode = "400", description = "Patient not found.",
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = "{\"message\": \"Patient not found\"}"))
            )
    })
    public ResponseEntity<Void> deletePatient(
            @Parameter(description = "Unique identifier of the patient to delete.", required = true)
            @PathVariable UUID id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build(); // code - 204
    }
}
