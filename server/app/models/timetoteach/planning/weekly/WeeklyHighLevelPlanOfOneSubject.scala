package models.timetoteach.planning.weekly

import java.time.{LocalDate, LocalDateTime}

import duplicate.model.esandos.EsAndOsPlusBenchmarksForCurriculumAreaAndLevel
import models.timetoteach.planning.ScottishCurriculumPlanningAreaWrapper
import models.timetoteach.{ClassId, TimeToTeachUserId}

case class WeeklyHighLevelPlanOfOneSubject(
                                timeToTeachUserId: TimeToTeachUserId,
                                classId: ClassId,
                                subject: ScottishCurriculumPlanningAreaWrapper,
                                weekBeginning: LocalDate,
                                timestamp: LocalDateTime,
                                selectedEsOsBenchmarksByGroup: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel],
                                completedEsOsBenchmarksByGroup: Map[String, EsAndOsPlusBenchmarksForCurriculumAreaAndLevel]
                              )
