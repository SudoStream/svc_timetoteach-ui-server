package timetoteach.planning.weekly

import org.scalajs.dom
import org.scalajs.dom.raw.HTMLDivElement
import timetoteach.planning.termly.ClassGroupPlanningJsScreen.{eAndORowBackgroundNormalColor, eAndORowBorderRadius, eAndORowForegroundNormalColor, selectedEsAndOsWithBenchmarks, setButtonDefaults, _}

import scala.collection.mutable
import scala.scalajs.js.Dynamic.global

object CreatePlanForTheWeekJsScreen {

  var selectedEsAndOsWithBenchmarks: scala.collection.mutable.Map[String, scala.collection.mutable.Map[String,
    (scala.collection.mutable.Set[String], scala.collection.mutable.Set[String])]] = scala.collection.mutable.Map.empty

  var eAndORowBackgroundNormalColor: Option[String] = None
  var eAndORowForegroundNormalColor: Option[String] = None
  var eAndORowBorderRadius: Option[String] = None

  def loadJavascript(): Unit =
  {
    global.console.log("Loading Create Plan For The Week Javascript")
    mouseoverHighlightEandOsAndBenchmarks()
    clickOnEandO()
    clickOnBenchmark()
  }

  private def setButtonDefaults(theDiv: HTMLDivElement): Unit =
  {
    theDiv.style.backgroundColor = eAndORowBackgroundNormalColor.getOrElse("white")
    theDiv.style.color = eAndORowForegroundNormalColor.getOrElse("grey")
    theDiv.style.borderRadius = eAndORowBorderRadius.getOrElse("0")
  }

  def clickOnEandO(): Unit =
  {
    val allEAndORows = dom.document.getElementsByClassName("create-weekly-plans-es-and-os-row")
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


  def mouseoverHighlightEandOsAndBenchmarks(): Unit =
  {
    val allEAndOAndBenchmarkRows = dom.document.getElementsByClassName("create-weekly-plans-eobenchmark-row")
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

  def clickOnBenchmark(): Unit =
  {
    val allBenchmarkRows = dom.document.getElementsByClassName("create-weekly-plans-benchmark-row")
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


}


