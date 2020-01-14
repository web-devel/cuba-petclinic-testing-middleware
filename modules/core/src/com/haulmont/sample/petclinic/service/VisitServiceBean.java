package com.haulmont.sample.petclinic.service;

import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.sample.petclinic.entity.pet.Pet;
import com.haulmont.sample.petclinic.entity.visit.Visit;
import java.util.Optional;
import javax.inject.Inject;
import org.springframework.stereotype.Service;

@Service(VisitService.NAME)
public class VisitServiceBean implements VisitService {

  @Inject
  protected DataManager dataManager;

  @Inject
  protected TimeSource timeSource;


  @Override
  public Visit createVisitForToday(String identificationNumber) {
    return loadPetByIdentificationNumber(identificationNumber)
            .map(this::createVisitForPet)
            .map(this::saveVisit)
            .orElse(null);
  }


  private Visit createVisitForPet(Pet pet) {
    Visit visit = dataManager.create(Visit.class);
    visit.setPet(pet);
    visit.setVisitDate(timeSource.currentTimestamp());
    return visit;
  }

  private Visit saveVisit(Visit visit) {
    return dataManager.commit(visit);
  }

  private Optional<Pet> loadPetByIdentificationNumber(String identificationNumber) {
    return dataManager.load(Pet.class)
        .query("e.identificationNumber = ?1", identificationNumber)
        .optional();
  }
}