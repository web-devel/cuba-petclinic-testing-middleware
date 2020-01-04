package com.haulmont.sample.petclinic.service.visit.create_visit;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.View;
import com.haulmont.sample.petclinic.PetclinicTestContainer;
import com.haulmont.sample.petclinic.entity.owner.Owner;
import com.haulmont.sample.petclinic.entity.pet.Pet;
import com.haulmont.sample.petclinic.entity.pet.PetType;
import java.util.Optional;
import java.util.UUID;


/**
 * PetclinicVisitDb represents an DB API abstraction for the Visit Service use-case
 */
public class PetclinicVisitDb {

  private final DataManager dataManager;
  private final PetclinicTestContainer testContainer;

  public PetclinicVisitDb(
      DataManager dataManager,
      PetclinicTestContainer testContainer
  ) {
    this.dataManager = dataManager;
    this.testContainer = testContainer;
  }

  public Pet petWithName(String name, String view) {
    return dataManager.load(Pet.class)
        .query("e.name = ?1", name)
        .view(view)
        .one();
  }

  public void remove(Entity<UUID> entity) {
    testContainer.deleteRecord(entity);
  }

  public Optional<Pet> petWithIdentificationNumber(String identificationNumber) {
    return petWithIdentificationNumber(identificationNumber, View.LOCAL);
  }

  public Optional<Pet> petWithIdentificationNumber(String identificationNumber, String view) {
    return dataManager.load(Pet.class)
        .query("e.identificationNumber = ?1", identificationNumber)
        .view(view)
        .optional();
  }

  public Long countVisitsFor(Pet pet) {
    return dataManager.loadValue(
        "select count(e) from petclinic_Visit e where e.pet = :pet", Long.class)
        .parameter("pet", pet)
        .one();
  }

  public Long countVisits() {
    return dataManager.loadValue(
        "select count(e) from petclinic_Visit e", Long.class)
        .one();
  }
}