package shared.model.planning

case class Lesson(
                   activities: List[Activity],
                   resources: List[Resource],
                   learningIntentions: List[LearningIntention],
                   successCriteria: List[SuccessCriteria],
                   plenary: Plenary,
                   formativeAssessment: FormativeAssessment,
                   notes: List[Note]
                 ) {

}

case class Activity(value: String)

case class Resource(value: String)

case class LearningIntention(value: String)

case class SuccessCriteria(value: String)

case class Plenary(value: String)

case class FormativeAssessment(value: String)

case class Note(value: String)
