package utils

import org.scalatest.FunSpec

class StringUtilsTest extends FunSpec
{
  describe("Given the string '    Andy Boyle  ' noWhiteSpaceAtAll") {
    it("should return the string 'AndyBoyle'") {
      val andyCrushed = StringUtils.noWhiteSpaceAtAll("    Andy Boyle  ")
      assert(andyCrushed === "AndyBoyle")
    }
  }
}
