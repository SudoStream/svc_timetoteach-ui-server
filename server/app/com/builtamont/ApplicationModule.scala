package com.builtamont

import com.google.inject.{AbstractModule, Provides}
import net.codingwell.scalaguice.ScalaModule

import _root_.play.api.Environment

import com.builtamont.play.pdf.PdfGenerator

class ApplicationModule extends AbstractModule with ScalaModule {

  /** Module configuration + binding */
  def configure(): Unit = {}

  /**
   * Provides PDF generator implementation.
   *
   * @param env The current Play app Environment context.
   * @return PDF generator implementation.
   */
  @Provides
  def providePdfGenerator(env: Environment): PdfGenerator = {
    val pdfGen = new PdfGenerator(env)
//    pdfGen.loadLocalFonts(Seq("fonts/opensans-regular.ttf"))
//    pdfGen.loadLocalFonts(Seq("fonts/simplifica.ttf"))
    pdfGen
  }

}
