package com.haulmont.sample.petclinic.service;

import com.haulmont.sample.petclinic.entity.pet.Pet;
import com.haulmont.sample.petclinic.entity.visit.Visit;
import java.time.LocalDate;
import java.util.List;

public interface VisitService {

  String NAME = "petclinic_VisitService";

  Visit createVisitForToday(String identificationNumber);

  LocalDate calculateNextRegularCheckupDate(
      Pet pet,
      List<Visit> vistsOfPet
  );
}