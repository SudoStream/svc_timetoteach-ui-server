package shared.model.classdetail

import org.scalatest._
import shared.model.classdetail.CurriculumLevel.CurriculumLevel
import shared.model.classtimetable.SchoolDayStarts

class GroupTypeTest extends FunSpec {
  describe("When created a class teacher with name 'Andy' 'Boyle' and no id") {
    val teacher = Teacher(None,"Andy","Boyle")

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
    val group = Group("triangles", GroupType.Maths, CurriculumLevel.First )

    it("should have a name of 'triangles'") {
      assert(group.groupName === "triangles")
    }
    it("should have a type of 'maths'") {
      assert(group.groupType === GroupType.Maths)
    }
    it("should have a level of 'first'") {
      assert(group.groupLevel === CurriculumLevel.First)
    }
  }

}
