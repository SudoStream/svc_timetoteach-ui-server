package timetoteach.planning

import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement}

import scala.scalajs.js.Dynamic.global

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
  }

  def saveButton(): Unit = {
    val saveButton = dom.document.getElementById("save-termly-groups-button").asInstanceOf[HTMLButtonElement]
    saveButton.addEventListener("click", (e: dom.Event) => {
      global.console.log("Selected:\n" +
        s"E&O Codes: ${selectedEsAndOs.toString()}\n" +
        s"Benchmarks: ${selectedBenchmarks.mkString("\n")}"
      )
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
          theDiv.style.backgroundColor = eAndORowBackgroundNormalColor.getOrElse("white")
          theDiv.style.color = eAndORowForegroundNormalColor.getOrElse("grey")
          theDiv.style.borderRadius = eAndORowBorderRadius.getOrElse("0")
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
          theDiv.style.backgroundColor = eAndORowBackgroundNormalColor.getOrElse("white")
          theDiv.style.color = eAndORowForegroundNormalColor.getOrElse("grey")
          theDiv.style.borderRadius = eAndORowBorderRadius.getOrElse("0")
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
