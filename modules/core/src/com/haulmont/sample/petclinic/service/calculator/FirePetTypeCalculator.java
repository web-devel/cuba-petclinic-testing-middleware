package com.haulmont.sample.petclinic.service.calculator;

import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.sample.petclinic.entity.pet.Pet;
import com.haulmont.sample.petclinic.entity.visit.Visit;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component("petclinic_FirePetTypeCalculator")
@Order(1)
public class FirePetTypeCalculator implements RegularCheckupDateCalculator {

  @Override
  public boolean supports(Pet pet) {
    return Objects.equals(pet.getType().getName(), "Fire");
  }

  @Override
  public LocalDate calculateRegularCheckupDate(
      Pet pet,
      List<Visit> visitsOfPet,
      TimeSource timeSource
  ) {


    Optional<Date> latestRegularCheckup = visitsOfPet.stream()
        .filter(visit -> visit.getDescription().contains("Regular Checkup"))
        .map(Visit::getVisitDate)
        .max(Date::compareTo);

    return latestRegularCheckup
        .map(date -> toLocalDate(date).plusMonths(6))
        .orElse(timeSource.now().toLocalDate().plusMonths(1));

  }


  public LocalDate toLocalDate(Date dateToConvert) {
    return dateToConvert.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate();
  }
}
