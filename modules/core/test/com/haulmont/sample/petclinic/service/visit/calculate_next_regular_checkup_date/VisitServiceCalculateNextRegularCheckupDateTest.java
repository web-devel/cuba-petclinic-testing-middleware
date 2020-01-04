package com.haulmont.sample.petclinic.service.visit.calculate_next_regular_checkup_date;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;

import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.sample.petclinic.entity.pet.Pet;
import com.haulmont.sample.petclinic.entity.visit.Visit;
import com.haulmont.sample.petclinic.service.VisitService;
import com.haulmont.sample.petclinic.service.VisitServiceBean;
import com.haulmont.sample.petclinic.service.calculator.ElectricPetTypeCalculator;
import com.haulmont.sample.petclinic.service.calculator.FirePetTypeCalculator;
import com.haulmont.sample.petclinic.service.calculator.OtherPetTypesCalculator;
import com.haulmont.sample.petclinic.service.calculator.RegularCheckupDateCalculator;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VisitServiceCalculateNextRegularCheckupDateTest {

  private final LocalDate LAST_YEAR = now().minusYears(1);
  private final LocalDate LAST_MONTH = now().minusMonths(1);
  private final LocalDate SIX_MONTHS_AGO = now().minusMonths(6);
  private final LocalDate THREE_MONTHS_AGO = now().minusMonths(3);
  private final LocalDate TWO_MONTHS_AGO = now().minusMonths(2);
  private final LocalDate NEXT_MONTH = now().plusMonths(1);


  @Mock
  private TimeSource timeSource;


  private VisitService visitService;
  private List<Visit> visits = new ArrayList<>();
  private PetclinicData data = new PetclinicData();

  @BeforeEach
  void setupEnvironment() {

    // given: desired behavior for the TimeSource dependency is configured
    Mockito.lenient()
        .when(timeSource.now())
        .thenReturn(ZonedDateTime.now());

    /*
     the order represents the order mentioned in the
     @Order annotation of the Spring beans
     */
    List<RegularCheckupDateCalculator> regularCheckupDateCalculators = Stream.of(
        new ElectricPetTypeCalculator(),
        new FirePetTypeCalculator(),
        new OtherPetTypesCalculator()
    ).collect(Collectors.toList());


    // and: visitService (System under Test) is manually created (no Integration test)
    visitService = new VisitServiceBean(
        timeSource,
        regularCheckupDateCalculators
    );

  }


  @Nested
  class ElectricPet {

    private Pet electricPet;

    @BeforeEach
    void setupPet() {
      electricPet = data.petWithType(data.electricType());
    }

    @Test
    public void intervalIsOneYear_fromTheLatestRegularCheckup() {

      // given:

      // and: there are two regular checkups in the visit history of this pet
      visits.add(data.regularCheckup(LAST_YEAR));
      visits.add(data.regularCheckup(LAST_MONTH));

      // when:
      LocalDate nextRegularCheckup =
          visitService.calculateNextRegularCheckupDate(electricPet, visits);

      // then:
      assertThat(nextRegularCheckup)
          .isEqualTo(LAST_MONTH.plusYears(1));
    }


    @Test
    public void onlyRegularCheckupVisits_areTakenIntoConsideration_whenCalculatingNextRegularCheckup() {

      // given: there are two regular checkups in the visit history of this pet
      visits.add(
          data.regularCheckup(SIX_MONTHS_AGO)
      );

      // and: a non-regular checkup happened last month
      visits.add(
          data.surgery(LAST_MONTH)
      );

      // when:
      LocalDate nextRegularCheckup =
          visitService.calculateNextRegularCheckupDate(electricPet, visits);

      // then: the date of the last checkup is used
      assertThat(nextRegularCheckup)
          .isEqualTo(SIX_MONTHS_AGO.plusYears(1));
    }


    @Test
    public void ifThePetDidNotHavePreviousCheckups_nextMonthIsProposed() {

      // given: there are two regular checkups in the visit history of this pet
      visits.add(data.surgery(LAST_MONTH));

      // when:
      LocalDate nextRegularCheckup =
          visitService.calculateNextRegularCheckupDate(electricPet, visits);

      // then:
      assertThat(nextRegularCheckup)
          .isEqualTo(NEXT_MONTH);
    }


    @Test
    public void ifThePetDidHasACheckupLongerThanTheInterval_nextMonthIsProposed() {

      // given: there are two regular checkups in the visit history of this pet
      visits.add(data.regularCheckup(LAST_YEAR.minusMonths(1)));

      // when:
      LocalDate nextRegularCheckup =
          visitService.calculateNextRegularCheckupDate(electricPet, visits);

      // then:
      assertThat(nextRegularCheckup)
          .isEqualTo(NEXT_MONTH);
    }

  }


  @Test
  public void forAFirePet_intervalIsSixMonths_fromTheLatestRegularCheckup() {

    // given:
    Pet firePet = data.petWithType(data.fireType());

    // and:
    visits.add(data.regularCheckup(LAST_YEAR));
    visits.add(data.regularCheckup(TWO_MONTHS_AGO));
    visits.add(data.regularCheckup(SIX_MONTHS_AGO));

    // when:
    LocalDate nextRegularCheckup =
        visitService.calculateNextRegularCheckupDate(firePet, visits);

    // then:
    assertThat(nextRegularCheckup)
        .isEqualTo(TWO_MONTHS_AGO.plusMonths(6));
  }

  @Test
  public void forAnyOtherPetType_intervalIsNineMonths_fromTheLatestRegularCheckup() {

    // given:
    Pet waterPet = data.petWithType(data.waterType());

    // and: there are two regular checkups in the visit history of this pet
    visits.add(data.regularCheckup(LAST_YEAR));
    visits.add(data.regularCheckup(THREE_MONTHS_AGO));

    // when:
    LocalDate nextRegularCheckup =
        visitService.calculateNextRegularCheckupDate(waterPet, visits);

    // then:
    assertThat(nextRegularCheckup)
        .isEqualTo(THREE_MONTHS_AGO.plusMonths(9));
  }

}