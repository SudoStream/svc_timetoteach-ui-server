package shared.model

import org.scalatest._
import shared.model.classtimetable.SubjectName

class SubjectNameTest extends FunSpec {
  describe("Subject on creation with valid name") {
    it("should have the same value as passed in") {
        val subject = SubjectName("subject-spelling")
      assert(subject.value == "subject-spelling")
    }
  }

  describe("Subject on creation with invalid name") {
    it("should through an runtime exception") {
      assertThrows[RuntimeException] {
        SubjectName("invalid-name")
      }
    }
  }
}
