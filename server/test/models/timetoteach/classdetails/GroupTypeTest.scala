package models.timetoteach.classdetails

import org.scalatest.FunSpec
import duplicate.model._

class GroupTypeTest extends FunSpec {
  def createGroupId() : GroupId = {
    GroupId(s"groupId_${java.util.UUID.randomUUID()}")
  }

  describe("When created a group with name 'triangles', type of 'maths' and level of 'first'") {
    val group = Group(createGroupId(), GroupName("triangles"), GroupDescription("The Triangles Group"), MathsGroupType, FirstLevel)

    it("should have a name of 'triangles'") {
      assert(group.groupName.name === "triangles")
    }
    it("should have a type of 'maths'") {
      assert(group.groupType === MathsGroupType)
    }
    it("should have a level of 'first'") {
      assert(group.groupLevel === FirstLevel)
    }
  }

  describe("Creating a class details with empty teachers") {
    val groups = List(
      Group(createGroupId(), GroupName("Triangles"), GroupDescription(""), MathsGroupType, SecondLevel),
      Group(createGroupId(), GroupName("Squares"), GroupDescription(""), MathsGroupType, FirstLevel),
      Group(createGroupId(), GroupName("Circles"),GroupDescription(""),  MathsGroupType, FirstLevel),
      Group(createGroupId(), GroupName("Bears"), GroupDescription(""), LiteracyGroupType, SecondLevel),
      Group(createGroupId(), GroupName("Lions"), GroupDescription(""), LiteracyGroupType, SecondLevel),
      Group(createGroupId(), GroupName("Tigers"),GroupDescription(""),  LiteracyGroupType, FirstLevel)
    )

  }

  describe("Creating a class details with empty teachers, pickled, then unpickled") {
    val groups = List(
      Group(createGroupId(), GroupName("Triangles"), GroupDescription(""), MathsGroupType, SecondLevel),
      Group(createGroupId(), GroupName("Squares"), GroupDescription(""), MathsGroupType, FirstLevel),
      Group(createGroupId(), GroupName("Circles"),GroupDescription(""),  MathsGroupType, FirstLevel),
      Group(createGroupId(), GroupName("Bears"), GroupDescription(""), LiteracyGroupType, SecondLevel),
      Group(createGroupId(), GroupName("Lions"), GroupDescription(""), LiteracyGroupType, SecondLevel),
      Group(createGroupId(), GroupName("Tigers"),GroupDescription(""),  LiteracyGroupType, FirstLevel)
    )

    val classDetails = ClassDetails(
      ClassId("classId123"),
      SchoolDetails(
        "school123",
        "schoolName",
        "schoolAddr",
        "schoolPostCode",
        "schoolTel",
        "schoolLa",
        "schoolCountry"
      ),
      ClassName("P3AB"),
      ClassDescription("My New Class!"),
      groups,
      List("id12"),
      Map()
    )
  }

}
