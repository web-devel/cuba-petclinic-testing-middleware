package com.haulmont.sample.petclinic.service;

import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.sample.petclinic.entity.pet.Pet;
import com.haulmont.sample.petclinic.entity.visit.Visit;
import javax.inject.Inject;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service(VisitService.NAME)
public class VisitServiceBean implements VisitService {

  @Inject
  protected DataManager dataManager;

  @Inject
  protected TimeSource timeSource;

  @Override
  public Visit createVisitForToday(String identificationNumber) {

    Optional<Pet> pet = loadPetByIdentificationNumber(identificationNumber);

    return pet.map(this::createVisitForPet).orElse(null);
  }

  private Visit createVisitForPet(Pet pet) {

    Visit visit = dataManager.create(Visit.class);

    visit.setPet(pet);
    visit.setVisitDate(timeSource.currentTimestamp());

    return dataManager.commit(visit);
  }


  /**
   * loads a Pet by its Identification Number
   * @param identificationNumber the Identification Number to load
   * @return the Pet for the given Identification Number if found
   */
  private Optional<Pet> loadPetByIdentificationNumber(String identificationNumber) {
    return dataManager.load(Pet.class)
        .query("select e from petclinic_Pet e where e.identificationNumber = :identificationNumber")
        .parameter("identificationNumber", identificationNumber)
        .optional();
  }
}