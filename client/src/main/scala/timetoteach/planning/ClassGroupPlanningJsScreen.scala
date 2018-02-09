package timetoteach.planning

import duplicate.model.TermlyPlansToSave
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.ext.Ajax.InputData
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement}
import upickle.default.write

import scala.scalajs.js.Dynamic.global
import scala.util.{Failure, Success}
import scalatags.JsDom.all.s

object ClassGroupPlanningJsScreen {

  var selectedEsAndOs: scala.collection.mutable.Set[String] = scala.collection.mutable.Set.empty[String]
  var selectedBenchmarks: scala.collection.mutable.Set[String] = scala.collection.mutable.Set.empty[String]

  var eAndORowBackgroundNormalColor: Option[String] = None
  var eAndORowForegroundNormalColor: Option[String] = None
  var eAndORowBorderRadius: Option[String] = None

  def loadJavascript(): Unit = {
    mouseoverHighlightEandOsAndBenchmarks()
    clickOnEandO()
    clickOnBenchmark()
    saveButton()
    clearButton()
  }

  def clearButton(): Unit = {
    val clearButton = dom.document.getElementById("clear-termly-groups-button").asInstanceOf[HTMLButtonElement]
    clearButton.addEventListener("click", (e: dom.Event) => {
      selectedEsAndOs.clear()
      selectedBenchmarks.clear()
      setDefaultsForAllButtonsByClass("termly-plans-es-and-os-code-and-eando-row")
      setDefaultsForAllButtonsByClass("termly-plans-es-and-os-benchmark")
    })
  }

  private def setDefaultsForAllButtonsByClass(classValue: String): Unit = {
    val element = dom.document.getElementsByClassName(classValue)
    val nodeListSize = element.length
    var index = 0
    while (index < nodeListSize) {
      val theDiv = element(index).asInstanceOf[HTMLDivElement]
      setButtonDefaults(theDiv)
      index = index + 1
    }
  }

  private def setButtonDefaults(theDiv: HTMLDivElement): Unit = {
    theDiv.style.backgroundColor = eAndORowBackgroundNormalColor.getOrElse("white")
    theDiv.style.color = eAndORowForegroundNormalColor.getOrElse("grey")
    theDiv.style.borderRadius = eAndORowBorderRadius.getOrElse("0")
  }

  def saveButton(): Unit = {
    val saveButton = dom.document.getElementById("save-termly-groups-button").asInstanceOf[HTMLButtonElement]
    saveButton.addEventListener("click", (e: dom.Event) => {
      saveButton.disabled = true

      global.console.log("Selected:\n" +
        s"E&O Codes: ${selectedEsAndOs.toString()}\n" +
        s"Benchmarks: ${selectedBenchmarks.mkString("\n")}"
      )

      val tttUserId = dom.window.localStorage.getItem("tttUserId")
      val schoolId = dom.window.localStorage.getItem("schoolId")

      val groupTermlyPlans = TermlyPlansToSave(
        schoolId = schoolId,
        tttUserId = tttUserId,
        eAndOCodes = selectedEsAndOs.toList,
        benchmarks = selectedBenchmarks.toList
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
            dom.window.location.href = s"/termlyplanningforclass/$classId"
          }, 10)
        case Failure(ex) =>
          dom.window.alert("Something went wrong with saving group termly plans. Specifically : -" +
            s"\n\n${ex.toString}")
      }




    })
  }

  def clickOnEandO(): Unit = {
    val allEAndORows = dom.document.getElementsByClassName("termly-plans-es-and-os-code-and-eando-row")
    val nodeListSize = allEAndORows.length
    var index = 0
    while (index < nodeListSize) {
      val theDiv = allEAndORows(index).asInstanceOf[HTMLDivElement]

      theDiv.addEventListener("click", (e: dom.Event) => {
        val eAndOCode = theDiv.getAttribute("data-eando-code")
        global.console.log(s"Selected E and O code '$eAndOCode'")
        if (selectedEsAndOs.contains(eAndOCode)) {
          selectedEsAndOs.remove(eAndOCode)
          setButtonDefaults(theDiv)
        } else {
          selectedEsAndOs.add(eAndOCode)
          theDiv.style.backgroundColor = "#016ecd"
          theDiv.style.color = "white"
          theDiv.style.borderRadius = "7px"
        }
      })


      index = index + 1
    }
  }

  def clickOnBenchmark(): Unit = {
    val allBenchmarkRows = dom.document.getElementsByClassName("termly-plans-es-and-os-benchmark")
    val nodeListSize = allBenchmarkRows.length
    var index = 0
    while (index < nodeListSize) {
      val theDiv = allBenchmarkRows(index).asInstanceOf[HTMLDivElement]

      theDiv.addEventListener("click", (e: dom.Event) => {
        val benchmarkValue = theDiv.getAttribute("data-benchmark")
        global.console.log(s"Selected Benchmark '$benchmarkValue'")
        if (selectedBenchmarks.contains(benchmarkValue)) {
          selectedBenchmarks.remove(benchmarkValue)
          setButtonDefaults(theDiv)
        } else {
          selectedBenchmarks.add(benchmarkValue)
          theDiv.style.backgroundColor = "#016ecd"
          theDiv.style.color = "white"
          theDiv.style.borderRadius = "7px"
        }
      })

      index = index + 1
    }
  }


  def mouseoverHighlightEandOsAndBenchmarks(): Unit = {
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

        if ((eAndOCode != null && eAndOCode.nonEmpty && !selectedEsAndOs.contains(eAndOCode)) ||
          (benchmarkValue != null && benchmarkValue.nonEmpty && !selectedBenchmarks.contains(benchmarkValue))) {
          theDiv.style.backgroundColor = "grey"
          theDiv.style.color = "white"
          theDiv.style.borderRadius = "7px"
        }
      })

      theDiv.addEventListener("mouseleave", (e: dom.Event) => {
        val eAndOCode = theDiv.getAttribute("data-eando-code")
        val benchmarkValue = theDiv.getAttribute("data-benchmark")
        if ((eAndOCode != null && eAndOCode.nonEmpty && !selectedEsAndOs.contains(eAndOCode)) ||
          (benchmarkValue != null && benchmarkValue.nonEmpty && !selectedBenchmarks.contains(benchmarkValue))) {
          theDiv.style.backgroundColor = eAndORowBackgroundNormalColor.getOrElse("white")
          theDiv.style.color = eAndORowForegroundNormalColor.getOrElse("grey")
          theDiv.style.borderRadius = eAndORowBorderRadius.getOrElse("0")
        }
      })


      index = index + 1
    }


  }

}
