package controllers.planning.termly

import java.time.LocalDateTime

import controllers.serviceproxies.TermServiceProxyImpl
import duplicate.model.{EandOsWithBenchmarks, TermlyPlansToSave}
import io.sudostream.timetoteach.messages.systemwide.model.classtimetable.subjectdetail.SubjectName
import models.timetoteach.planning.{GroupId, PlanType}
import models.timetoteach.term.SchoolTermName
import org.scalatest.FunSpec

class TermPlansHelperTest extends FunSpec {

  private val SCHOOL_ID = "school3333"
  private val CLASS_ID = "classId12312321"
  private val USER_ID = "userId123"
  private val E_AND_O_CODES = List("CODE1", "CODE2")
  private val BENCHMARKS = List("Benchmark1", "Benchmark2", "Benchmark3")
  private val GROUP_ID = "groupAbc"
  private val SUBJECT_MATHS = "Maths"

  describe("Given a TermlyPlansToSave, a group id and a subject 'maths', TermPlansHelper.convertTermlyPlanToModel(...)") {
    val planHelper = new TermPlansHelper(new TermServiceProxyImpl)
    val subjectTermlyPlan = planHelper.convertTermlyPlanToModel(
      CLASS_ID,
      TermlyPlansToSave(
        USER_ID,
        List(EandOsWithBenchmarks(
          E_AND_O_CODES,
          BENCHMARKS
        ))
      ),
      Some(GroupId(GROUP_ID)),
      SUBJECT_MATHS
    )

    it("should create a SubjectTermlyPlan with a timestamp after 2 minutes ago ") {
      assert(subjectTermlyPlan.createdTime.isAfter(LocalDateTime.now().minusMinutes(2)))
    }

    it("should create a SubjectTermlyPlan with a group id = 'groupAbc") {
      assert(subjectTermlyPlan.maybeGroupId.get.value === GROUP_ID)
    }

    it("should create a SubjectTermlyPlan with a school term of spring second term") {
      assert(subjectTermlyPlan.schoolTerm.schoolTermName === SchoolTermName.SPRING_SECOND_TERM)
    }

    it("should create a SubjectTermlyPlan with a subject of maths") {
      assert(subjectTermlyPlan.subject === SubjectName.MATHS)
    }

    it("should create a SubjectTermlyPlan with 1 full list of esos with benchmarks") {
      assert(subjectTermlyPlan.eandOsWithBenchmarks.size === 1)
    }

    it("should create a SubjectTermlyPlan with 2 es and os") {
      assert(subjectTermlyPlan.eandOsWithBenchmarks.head.eAndOCodes.size === 2)
    }

    it("should create a SubjectTermlyPlan with 3 benchmarks") {
      assert(subjectTermlyPlan.eandOsWithBenchmarks.head.benchmarks.size === 3)
    }

  }
}
