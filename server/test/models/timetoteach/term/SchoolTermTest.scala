package models.timetoteach.term

import java.time.LocalDate

import org.scalatest.FunSpec

class SchoolTermTest extends FunSpec {

  describe("Given a SchoolTerm with start date Monday 19th Feb 2018 & end date Thursday 29th March 2018, listOfAllMondaysInTerm()") {
    val schoolTerm = SchoolTerm(
      SchoolYear(2017, Some(2018)),
      SchoolTermName.SPRING_SECOND_TERM,
      LocalDate.of(2018, 2, 19),
      LocalDate.of(2018, 3, 29)
    )
    it("should return a list of 6 dates") {
      println(schoolTerm.listOfAllMondaysInTerm().toString())
      assert(schoolTerm.listOfAllMondaysInTerm().size === 6)
    }
    it("should return a list of dates, first one is Monday 19th February") {
      assert(schoolTerm.listOfAllMondaysInTerm().head === LocalDate.of(2018,2,19))
    }
    it("should return a list of dates, sixth one is Monday 26th March") {
      assert(schoolTerm.listOfAllMondaysInTerm().last === LocalDate.of(2018,3,26))
    }
  }

  describe("Given a SchoolTerm with start date Thursday 15th Feb 2018 & end date Tuesday 3rd Arpil 2018, listOfAllMondaysInTerm()") {
    val schoolTerm = SchoolTerm(
      SchoolYear(2017, Some(2018)),
      SchoolTermName.SPRING_SECOND_TERM,
      LocalDate.of(2018, 2, 15),
      LocalDate.of(2018, 4, 3)
    )
    it("should return a list of 8 dates") {
      println(schoolTerm.listOfAllMondaysInTerm().toString())
      assert(schoolTerm.listOfAllMondaysInTerm().size === 8)
    }
    it("should return a list of dates, first one is Monday 12th February") {
      assert(schoolTerm.listOfAllMondaysInTerm().head === LocalDate.of(2018,2,12))
    }
    it("should return a list of dates, last one is Monday 2nd April") {
      assert(schoolTerm.listOfAllMondaysInTerm().last === LocalDate.of(2018,4,2))
    }
  }


  describe("Given Thursday 22nd March, 2018, findNearestPreviousMonday()") {
    it("should return Monday 19th 2018") {
      val nearestPreviousMonday = SchoolTerm.findNearestPreviousMonday(LocalDate.of(2018,3,22))
      assert(nearestPreviousMonday === LocalDate.of(2018,3,19))
    }
  }

  describe("Given Monday 19th March, 2018, findNearestPreviousMonday()") {
    it("should return Monday 19th 2018") {
      val nearestPreviousMonday = SchoolTerm.findNearestPreviousMonday(LocalDate.of(2018,3,19))
      assert(nearestPreviousMonday === LocalDate.of(2018,3,19))
    }
  }

  describe("Given Sunday 18th March, 2018, findNearestPreviousMonday()") {
    it("should return Monday 12th 2018") {
      val nearestPreviousMonday = SchoolTerm.findNearestPreviousMonday(LocalDate.of(2018,3,18))
      assert(nearestPreviousMonday === LocalDate.of(2018,3,12))
    }
  }

  describe("Given a SchoolTerm with start date Monday 19th Feb 2018 & end date Thursday 29th March 2018, weekNumberForGivenDate(...)") {
    val schoolTerm = SchoolTerm(
      SchoolYear(2017, Some(2018)),
      SchoolTermName.SPRING_SECOND_TERM,
      LocalDate.of(2018, 2, 19),
      LocalDate.of(2018, 3, 29)
    )
    it("should return 0 for an input date of Sunday 18th Feb 2018") {
      assert(schoolTerm.weekNumberForGivenDate(LocalDate.of(2018, 2, 18)) == 0)
    }
    it("should return 1 for an input date of Monday 19th Feb 2018") {
      assert(schoolTerm.weekNumberForGivenDate(LocalDate.of(2018, 2, 19)) == 1)
    }
    it("should return 1 for an input date of Sunday 25th Feb 2018") {
      assert(schoolTerm.weekNumberForGivenDate(LocalDate.of(2018, 2, 25)) == 1)
    }
    it("should return 2 for an input date of  Monday 26th Feb 2018") {
      assert(schoolTerm.weekNumberForGivenDate(LocalDate.of(2018, 2, 26)) == 2)
    }

  }

}
