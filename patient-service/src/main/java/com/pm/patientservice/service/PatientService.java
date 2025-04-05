package com.pm.patientservice.service;

import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.exception.EmailAlreadyExistsException;
import com.pm.patientservice.mapper.PatientMapper;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {
    private PatientRepository patientRepository;

    @Autowired
    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<PatientResponseDTO > getPatients(){
         List<Patient> patients = patientRepository.findAll();

         List<PatientResponseDTO> patientResponseDTOs = patients.stream()
                 .map(PatientMapper::toDTO)
                 .toList();

         return patientResponseDTOs;
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO){
        if(patientRepository.existsByEmail(patientRequestDTO.getEmail())){
            throw new EmailAlreadyExistsException("A patient with the email "
                    + patientRequestDTO.getEmail() + " already exists");
        }

        Patient newPatient = PatientMapper.toModel(patientRequestDTO);
        newPatient = patientRepository.save(newPatient);

        PatientResponseDTO patientResponseDTO = PatientMapper.toDTO(newPatient);

        return patientResponseDTO;
    }
}
