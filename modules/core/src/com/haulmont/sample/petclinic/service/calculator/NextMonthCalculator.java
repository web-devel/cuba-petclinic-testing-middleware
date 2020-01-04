package com.haulmont.sample.petclinic.service.calculator;

import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.sample.petclinic.entity.pet.Pet;
import com.haulmont.sample.petclinic.entity.visit.Visit;
import java.time.LocalDate;
import java.util.List;

public class NextMonthCalculator implements RegularCheckupDateCalculator {

  @Override
  public boolean supports(Pet pet) {
    return true;
  }

  @Override
  public LocalDate calculateRegularCheckupDate(
      Pet pet,
      List<Visit> visitsOfPet,
      TimeSource timeSource
  ) {
    return timeSource.now().toLocalDate().plusMonths(1);
  }
}
