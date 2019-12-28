package com.haulmont.sample.petclinic.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.haulmont.cuba.core.entity.SendingMessage;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.SendingStatus;
import com.haulmont.cuba.core.global.View;
import com.haulmont.sample.petclinic.PetclinicTestContainer;
import com.haulmont.sample.petclinic.entity.owner.Owner;
import com.haulmont.sample.petclinic.entity.pet.Pet;
import com.haulmont.sample.petclinic.entity.pet.PetType;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PetclinicTestContainer.Common.class)
public class DiseaseWarningMailingServiceBeanTest {

  public static PetclinicTestContainer testContainer = PetclinicTestContainer.Common.INSTANCE;

  private static DataManager dataManager;
  private static DiseaseWarningMailingService diseaseWarningMailingService;
  private static PetclinicEntityLoader entityLoader;
  private static PetclinicEntities entities;

  @BeforeAll
  public static void setUp() throws Exception {
    dataManager = AppBeans.get(DataManager.class);
    diseaseWarningMailingService = AppBeans.get(DiseaseWarningMailingService.class);

    entityLoader = new PetclinicEntityLoader(dataManager);
    entities = new PetclinicEntities(dataManager);
  }


  @Test
  public void warnAboutDisease_createsAnEmail_withTheRightEmailInformation() {

    // given: there is an 'Electric' pet type
    PetType electric = entityLoader.petType("Electric");

    // and: there is exactly one pet with type 'electric' from Alabastia
    List<Pet> allElectricPetsFromAlabastia =
        entityLoader.petsWithTypeFromCity(
            electric,
            "Alabastia",
            "pet-with-owner-and-type"
        );

    assertThat(allElectricPetsFromAlabastia).hasSize(1);

    // and:
    Pet pikachu = allElectricPetsFromAlabastia.get(0);
    Owner ash = pikachu.getOwner();

    // when:
    diseaseWarningMailingService.warnAboutDisease(
        electric,
        "Electrical overcharging",
        "Alabastia"
    );

    SendingMessage email = entityLoader.allEmailSendRequests().get(0);

    // then: the target email address is the email address of ash
    assertThat(email.getAddress())
        .isEqualTo(ash.getEmail());

    // and: the email subject is correctly referring to an Electrical overcharging in Alabastia
    assertThat(email.getCaption())
        .isEqualTo("Warning about Electrical overcharging in the Area of Alabastia");

    // and: the email content should contain the right information in the body
    assertThat(email.getContentText())
        .contains(
            ash.getName(), // the owners name
            pikachu.getName(), // the pets name
            "Electrical overcharging", // the disease
            "Alabastia" // the city
        );

    // and:
    assertThat(email.getStatus())
        .isEqualTo(SendingStatus.QUEUE);

    // cleanup:
    clearEmailSendRequests();
  }


  @Test
  public void warnAboutDisease_createsAnEmail_forEachEffectedPet() {

    // given: there is an 'Electric' pet type
    PetType electric = entityLoader.petType("Electric");

    // and: there is exactly one pet with type 'electric' from Alabastia
    List<Pet> allElectricPetsFromAlabastia =
        entityLoader.petsWithTypeFromCity(electric, "Alabastia", View.LOCAL);

    assertThat(allElectricPetsFromAlabastia).hasSize(1);

    // when:
    int effectedPets = diseaseWarningMailingService.warnAboutDisease(
        electric,
        "Electrical overcharging",
        "Alabastia"
    );

    // then:
    assertThat(effectedPets)
        .isEqualTo(1);

    assertThat(entityLoader.allEmailSendRequests())
        .hasSize(1);

    // cleanup:
    clearEmailSendRequests();
  }


  @Test
  public void warnAboutDisease_createsNoEmail_whenTheOwnerDoesNotHaveAnEmailAddress() {

    // given: there is an 'Electric' pet type
    PetType electric = entityLoader.petType("Electric");

    // and: there is a second owner in Alabastia with an electric pet
    Owner falkner = entities.createOwner(
        "Falkner",
        null,
        "Miastreet 234",
        "Alabastia"
    );

    Pet zapdos = entities.createPet(
        "123",
        "Zapdos",
        electric,
        falkner
    );

    // and: there is two pets with type 'electric' from Alabastia
    List<Pet> electricPetsFromAlabastia =
        entityLoader.petsWithTypeFromCity(electric, "Alabastia", View.LOCAL);

    assertThat(electricPetsFromAlabastia)
        .hasSize(2);

    // when:
    int effectedPets = diseaseWarningMailingService.warnAboutDisease(
        electric,
        "Electrical overcharging",
        "Alabastia"
    );

    // then:
    assertThat(effectedPets)
        .isEqualTo(1);

    assertThat(entityLoader.allEmailSendRequests())
        .hasSize(1);

    testContainer.deleteRecord(zapdos);
    testContainer.deleteRecord(falkner);

  }

  @AfterEach
  public void tearDown() throws Exception {
    clearEmailSendRequests();
  }

  private void clearEmailSendRequests() {
    entityLoader.allEmailSendRequests()
        .forEach(sendingMessage -> testContainer.deleteRecord(sendingMessage));
  }

}