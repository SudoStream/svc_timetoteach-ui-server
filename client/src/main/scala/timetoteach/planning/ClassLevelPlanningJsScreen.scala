package timetoteach.planning

import duplicate.model.{EandOsWithBenchmarks, TermlyPlansToSave}
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.ext.Ajax.InputData
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement, NodeList}
import upickle.default.write

import scala.collection.mutable
import scala.scalajs.js.Dynamic.global
import scala.util.{Failure, Success}

object ClassLevelPlanningJsScreen
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
    setCurriculumLevelFilterButtonsToNotDisplay()
    curriculumLevelFilterButton("Early")
    curriculumLevelFilterButton("First")
    curriculumLevelFilterButton("Second")
    curriculumLevelFilterButton("Third")
    curriculumLevelFilterButton("Fourth")
    showAlertIfAllFilterButtonsAreOff()
  }

  def showAlertIfAllFilterButtonsAreOff(): Unit =
  {
    if (allFilterButtonsAreOff()) {
      dom.document.getElementById("alert-div").asInstanceOf[HTMLDivElement].style.display = "block"
    } else {
      dom.document.getElementById("alert-div").asInstanceOf[HTMLDivElement].style.display = "none"
    }
  }

  private def allElementsAreOff(nodes: NodeList, tag: String): Boolean =
  {
    val nodeListSize = nodes.length
    var index = 0
    var numberElemsOff = 0
    while (index < nodeListSize) {
      val div = nodes(index).asInstanceOf[HTMLDivElement]
      if (div.style.display == "none") {
        numberElemsOff = numberElemsOff + 1
      }

      index = index + 1
    }

    numberElemsOff == nodeListSize
  }

  def allFilterButtonsAreOff(): Boolean =
  {
    val earlys = dom.document.getElementsByClassName("termly-plans-es-and-os-level-section-div-EarlyLevel")
    val firsts = dom.document.getElementsByClassName("termly-plans-es-and-os-level-section-div-FirstLevel")
    val seconds = dom.document.getElementsByClassName("termly-plans-es-and-os-level-section-div-SecondLevel")
    val thirds = dom.document.getElementsByClassName("termly-plans-es-and-os-level-section-div-ThirdLevel")
    val fourths = dom.document.getElementsByClassName("termly-plans-es-and-os-level-section-div-FourthLevel")

    allElementsAreOff(earlys, "earlys") && allElementsAreOff(firsts, "firsts") && allElementsAreOff(seconds, "seconds") &&
      allElementsAreOff(thirds, "thirds") && allElementsAreOff(fourths, "fourths")
  }

  private def setNodesToNotDisplay(nodes: NodeList): Unit =
  {
    val nodeListSize = nodes.length
    var index = 0
    while (index < nodeListSize) {
      val div = nodes(index).asInstanceOf[HTMLDivElement]
      div.style.display = "none"
      index = index + 1
    }
  }

  def setCurriculumLevelFilterButtonsToNotDisplay(): Unit =
  {
    setNodesToNotDisplay(dom.document.getElementsByClassName("termly-plans-es-and-os-level-section-div-EarlyLevel"))
    setNodesToNotDisplay(dom.document.getElementsByClassName("termly-plans-es-and-os-level-section-div-FirstLevel"))
    setNodesToNotDisplay(dom.document.getElementsByClassName("termly-plans-es-and-os-level-section-div-SecondLevel"))
    setNodesToNotDisplay(dom.document.getElementsByClassName("termly-plans-es-and-os-level-section-div-ThirdLevel"))
    setNodesToNotDisplay(dom.document.getElementsByClassName("termly-plans-es-and-os-level-section-div-FourthLevel"))
  }

  private def curriculumLevelFilterButtonInstance(nodes: NodeList, curriculumLevelFilterButton: HTMLButtonElement): Unit =
  {
    val nodeListSize = nodes.length
    var index = 0
    while (index < nodeListSize) {
      val levelSectionDiv = nodes(index).asInstanceOf[HTMLDivElement]
      if (levelSectionDiv.style.display == "none") {
        levelSectionDiv.style.display = "block"
        curriculumLevelFilterButton.classList.remove("btn-outline-info")
        curriculumLevelFilterButton.classList.add("btn-warning")
        showAlertIfAllFilterButtonsAreOff()
      } else {
        levelSectionDiv.style.display = "none"
        curriculumLevelFilterButton.classList.add("btn-outline-info")
        curriculumLevelFilterButton.classList.remove("btn-warning")
        showAlertIfAllFilterButtonsAreOff()
      }

      index = index + 1
    }
  }

  def headerVisibility(): Unit =
  {
    val currentHeaders = scala.collection.mutable.ArrayBuffer.empty[String]
    val headerDivs = dom.document.getElementsByClassName("termly-plans-es-and-os-level-section-header")
    val nodeListSize = headerDivs.length
    var index = 0
    while (index < nodeListSize) {
      val headerDiv = headerDivs(index).asInstanceOf[HTMLDivElement]
      val parentHeaderDiv = headerDiv.parentNode.asInstanceOf[HTMLDivElement]
      val curriculumLevel = headerDiv.getAttribute("data-curriculum-level")
      global.console.log(s"curriculumLevel = ${curriculumLevel}")
      if (currentHeaders.contains(curriculumLevel)) {
        headerDiv.asInstanceOf[HTMLDivElement].style.display = "none"
      } else {
        headerDiv.asInstanceOf[HTMLDivElement].style.display = "block"
        currentHeaders += curriculumLevel
      }

      index = index + 1
    }
  }

  def curriculumLevelFilterButton(level: String): Unit =
  {
    val curriculumLevelFilterButton = dom.document.getElementById(s"curriculumLevel${level}FilterButton").asInstanceOf[HTMLButtonElement]
    curriculumLevelFilterButton.addEventListener("click", (e: dom.Event) => {
      val levelSectionDivs = dom.document.getElementsByClassName(s"termly-plans-es-and-os-level-section-div-${level}Level")
      curriculumLevelFilterButtonInstance(levelSectionDivs, curriculumLevelFilterButton)
      headerVisibility()
    })
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
        esAndOsToBenchmarksTuple._1.toList,
        esAndOsToBenchmarksTuple._2.toList
      )
    }.toList
  }

  def saveButton(): Unit =
  {
    val saveButton = dom.document.getElementById("save-termly-groups-button").asInstanceOf[HTMLButtonElement]
    saveButton.addEventListener("click", (e: dom.Event) => {
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

      val groupTermlyPlansPickled = write[TermlyPlansToSave](groupTermlyPlans)

      val classId = dom.window.localStorage.getItem("classId")
      val subject = dom.window.localStorage.getItem("subject")
      val groupId = dom.window.localStorage.getItem("groupId")

      import scala.concurrent.ExecutionContext.Implicits.global
      val theUrl = s"/termlysaveplanningforsubjectandgroup/$classId/$subject/$groupId"
      val theHeaders = Map(
        "Content-Type" -> "application/x-www-form-urlencoded",
        "X-Requested-With" -> "Accept"
      )
      val theData = InputData.str2ajax(s"groupTermlyPlansPickled=$groupTermlyPlansPickled")

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

        global.console.log(s"Selected Benchmark '$benchmarkValue'")
        if (
          (selectedEsAndOsWithBenchmarks.nonEmpty &&
            selectedEsAndOsWithBenchmarks.isDefinedAt(curriculumSection) &&
            selectedEsAndOsWithBenchmarks(curriculumSection).isDefinedAt(curriculumSubSection)) &&
            selectedEsAndOsWithBenchmarks(curriculumSection)(curriculumSubSection)._1.contains(benchmarkValue)) {
          selectedEsAndOsWithBenchmarks(curriculumSection)(curriculumSubSection)._2.remove(benchmarkValue)
          setButtonDefaults(theDiv)
        } else {
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
