package utils

import io.sudostream.timetoteach.messages.scottish.ScottishCurriculumLevel
import org.scalatest._
import shared.model.classdetail._
import utils.ClassDetailsAvroConverter.convertPickledClassToAvro

class ClassDetailsAvroConverterTest extends FunSpec {

  private val classDetails = ClassDetails(
    ClassId("id123"),
    ClassName("P1AB"),
    List(
      Group(
        GroupId("groupId1"),
        GroupName("Triangles"),
        MathsGroupType,
        EarlyLevel),
      Group(
        GroupId("groupId2"),
        GroupName("Squares"),
        MathsGroupType,
        FirstLevel),
      Group(
        GroupId("groupId2"),
        GroupName("Circles"),
        MathsGroupType,
        FirstLevel)
    ),
    Nil
  )

  describe("ClassDetailsAvroConverter given a pickled ClassDetails") {
    it("should convert Pickled Class To Avro with class id 123") {
      val classDetailsAvro = convertPickledClassToAvro(classDetails)
      assert(classDetailsAvro.classId.value === "id123")
    }
    it("should convert Pickled Class To Avro with class name P1AB") {
      val classDetailsAvro = convertPickledClassToAvro(classDetails)
      assert(classDetailsAvro.className.value === "P1AB")
    }
    it("should convert Pickled Class To Avro with 3 group") {
      val classDetailsAvro = convertPickledClassToAvro(classDetails)
      assert(classDetailsAvro.classGroups.size === 3)
    }
    it("should convert Pickled Class To Avro with 1 Early level group") {
      val classDetailsAvro = convertPickledClassToAvro(classDetails)
      assert(classDetailsAvro.classGroups.count(_.group.groupLevel === ScottishCurriculumLevel.EARLY) === 1)
    }
    it("should convert Pickled Class To Avro with 2 First level group") {
      val classDetailsAvro = convertPickledClassToAvro(classDetails)
      assert(classDetailsAvro.classGroups.count(_.group.groupLevel === ScottishCurriculumLevel.FIRST) === 2)
    }

  }

}
