package shared.util

import scala.annotation.tailrec

object PlanningHelper
{
  val JAWN_UNFRIENDLY_CHARACTERS = Map(
    "=" -> "[[EQUALS]]",
    "%" -> "[[PERCENT]]",
    ";" -> "[[SEMICOLON]]",
    "&" -> "[[AMPERSAND]]",
    "!" -> "[[EXCLAIMATION]]",
    "\"" -> "[[DOUBLE_QUOTES]]",
    "\'" -> "[[SINGLE_QUOTE]]",
    ":" -> "[[COLON]]",
    "$" -> "[[DOLLAR]]",
    "^" -> "[[POWER]]",
    "*" -> "[[STAR]]",
    "-" -> "[[MINUS]]",
    "+" -> "[[PLUS]]",
    "(" -> "[[OPEN_PARENS]]",
    ")" -> "[[CLOSE_PARENS]]",
    "|" -> "[[BAR]]",
    "\\" -> "[[BACKSLASH]]",
    "," -> "[[COMMA]]",
    "." -> "[[FULLSTOP]]"
  )

  val JAWN_UNFRIENDLY_CHARACTERS_DECODE = Map(
    "[[EQUALS]]" -> "=",
    "[[PERCENT]]" -> "%",
    "[[SEMICOLON]]" -> ";",
    "[[AMPERSAND]]" -> "\u0026",
    "[[EXCLAIMATION]]" -> "!",
    "[[DOUBLE_QUOTES]]" -> "\"",
    "[[SINGLE_QUOTE]]" -> "\'",
    "[[COLON]]" -> ":",
    "[[DOLLAR]]" -> "$",
    "[[POWER]]" -> "^",
    "[[STAR]]" -> "*",
    "[[MINUS]]" -> "-",
    "[[PLUS]]" -> "+",
    "[[OPEN_PARENS]]" -> "(",
    "[[CLOSE_PARENS]]" -> ")",
    "[[BAR]]" -> "|",
    "[[BACKSLASH]]" -> "\\",
    "[[COMMA]]" -> ",",
    "[[FULLSTOP]]" -> "."
  )

  def encodeAnyJawnNonFriendlyCharacters(stringToEncode: String): String =
  {
    {
      for {
        character <- stringToEncode
        characterEncoded = encodeCharacterIfNecessary(character)
      } yield characterEncoded
    }.mkString
  }

  private def encodeCharacterIfNecessary(character: Char): String =
  {
    if (JAWN_UNFRIENDLY_CHARACTERS.isDefinedAt(character.toString)) {
      JAWN_UNFRIENDLY_CHARACTERS(character.toString)
    } else {
      character.toString
    }
  }

  def decodeAnyNonFriendlyCharacters(stringToDecode: String): String =
  {
    @tailrec
    def decodeAnyNonFriendlyCharactersLoop(
                                            remainingCodesToDecode: List[String],
                                            currentString: String
                                          ): String =
    {
      if (remainingCodesToDecode.isEmpty) currentString
      else {
        val nextCode = remainingCodesToDecode.head
        val nextCodeDecodedString = if (JAWN_UNFRIENDLY_CHARACTERS_DECODE.isDefinedAt(nextCode)) {
          currentString.replace(nextCode, JAWN_UNFRIENDLY_CHARACTERS_DECODE(nextCode))
        } else {
          currentString
        }

        decodeAnyNonFriendlyCharactersLoop(remainingCodesToDecode.tail, nextCodeDecodedString)
      }
    }

    decodeAnyNonFriendlyCharactersLoop(JAWN_UNFRIENDLY_CHARACTERS_DECODE.keys.toList, stringToDecode)
  }
}
