package controllers.pdf

import com.builtamont.play.pdf.PdfGenerator
import javax.inject.{Inject, Singleton}

@Singleton
case class PdfGeneratorWrapper @Inject()(pdfGenerator: PdfGenerator)
