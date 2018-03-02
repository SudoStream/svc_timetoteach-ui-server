package utils

object StringUtils
{
  def noWhiteSpaceAtAll(str: String): String =
  {
    str.trim.replace(" ","")
  }
}
