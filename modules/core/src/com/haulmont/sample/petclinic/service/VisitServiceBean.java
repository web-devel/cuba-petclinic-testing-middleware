package com.haulmont.sample.petclinic.service;

import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.sample.petclinic.entity.pet.Pet;
import com.haulmont.sample.petclinic.entity.visit.Visit;
import com.haulmont.sample.petclinic.service.calculator.NextMonthCalculator;
import com.haulmont.sample.petclinic.service.calculator.RegularCheckupDateCalculator;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import org.springframework.stereotype.Service;

@Service(VisitService.NAME)
public class VisitServiceBean implements VisitService {

  @Inject
  protected DataManager dataManager;

  @Inject
  protected TimeSource timeSource;

  @Inject
  protected List<RegularCheckupDateCalculator> regularCheckupDateCalculators;

  public VisitServiceBean(TimeSource timeSource, List<RegularCheckupDateCalculator> regularCheckupDateCalculators) {
    this.timeSource = timeSource;
    this.regularCheckupDateCalculators = regularCheckupDateCalculators;
  }

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

  @Override
  public LocalDate calculateNextRegularCheckupDate(
      Pet pet,
      List<Visit> vistsOfPet
  ) {
    RegularCheckupDateCalculator calculator = regularCheckupDateCalculators.stream()
        .filter(regularCheckupDateCalculator -> regularCheckupDateCalculator.supports(pet))
        .findFirst()
        .orElse(new NextMonthCalculator());

    return calculator.calculateRegularCheckupDate(pet, vistsOfPet, timeSource);

  }
}