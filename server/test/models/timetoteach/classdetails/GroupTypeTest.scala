package models.timetoteach.classdetails

import org.scalatest.FunSpec
import shared.model.classdetail._

import scala.pickling.Defaults._
import scala.pickling.json._

class GroupTypeTest extends FunSpec {
  def createGroupId() : GroupId = {
    GroupId(s"groupId_${java.util.UUID.randomUUID()}")
  }

  describe("When created a group with name 'triangles', type of 'maths' and level of 'first'") {
    val group = Group(createGroupId(), GroupName("triangles"), MathsGroupType, FirstLevel)

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

  describe("When pickling and unpickling a group with name 'triangles', type of 'maths' and level of 'first'") {
    val group = Group(createGroupId(), GroupName("triangles"), MathsGroupType, FirstLevel)
    val groupPickled = group.pickle
    val groupUnpickled = groupPickled.unpickle[Group]

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
      Group(createGroupId(), GroupName("Triangles"), MathsGroupType, SecondLevel),
      Group(createGroupId(), GroupName("Squares"), MathsGroupType, FirstLevel),
      Group(createGroupId(), GroupName("Circles"), MathsGroupType, FirstLevel),
      Group(createGroupId(), GroupName("Bears"), LiteracyGroupType, SecondLevel),
      Group(createGroupId(), GroupName("Lions"), LiteracyGroupType, SecondLevel),
      Group(createGroupId(), GroupName("Tigers"), LiteracyGroupType, FirstLevel)
    )

  }

  describe("Creating a class details with empty teachers, pickled, then unpickled") {
    val groups = List(
      Group(createGroupId(), GroupName("Triangles"), MathsGroupType, SecondLevel),
      Group(createGroupId(), GroupName("Squares"), MathsGroupType, FirstLevel),
      Group(createGroupId(), GroupName("Circles"), MathsGroupType, FirstLevel),
      Group(createGroupId(), GroupName("Bears"), LiteracyGroupType, SecondLevel),
      Group(createGroupId(), GroupName("Lions"), LiteracyGroupType, SecondLevel),
      Group(createGroupId(), GroupName("Tigers"), LiteracyGroupType, FirstLevel)
    )

    val classDetails = ClassDetails(
      ClassId("classId123"),
      ClassName("P3AB"),
      groups,
      List("id12")
    )

    val classDetailsPickled = classDetails.pickle
    val classDetailsUnpickled = classDetailsPickled.unpickle[ClassDetails]

    it("should have a class id of 'classId123'") {
      assert(classDetailsUnpickled.id.id === "classId123")
    }
    it("should have a one class teacher") {
      assert(classDetailsUnpickled.classTeachersWithWriteAccess.size === 1)
    }
    it("should have 6 groups") {
      assert(classDetailsUnpickled.groups.size === 6)
    }
  }

}
