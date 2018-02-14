package controllers.planning.termly

import duplicate.model._
import org.scalatest.FunSpec

class TermlyPlansControllerTest extends FunSpec {

  private def createListOfClassDetails(): List[ClassDetails] = {
    ClassDetails(
      ClassId("class1"),
      SchoolDetails(
        id = "school1",
        name = "The One School",
        address = "",
        postCode = "",
        telephone = "",
        localAuthority = "",
        country = ""
      ),
      ClassName("P1AB"),
      ClassDescription(""),
      Nil,
      List("andy"),
      Map()
    ) :: ClassDetails(
      ClassId("class2"),
      SchoolDetails(
        id = "school1",
        name = "The One School",
        address = "",
        postCode = "",
        telephone = "",
        localAuthority = "",
        country = ""
      ),
      ClassName("P2QQ"),
      ClassDescription(""),
      Nil,
      List("andy"),
      Map()
    ) :: ClassDetails(
      ClassId("class3"),
      SchoolDetails(
        id = "school1",
        name = "The One School",
        address = "",
        postCode = "",
        telephone = "",
        localAuthority = "",
        country = ""
      ),
      ClassName("P3WW"),
      ClassDescription(""),
      Nil,
      List("andy"),
      Map()
    ) :: ClassDetails(
      ClassId("classAnother1"),
      SchoolDetails(
        id = "school2",
        name = "The Two School",
        address = "",
        postCode = "",
        telephone = "",
        localAuthority = "",
        country = ""
      ),
      ClassName("AN67"),
      ClassDescription(""),
      Nil,
      List("andy"),
      Map()
    ) :: ClassDetails(
      ClassId("classYep"),
      SchoolDetails(
        id = "school2",
        name = "The Two School",
        address = "",
        postCode = "",
        telephone = "",
        localAuthority = "",
        country = ""
      ),
      ClassName("P6YY"),
      ClassDescription(""),
      Nil,
      List("andy"),
      Map()
    ) :: Nil
  }

  describe("Given a list of class details, buildSchoolToClassesMap()") {
    it("should return a map of the same total entries") {
      val schoolsToClassesMap = TermlyPlansController.buildSchoolNameToClassesMap(createListOfClassDetails())
      assert(schoolsToClassesMap.values.flatten.size === 5)
    }
    it("should return a map with 2 keys") {
      val schoolsToClassesMap = TermlyPlansController.buildSchoolNameToClassesMap(createListOfClassDetails())
      assert(schoolsToClassesMap.keySet.size === 2)
    }
    it("should return a map which has a key value of 'The One School' having a list of 3 schools") {
      val schoolsToClassesMap = TermlyPlansController.buildSchoolNameToClassesMap(createListOfClassDetails())
      assert(schoolsToClassesMap.get("The One School").isDefined)
      assert(schoolsToClassesMap("The One School").size === 3)
    }
    it("should return a map which has a key value of 'The Two School' having a list of 2 schools") {
      val schoolsToClassesMap = TermlyPlansController.buildSchoolNameToClassesMap(createListOfClassDetails())
      assert(schoolsToClassesMap.get("The Two School").isDefined)
      assert(schoolsToClassesMap("The Two School").size === 2)
    }

  }

}

