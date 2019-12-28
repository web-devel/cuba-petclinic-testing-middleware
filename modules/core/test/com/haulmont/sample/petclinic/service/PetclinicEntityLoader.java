package com.haulmont.sample.petclinic.service;

import com.haulmont.cuba.core.entity.SendingMessage;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.View;
import com.haulmont.sample.petclinic.entity.pet.Pet;
import com.haulmont.sample.petclinic.entity.pet.PetType;
import java.util.List;

public class PetclinicEntityLoader {

  private final DataManager dataManager;

  public PetclinicEntityLoader(DataManager dataManager) {
    this.dataManager = dataManager;
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

  public PetType petType(String name) {
    return dataManager.load(PetType.class)
        .query("e.name = ?1", name)
        .one();
  }

  public List<SendingMessage> allEmailSendRequests() {
    return dataManager.load(SendingMessage.class).list();
  }
}