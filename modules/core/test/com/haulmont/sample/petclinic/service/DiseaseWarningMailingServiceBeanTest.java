package com.haulmont.sample.petclinic.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.SendingMessage;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.SendingStatus;
import com.haulmont.sample.petclinic.PetclinicTestContainer;
import com.haulmont.sample.petclinic.entity.owner.Owner;
import com.haulmont.sample.petclinic.entity.pet.Pet;
import com.haulmont.sample.petclinic.entity.pet.PetType;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PetclinicTestContainer.Common.class)
public class DiseaseWarningMailingServiceBeanTest {

  private final String ALABASTIA = "Alabastia";
  private final String ELECTRICAL_OVERCHARGING = "Electrical overcharging";

  public static PetclinicTestContainer testContainer = PetclinicTestContainer.Common.INSTANCE;

  private static DiseaseWarningMailingService diseaseWarningMailingService;

  private static PetclinicDb db;


  private PetType electricType;
  private List<Pet> electricPetsFromAlabastia;

  @BeforeAll
  public static void setupEnvironment() {
    diseaseWarningMailingService = AppBeans.get(DiseaseWarningMailingService.class);

    DataManager dataManager = AppBeans.get(DataManager.class);
    db = new PetclinicDb(dataManager);
  }

  @BeforeEach
  public void loadTestDataAndVerifyItsCorrectness() {

    // given: there is an 'Electric' pet type
    electricType = db.petTypeWithName("Electric");

    assertThat(electricType).isNotNull();

    // and: there is exactly one electric pet from Alabastia
    electricPetsFromAlabastia = db.petsWithTypeFromCity(
        electricType,
        ALABASTIA,
        "pet-with-owner-and-type"
    );

    assertThat(electricPetsFromAlabastia).hasSize(1);
  }


  private int warnAboutElectricalOverchargingIn(String city) {
    return diseaseWarningMailingService.warnAboutDisease(
        electricType,
        ELECTRICAL_OVERCHARGING,
        city
    );
  }

  @Test
  public void warnAboutDisease_createsAnEmail_withTheRightEmailInformation() {

    // given:
    Pet pikachu = electricPetsFromAlabastia.get(0);
    Owner ash = pikachu.getOwner();

    // when: a warning is send out for Alabastia
    warnAboutElectricalOverchargingIn(ALABASTIA);

    SendingMessage outgoingEmail = db.latestOutgoingEmail();

    // then: the target email address is the email address of ash
    assertThat(outgoingEmail.getAddress())
        .isEqualTo(ash.getEmail());

    // and: the email subject is correctly referring to an Electrical overcharging in Alabastia
    assertThat(outgoingEmail.getCaption())
        .isEqualTo(String.format("Warning about %s in the Area of %s", ELECTRICAL_OVERCHARGING, ALABASTIA));

    // and: the email content should contain the right information in the body
    assertThat(outgoingEmail.getContentText())
        .contains(
            ash.getName(), // the owners name
            pikachu.getName(), // the pets name
            ELECTRICAL_OVERCHARGING, // the disease
            ALABASTIA // the city
        );

    // and: the outgoing email is in status QUEUE - to be processed by the asynchronous Email API of CUBA
    assertThat(outgoingEmail.getStatus())
        .isEqualTo(SendingStatus.QUEUE);
  }


  @Test
  public void warnAboutDisease_createsAnEmail_forEachEffectedPet() {

    // given: there is only one electric pet from Alabastia
    assertThat(electricPetsFromAlabastia).hasSize(1);

    // when: a warning is send out for Alabastia
    int effectedPets = warnAboutElectricalOverchargingIn(ALABASTIA);

    // then:
    assertThat(effectedPets)
        .isEqualTo(1);

    assertThat(db.outgoingEmails())
        .hasSize(1);

  }

  @Test
  public void warnAboutDisease_createsNoEmails_forACityWithoutOwners() {

    // given: there is no owner in the Cerulean City
    assertThat(db.ownersFromCity("Cerulean City")).isEmpty();

    // when: a warning is send out for Cerulean City
    int effectedPets = warnAboutElectricalOverchargingIn("Cerulean City");

    // then:
    assertThat(effectedPets)
        .isEqualTo(0);

    assertThat(db.outgoingEmails())
        .hasSize(0);

  }


  @Nested
  class WarnAboutDiseaseWithAdditionalElectricPetAndOwner {

    private Owner falkner;
    private Pet zapdos;

    @BeforeEach
    void createAdditionalElectricPetAndOwner() {
      // given: there is an 'Electric' pet type

      // and: there is a second owner in Alabastia with an electric pet
      falkner = db.createOwner(
          "Falkner",
          null,
          "Miastreet 234",
          ALABASTIA
      );

      zapdos = db.createPet(
          "123",
          "Zapdos",
          electricType,
          falkner
      );

    }

    @Test
    public void warnAboutDisease_createsNoEmail_whenTheOwnerDoesNotHaveAnEmailAddress() {

      // and: there is two pets with type 'electric' from Alabastia
      List<Pet> electricPetsFromAlabastia =
          db.petsWithTypeFromCity(electricType, ALABASTIA);

      assertThat(electricPetsFromAlabastia)
          .hasSize(2);

      // when: a warning is send out for Alabastia
      int effectedPets = warnAboutElectricalOverchargingIn(ALABASTIA);

      // then: only one email was send out for the pet with an owner that has an email
      assertThat(effectedPets)
          .isEqualTo(1);

      assertThat(db.outgoingEmails())
          .hasSize(1);

    }

    @AfterEach
    void removeAdditionalElectricPetAndOwner() {
      removeEntity(zapdos);
      removeEntity(falkner);
    }

  }

  @AfterEach
  public void clearOutgoingEmails() {
    db.outgoingEmails()
        .forEach(this::removeEntity);
  }

  private void removeEntity(Entity entity) {
    testContainer.deleteRecord(entity);
  }

}