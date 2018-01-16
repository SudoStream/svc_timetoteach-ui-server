package models.timetoteach.classdetails

import org.scalatest.FunSpec

import scala.pickling.Defaults._
import scala.pickling.json._

class GroupTypeTest extends FunSpec {
  describe("When created a class teacher with name 'Andy' 'Boyle' and no id") {
    val teacher = Teacher(None, "Andy", "Boyle")

    it("should have no id") {
      assert(teacher.maybeId.isEmpty)
    }
    it("should have a first name of 'Andy'") {
      assert(teacher.firstName === "Andy")
    }
    it("should have a surname of 'Boyle'") {
      assert(teacher.surname === "Boyle")
    }
  }

  describe("When created a group with name 'triangles', type of 'maths' and level of 'first'") {
    val group = Group(GroupName("triangles"), MathsGroupType, FirstLevel)

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
    val group = Group(GroupName("triangles"), MathsGroupType, FirstLevel)
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

  describe("Pickling and then unpickling a teacher with no id") {
    val teacher = Teacher(None, "Andy", "Boyle")
    val teacherPickled = teacher.pickle
    val teacherUnpickled = teacherPickled.unpickle[Teacher]

    it("should have a first name of 'Andy'") {
      assert(teacherUnpickled.firstName === "Andy")
    }
    it("should have a surname of 'Boyle'") {
      assert(teacherUnpickled.surname === "Boyle")
    }
    it("should have an id on 'None'") {
      assert(teacherUnpickled.maybeId.isEmpty)
    }
  }

  describe("Pickling and then unpickling a teacher with an id") {
    val teacher = Teacher(Some(TeacherId("id123")), "Yvonne", "Boyle")
    val teacherPickled = teacher.pickle

    it("should have a first name of 'Yvonne'") {
      val teacherUnpickled = teacherPickled.unpickle[Teacher]
      assert(teacherUnpickled.firstName === "Yvonne")
    }
    it("should have a surname of 'Boyle'") {
      val teacherUnpickled = teacherPickled.unpickle[Teacher]
      assert(teacherUnpickled.surname === "Boyle")
    }
    it("should have an id defined") {
      val teacherUnpickled = teacherPickled.unpickle[Teacher]
      assert(teacherUnpickled.maybeId.isDefined)
    }
    it("should have an id on 'id123'") {
      val teacherUnpickled = teacherPickled.unpickle[Teacher]
      assert(teacherUnpickled.maybeId.get.id === "id123")
    }
  }

  describe("Creating a class details with empty teachers") {
    val groups = List(
      Group(GroupName("Triangles"), MathsGroupType, SecondLevel),
      Group(GroupName("Squares"), MathsGroupType, FirstLevel),
      Group(GroupName("Circles"), MathsGroupType, FirstLevel),
      Group(GroupName("Bears"), LiteracyGroupType, SecondLevel),
      Group(GroupName("Lions"), LiteracyGroupType, SecondLevel),
      Group(GroupName("Tigers"), LiteracyGroupType, FirstLevel)
    )

    it("should throw exception") {
      assertThrows[IllegalArgumentException] {
        val classDetails = ClassDetails(
          ClassId("classId123"),
          ClassName("P3AB"),
          groups,
          List()
        )
      }
    }
  }

  describe("Creating a class details with empty teachers, pickled, then unpickled") {
    val groups = List(
      Group(GroupName("Triangles"), MathsGroupType, SecondLevel),
      Group(GroupName("Squares"), MathsGroupType, FirstLevel),
      Group(GroupName("Circles"), MathsGroupType, FirstLevel),
      Group(GroupName("Bears"), LiteracyGroupType, SecondLevel),
      Group(GroupName("Lions"), LiteracyGroupType, SecondLevel),
      Group(GroupName("Tigers"), LiteracyGroupType, FirstLevel)
    )

    val classDetails = ClassDetails(
      ClassId("classId123"),
      ClassName("P3AB"),
      groups,
      List(Teacher(Some(TeacherId("id12")), "Andy", "Boyle"))
    )

    val classDetailsPickled = classDetails.pickle
    val classDetailsUnpickled = classDetailsPickled.unpickle[ClassDetails]

    it("should have a class id of 'classId123'") {
      assert(classDetailsUnpickled.id.id === "classId123")
    }
    it("should have a one class teacher") {
      assert(classDetailsUnpickled.classTeachers.size === 1)
    }
    it("should have 6 groups") {
      assert(classDetailsUnpickled.groups.size === 6)
    }
  }

}
