package com.pm.patientservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientResponseDTO {
    // contain all the properties that we want to return to frontend client
    private String id;
    private String name;
    private String email;
    private String address;
    private String dateOfBirth;
}
