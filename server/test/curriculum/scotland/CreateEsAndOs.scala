package curriculum.scotland

import io.sudostream.timetoteach.messages.scottish._

trait CreateEsAndOs {

  def createEmptyScottishEsAndOsData(): ScottishEsAndOsData = {
    ScottishEsAndOsData(Nil)
  }

  def createScottishEsAndOsData(): ScottishEsAndOsData = {
    ScottishEsAndOsData(
      allExperiencesAndOutcomes = List(

        ScottishEsAndOsBySubSection(
          experienceAndOutcomes = List(
            SingleScottishExperienceAndOutcome(
              code = "MNU 0-01a",
              eAndOLines = List(
                ScottishExperienceAndOutcomeLine(
                  sentence = "I am developing a sense of size and amount by observing, exploring, using and communicating with others about things in the world around me",
                  bulletPoints = Nil
                )
              )
            )
          ),
          scottishCurriculumLevel = ScottishCurriculumLevel.EARLY,
          associatedBenchmarks = List(
            "Recognises the number of objects in a group, without counting (subitising)\nand uses this information to estimate the number of objects in other groups.",
            "Checks estimates by counting.",
            "Demonstrates skills of estimation in the contexts of number and measure using relevant vocabulary, including less than, longer than, more than and the same."
          ),
          curriculumAreaName = ScottishCurriculumAreaName.MATHEMATICS,
          eAndOSetSectionName = "Number, money and measure",
          eAndOSetSubSectionName = Some("Estimation and rounding"),
          eAndOSetSubSectionAuxiliaryText = Option.empty,
          responsibilityOfAllPractitioners = false
        ),

        ScottishEsAndOsBySubSection(
          experienceAndOutcomes = List(
            SingleScottishExperienceAndOutcome(
              code = "MNU 1-01a",
              eAndOLines = List(
                ScottishExperienceAndOutcomeLine(
                  sentence = "I can share ideas with others to develop ways of estimating the answer to a calculation" +
                    " or problem, work out the actual answer, then check my solution by comparing it with the estimate.",
                  bulletPoints = Nil
                )
              )
            )
          ),
          scottishCurriculumLevel = ScottishCurriculumLevel.FIRST,
          associatedBenchmarks = List(
            "Uses strategies to estimate an answer to a calculation or problem, for example, doubling and rounding.",
            "Rounds whole numbers to the nearest 10 and 100 and uses this routinely to estimate and check the reasonableness of a solution."
          ),
          curriculumAreaName = ScottishCurriculumAreaName.MATHEMATICS,
          eAndOSetSectionName = "Number, money and measure",
          eAndOSetSubSectionName = Some("Estimation and rounding"),
          eAndOSetSubSectionAuxiliaryText = Option.empty,
          responsibilityOfAllPractitioners = false
        ),

        ScottishEsAndOsBySubSection(
          experienceAndOutcomes = List(
            SingleScottishExperienceAndOutcome(
              code = "MNU 0-02a",
              eAndOLines = List(
                ScottishExperienceAndOutcomeLine(
                  sentence = "I have explored numbers, understanding that they represent quantities, and I can use them to count, create sequences and describe order.",
                  bulletPoints = Nil
                )
              )
            ),
            SingleScottishExperienceAndOutcome(
              code = "MNU 0-03a",
              eAndOLines = List(
                ScottishExperienceAndOutcomeLine(
                  sentence = "I use practical materials and can ‘count on and back’ to help me to understand addition and subtraction, recording my ideas and solutions in different ways.",
                  bulletPoints = Nil
                )
              )
            )
          ),
          scottishCurriculumLevel = ScottishCurriculumLevel.EARLY,
          associatedBenchmarks = List(
            "Explains that zero means there is none of a particular quantity and is represented by the numeral 0.",
            "Recalls the number sequence forwards within the range 0 - 30, from any given number.",
            "Recalls the number sequence backwards from 20.",
            "Identifies and recognises numbers from 0 to 20.",
            "Orders all numbers forwards and backwards within the range 0 - 20.",
            "Identifies the number before, the number after and missing numbers in a sequence within 20",
            "Uses one-to-one correspondence to count a given number of objects to 20",
            "Identifies ‘how many?’ in regular dot patterns, for example, arrays, five frames, ten frames, dice and irregular dot patterns, without having to count (subitising)",
            "Groups items recognising that the appearance of the group has no effect on the overall total (conservation of number).",
            "Uses ordinal numbers in real life contexts, for example, ‘I am third in the line’.",
            "Uses the language of before, after and in-between.",
            "Counts on and back in ones to add and subtract.",
            "Doubles numbers to a total of 10 mentally.",
            "When counting objects, understands that the number name of the last object counted is the name given to the total number of objects in the group"
          ),
          curriculumAreaName = ScottishCurriculumAreaName.MATHEMATICS,
          eAndOSetSectionName = "Number, money and measure",
          eAndOSetSubSectionName = Some("Number and number processes"),
          eAndOSetSubSectionAuxiliaryText = Some("including addition, subtraction, multiplication, division and negative numbers"),
          responsibilityOfAllPractitioners = false
        )
      )
    )
  }
}
