package com.gary.backendv2.service;

import com.gary.backendv2.exception.NotFoundException;
import com.gary.backendv2.model.Allergy;
import com.gary.backendv2.model.MedicalInfo;
import com.gary.backendv2.model.dto.request.AllergyRequest;
import com.gary.backendv2.repository.AllergyRepository;
import com.gary.backendv2.repository.MedicalInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AllergyService {
	private final AllergyRepository allergyRepository;
	private final MedicalInfoRepository medicalInfoRepository;

	public List<Allergy> getAll(){
		return allergyRepository.findAll();
	}

	public Allergy getById(Integer id){
		return allergyRepository.findByAllergyId(id).orElseThrow(()-> new NotFoundException("No record with that ID"));
	}

	public void addAllergy(AllergyRequest allergyRequest){
		MedicalInfo medicalInfo = medicalInfoRepository.findByMedicalInfoId(allergyRequest.getMedicalInfoId());
		if(!(allergyRepository.existsByAllergyName(allergyRequest.getAllergyName())||allergyRepository.existsByAllergyType(allergyRequest.getAllergyType()) || allergyRepository.existsByOther(allergyRequest.getOther()))){
			Set<MedicalInfo> medicalInfos = new HashSet<>();
			medicalInfos.add(medicalInfo);
			Allergy allergy =Allergy.builder()
					.allergyType(allergyRequest.getAllergyType())
					.allergyName(allergyRequest.getAllergyName())
					.other(allergyRequest.getOther())
					.medicalInfos(medicalInfos).build();
			allergyRepository.save(allergy);
			medicalInfo.getAllergies().add(allergy);
		}else{
			Allergy a = allergyRepository
					.findAllByAllergyName(allergyRequest.getAllergyName()).stream()
					.filter(allergy -> allergy.getAllergyType() == allergyRequest.getAllergyType() && allergyRequest.getOther().equals(allergy.getOther()))
					.findFirst().orElseThrow();
			medicalInfo.getAllergies().add(a);
			a.getMedicalInfos().add(medicalInfo);
			allergyRepository.save(a);
		}
		medicalInfoRepository.save(medicalInfo);
	}
	public void updateAllergy(Integer id, AllergyRequest allergyRequest){
		Allergy allergy = allergyRepository.findByAllergyId(id).orElseThrow();
		allergy.setAllergyName(allergyRequest.getAllergyName());
		allergy.setAllergyType(allergyRequest.getAllergyType());
		allergy.setOther(allergyRequest.getOther());
		allergyRepository.save(allergy);
	}
	public void removeAllergy(Integer id){
		allergyRepository.delete(allergyRepository.findByAllergyId(id).orElseThrow(()-> new NotFoundException("No record with that ID")));
	}
}