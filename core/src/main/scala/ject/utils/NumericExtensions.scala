package ject.utils

import java.text.DecimalFormat

object NumericExtensions {

  implicit class IntExtension(val self: Int) extends AnyVal {

    def withCommas: String =
      s"%,d".format(self)
  }

  implicit class LongExtension(val self: Long) extends AnyVal {

    def withCommas: String =
      s"%,d".format(self)
  }

  implicit class DoubleExtension(val self: Double) extends AnyVal {

    def digits(digits: Int): String =
      if (digits <= 0)
        self.toInt.toString
      else
        new DecimalFormat(s"0.${"#" * digits}").format(self)

    def digits2(digits: Int): String =
      s"%,.${digits}f".format(self)

    def percent(digits: Int): String =
      if (self.isNaN)
        "n/a"
      else
        s"${(self * 100).digits(digits)}%"
  }

}
