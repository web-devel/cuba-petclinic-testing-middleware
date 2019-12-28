package com.haulmont.sample.petclinic.service;

import com.haulmont.cuba.core.entity.SendingMessage;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.View;
import com.haulmont.sample.petclinic.entity.owner.Owner;
import com.haulmont.sample.petclinic.entity.pet.Pet;
import com.haulmont.sample.petclinic.entity.pet.PetType;
import java.util.List;


/**
 * PetclinicDb represents an API abstraction on the use-cases for the Integration tests of the
 * Petclinic application
 */
public class PetclinicDb {

  private final DataManager dataManager;

  public PetclinicDb(DataManager dataManager) {
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


  public List<Pet> petsWithTypeFromCity(PetType type, String city) {
    return petsWithTypeFromCity(type, city, View.LOCAL);
  }

  public List<Pet> petsWithTypeFromCity(PetType type, String city, String view) {
    return dataManager.load(Pet.class)
        .query("e.type = ?1 AND e.owner.city = ?2", type, city)
        .view(view)
        .list();
  }

  public PetType petTypeWithName(String name) {
    return dataManager.load(PetType.class)
        .query("e.name = ?1", name)
        .one();
  }

  public List<SendingMessage> outgoingEmails() {
    return dataManager.load(SendingMessage.class).list();
  }

  public SendingMessage latestOutgoingEmail() {
    return outgoingEmails().get(0);
  }

  public List<Owner> ownersFromCity(String city) {
    return dataManager.load(Owner.class)
        .query("e.city = ?1", city)
        .list();
  }
}