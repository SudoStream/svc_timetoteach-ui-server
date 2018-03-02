package com.builtamont

import com.google.inject.{AbstractModule, Provides}
import net.codingwell.scalaguice.ScalaModule
import _root_.play.api.Environment
import com.builtamont.play.pdf.PdfGenerator
import controllers.routes

class ApplicationModule extends AbstractModule with ScalaModule
{

  /** Module configuration + binding */
  def configure(): Unit =
  {}

  /**
    * Provides PDF generator implementation.
    *
    * @param env The current Play app Environment context.
    * @return PDF generator implementation.
    */
  @Provides
  def providePdfGenerator(env: Environment): PdfGenerator =
  {
    val pdfGen = new PdfGenerator(env)
//    pdfGen.loadLocalFonts(Seq("./public/fonts/simplifica.ttf"))
    pdfGen.loadLocalFonts(Seq("/home/andy/projects/timeToTeach/svc_timetoteach-ui-server/server/target/web/public/test/public/fonts/simplifica.ttf"))
//    pdfGen.loadLocalFonts(Seq(routes.Assets.versioned("fonts/simplifica.ttf").absoluteURL()))
    pdfGen
  }

}
