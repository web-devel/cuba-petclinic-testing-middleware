package com.haulmont.sample.petclinic.service.calculator;

import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.sample.petclinic.entity.pet.Pet;
import com.haulmont.sample.petclinic.entity.visit.Visit;
import java.time.LocalDate;
import java.util.List;

public interface RegularCheckupDateCalculator {

  boolean supports(Pet pet);

  LocalDate calculateRegularCheckupDate(
      Pet pet,
      List<Visit> visitsOfPet,
      TimeSource timeSource
  );
}
