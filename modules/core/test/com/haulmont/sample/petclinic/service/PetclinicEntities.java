package com.haulmont.sample.petclinic.service;

import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.sample.petclinic.entity.owner.Owner;
import com.haulmont.sample.petclinic.entity.pet.Pet;
import com.haulmont.sample.petclinic.entity.pet.PetType;

public class PetclinicEntities {

  private final DataManager dataManager;

  public PetclinicEntities(DataManager dataManager) {
    this.dataManager = dataManager;
  }


  public Pet createPet(
      String identificationNumber,
      String name,
      PetType type,
      Owner owner
  ) {
    Pet pet = dataManager.create(Pet.class);

    pet.setType(type);
    pet.setName(name);
    pet.setOwner(owner);
    pet.setIdentificationNumber(identificationNumber);

    return dataManager.commit(pet);
  }

  public Owner createOwner(
      String name,
      String email,
      String address,
      String city
  ) {
    Owner owner = dataManager.create(Owner.class);

    owner.setFirstName(name);
    owner.setEmail(email);
    owner.setAddress(address);
    owner.setCity(city);

    return dataManager.commit(owner);
  }

}