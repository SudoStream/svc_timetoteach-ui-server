package timetoteach.planning.termly

import duplicate.model.{EandOsWithBenchmarks, TermlyPlansToSave}
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.ext.Ajax.InputData
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement}
import shared.util.PlanningHelper
import upickle.default.write

import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.Dynamic.global
import scala.util.{Failure, Success}

object ClassGroupPlanningJsScreen
{

  var selectedEsAndOsWithBenchmarks: scala.collection.mutable.Map[String, scala.collection.mutable.Map[String,
    (scala.collection.mutable.Set[String], scala.collection.mutable.Set[String])]] = scala.collection.mutable.Map.empty

  var eAndORowBackgroundNormalColor: Option[String] = None
  var eAndORowForegroundNormalColor: Option[String] = None
  var eAndORowBorderRadius: Option[String] = None

  def loadJavascript(): Unit =
  {
    mouseoverHighlightEandOsAndBenchmarks()
    clickOnEandO()
    clickOnBenchmark()
    saveButton()
    clearButton()
//    popovers()
  }

  private def popovers(): Unit =
  {
    val alert = dom.document.getElementById("es-and-os-alert").asInstanceOf[HTMLDivElement]
    if (alert != null) {
      dom.window.setTimeout(() => {
        alert.style.display = "block"
      }, 10)
    }
  }


  def clearButton(): Unit =
  {
    val clearButton = dom.document.getElementById("clear-termly-groups-button").asInstanceOf[HTMLButtonElement]
    clearButton.addEventListener("click", (e: dom.Event) => {
      selectedEsAndOsWithBenchmarks.clear()
      setDefaultsForAllButtonsByClass("termly-plans-es-and-os-code-and-eando-row")
      setDefaultsForAllButtonsByClass("termly-plans-es-and-os-benchmark")
    })
  }

  private def setDefaultsForAllButtonsByClass(classValue: String): Unit =
  {
    val element = dom.document.getElementsByClassName(classValue)
    val nodeListSize = element.length
    var index = 0
    while (index < nodeListSize) {
      val theDiv = element(index).asInstanceOf[HTMLDivElement]
      setButtonDefaults(theDiv)
      index = index + 1
    }
  }

  private def setButtonDefaults(theDiv: HTMLDivElement): Unit =
  {
    theDiv.style.backgroundColor = eAndORowBackgroundNormalColor.getOrElse("white")
    theDiv.style.color = eAndORowForegroundNormalColor.getOrElse("grey")
    theDiv.style.borderRadius = eAndORowBorderRadius.getOrElse("0")
  }

  def convertEsAndOsToListFormat(selectedEsAndOsWithBenchmarks: mutable.Map[String, mutable.Map[String, (mutable.Set[String], mutable.Set[String])]]): List[EandOsWithBenchmarks] =
  {
    {
      for {
        section <- selectedEsAndOsWithBenchmarks.keys
        subsection <- selectedEsAndOsWithBenchmarks(section).keys
        esAndOsToBenchmarksTuple = selectedEsAndOsWithBenchmarks(section)(subsection)
      } yield EandOsWithBenchmarks(
        section,
        subsection,
        esAndOsToBenchmarksTuple._1.toList,
        esAndOsToBenchmarksTuple._2.toList
      )
    }.toList
  }

  def saveButton(): Unit =
  {
    val saveButton = dom.document.getElementById("save-termly-groups-button").asInstanceOf[HTMLButtonElement]
    saveButton.addEventListener("click", (e: dom.Event) => {
      val $ = js.Dynamic.global.$
      $("#doing-stuff").modal("show", "backdrop: static", "keyboard : false")

      saveButton.disabled = true

      global.console.log("Selected:\n" +
        s"E&O Codes With Benchmarks: |||${selectedEsAndOsWithBenchmarks.toString()}|||\n"
      )

      val tttUserId = dom.window.localStorage.getItem("tttUserId")
      val schoolId = dom.window.localStorage.getItem("schoolId")

      val esAndOsWithTheirBenchmarks: List[EandOsWithBenchmarks] = convertEsAndOsToListFormat(selectedEsAndOsWithBenchmarks)

      val groupTermlyPlans = TermlyPlansToSave(
        tttUserId = tttUserId,
        esAndOsWithTheirBenchmarks
      )

      val groupTermlyPlansPickled = PlanningHelper.encodeAnyJawnNonFriendlyCharacters(write[TermlyPlansToSave](groupTermlyPlans))
      global.console.log(s"Pickled, this == $groupTermlyPlansPickled")

      val classId = dom.window.localStorage.getItem("classId")
      val subject = dom.window.localStorage.getItem("subject")
      val groupId = dom.window.localStorage.getItem("groupId")

      import scala.concurrent.ExecutionContext.Implicits.global
      val theUrl = s"/termlysaveplanningforsubjectandgroup/$classId/$subject/$groupId"
      val theHeaders = Map(
        "Content-Type" -> "application/x-www-form-urlencoded",
        "X-Requested-With" -> "Accept"
      )
      val theData = InputData.str2ajax(s"termlyPlansPickled=$groupTermlyPlansPickled&tttUserId=$tttUserId")

      Ajax.post(
        url = theUrl,
        headers = theHeaders,
        data = theData
      ).onComplete {
        case Success(xhr) =>
          val responseText = xhr.responseText
          println(s"response = '$responseText'")
          dom.window.setTimeout(() => {
            println(s"lets goto group planning overview")
            dom.window.location.href = s"/termlyoverviewforcurriculumareaandgroup/$classId/$subject/$groupId"
          }, 10)
        case Failure(ex) =>
          dom.window.alert("Something went wrong with saving group termly plans. Specifically : -" +
            s"\n\n${ex.toString}")
      }





    })
  }

  def clickOnEandO(): Unit =
  {
    val allEAndORows = dom.document.getElementsByClassName("termly-plans-es-and-os-code-and-eando-row")
    val nodeListSize = allEAndORows.length
    var index = 0
    while (index < nodeListSize) {
      val theDiv = allEAndORows(index).asInstanceOf[HTMLDivElement]

      theDiv.addEventListener("click", (e: dom.Event) => {
        val eAndOCode = theDiv.getAttribute("data-eando-code")
        val curriculumSection = theDiv.getAttribute("data-curriculum-section")
        val curriculumSubSection = theDiv.getAttribute("data-curriculum-subsection")

        global.console.log(s"Selected E and O code '$eAndOCode'")

        if ((selectedEsAndOsWithBenchmarks.nonEmpty &&
          selectedEsAndOsWithBenchmarks.isDefinedAt(curriculumSection) &&
          selectedEsAndOsWithBenchmarks(curriculumSection).isDefinedAt(curriculumSubSection)) &&
          selectedEsAndOsWithBenchmarks(curriculumSection)(curriculumSubSection)._1.contains(eAndOCode)) {
          selectedEsAndOsWithBenchmarks(curriculumSection)(curriculumSubSection)._1.remove(eAndOCode)
          setButtonDefaults(theDiv)
        } else {
          if (!selectedEsAndOsWithBenchmarks.isDefinedAt(curriculumSection)) {
            selectedEsAndOsWithBenchmarks = selectedEsAndOsWithBenchmarks + (curriculumSection -> mutable.Map.empty)
          }
          if (!selectedEsAndOsWithBenchmarks(curriculumSection).isDefinedAt(curriculumSubSection)) {
            selectedEsAndOsWithBenchmarks(curriculumSection)(curriculumSubSection) = (scala.collection.mutable.Set.empty, scala.collection.mutable.Set.empty)
          }

          selectedEsAndOsWithBenchmarks(curriculumSection)(curriculumSubSection)._1.add(eAndOCode)
          global.console.log(s"Should be now .. values of Es and Os are ... ${selectedEsAndOsWithBenchmarks(curriculumSection)(curriculumSubSection)._1.toString()}")
          theDiv.style.backgroundColor = "#016ecd"
          theDiv.style.color = "white"
          theDiv.style.borderRadius = "7px"
        }
      })


      index = index + 1
    }
  }

  def clickOnBenchmark(): Unit =
  {
    val allBenchmarkRows = dom.document.getElementsByClassName("termly-plans-es-and-os-benchmark")
    val nodeListSize = allBenchmarkRows.length
    var index = 0
    while (index < nodeListSize) {
      val theDiv = allBenchmarkRows(index).asInstanceOf[HTMLDivElement]

      theDiv.addEventListener("click", (e: dom.Event) => {
        val benchmarkValue = theDiv.getAttribute("data-benchmark")
        val curriculumSection = theDiv.getAttribute("data-curriculum-section")
        val curriculumSubSection = theDiv.getAttribute("data-curriculum-subsection")

        global.console.log(s"Selected the Benchmark '$benchmarkValue'||$curriculumSection||$curriculumSubSection")
        if (
          (selectedEsAndOsWithBenchmarks.nonEmpty &&
            selectedEsAndOsWithBenchmarks.isDefinedAt(curriculumSection) &&
            selectedEsAndOsWithBenchmarks(curriculumSection).isDefinedAt(curriculumSubSection)) &&
            selectedEsAndOsWithBenchmarks(curriculumSection)(curriculumSubSection)._2.contains(benchmarkValue)) {
          selectedEsAndOsWithBenchmarks(curriculumSection)(curriculumSubSection)._2.remove(benchmarkValue)
          setButtonDefaults(theDiv)
        } else {
          if (!selectedEsAndOsWithBenchmarks.isDefinedAt(curriculumSection)) {
            selectedEsAndOsWithBenchmarks = selectedEsAndOsWithBenchmarks + (curriculumSection -> mutable.Map.empty)
          }
          if (!selectedEsAndOsWithBenchmarks(curriculumSection).isDefinedAt(curriculumSubSection)) {
            selectedEsAndOsWithBenchmarks(curriculumSection)(curriculumSubSection) = (scala.collection.mutable.Set.empty, scala.collection.mutable.Set.empty)
          }

          selectedEsAndOsWithBenchmarks(curriculumSection)(curriculumSubSection)._2.add(benchmarkValue)
          theDiv.style.backgroundColor = "#016ecd"
          theDiv.style.color = "white"
          theDiv.style.borderRadius = "7px"
        }
      })

      index = index + 1
    }
  }


  def mouseoverHighlightEandOsAndBenchmarks(): Unit =
  {
    val allEAndOAndBenchmarkRows = dom.document.getElementsByClassName("termly-plans-eobenchmark-row")
    val nodeListSize = allEAndOAndBenchmarkRows.length
    var index = 0
    while (index < nodeListSize) {
      val theDiv = allEAndOAndBenchmarkRows(index).asInstanceOf[HTMLDivElement]

      theDiv.addEventListener("mouseover", (e: dom.Event) => {
        if (eAndORowBackgroundNormalColor.isEmpty) {
          eAndORowBackgroundNormalColor = Some(theDiv.style.backgroundColor)
        }
        if (eAndORowForegroundNormalColor.isEmpty) {
          eAndORowForegroundNormalColor = Some(theDiv.style.color)
        }
        if (eAndORowBorderRadius.isEmpty) {
          eAndORowBorderRadius = Some(theDiv.style.borderRadius)
        }

        val eAndOCode = theDiv.getAttribute("data-eando-code")
        val benchmarkValue = theDiv.getAttribute("data-benchmark")
        val curriculumSection = theDiv.getAttribute("data-curriculum-section")
        val curriculumSubSection = theDiv.getAttribute("data-curriculum-subsection")

        if ((eAndOCode != null && eAndOCode.nonEmpty &&
          (selectedEsAndOsWithBenchmarks.isEmpty ||
            !selectedEsAndOsWithBenchmarks.isDefinedAt(curriculumSection) ||
            !selectedEsAndOsWithBenchmarks(curriculumSection).isDefinedAt(curriculumSubSection) ||
            !selectedEsAndOsWithBenchmarks(curriculumSection)(curriculumSubSection)._1.contains(eAndOCode))) ||
          (benchmarkValue != null && benchmarkValue.nonEmpty &&
            (selectedEsAndOsWithBenchmarks.isEmpty ||
              !selectedEsAndOsWithBenchmarks.isDefinedAt(curriculumSection) ||
              !selectedEsAndOsWithBenchmarks(curriculumSection).isDefinedAt(curriculumSubSection) ||
              !selectedEsAndOsWithBenchmarks(curriculumSection)(curriculumSubSection)._2.contains(benchmarkValue)))) {
          theDiv.style.backgroundColor = "grey"
          theDiv.style.color = "white"
          theDiv.style.borderRadius = "7px"
        }
      })

      theDiv.addEventListener("mouseleave", (e: dom.Event) => {
        val eAndOCode = theDiv.getAttribute("data-eando-code")
        val benchmarkValue = theDiv.getAttribute("data-benchmark")
        val curriculumSection = theDiv.getAttribute("data-curriculum-section")
        val curriculumSubSection = theDiv.getAttribute("data-curriculum-subsection")
        if ((eAndOCode != null && eAndOCode.nonEmpty &&
          (selectedEsAndOsWithBenchmarks.isEmpty ||
            !selectedEsAndOsWithBenchmarks.isDefinedAt(curriculumSection) ||
            !selectedEsAndOsWithBenchmarks(curriculumSection).isDefinedAt(curriculumSubSection) ||
            !selectedEsAndOsWithBenchmarks(curriculumSection)(curriculumSubSection)._1.contains(eAndOCode))) ||
          (benchmarkValue != null && benchmarkValue.nonEmpty &&
            (selectedEsAndOsWithBenchmarks.isEmpty ||
              !selectedEsAndOsWithBenchmarks.isDefinedAt(curriculumSection) ||
              !selectedEsAndOsWithBenchmarks(curriculumSection).isDefinedAt(curriculumSubSection) ||
              !selectedEsAndOsWithBenchmarks(curriculumSection)(curriculumSubSection)._2.contains(benchmarkValue)))) {
          theDiv.style.backgroundColor = eAndORowBackgroundNormalColor.getOrElse("white")
          theDiv.style.color = eAndORowForegroundNormalColor.getOrElse("grey")
          theDiv.style.borderRadius = eAndORowBorderRadius.getOrElse("0")
        }
      })


      index = index + 1
    }


  }

}
