package io.xoc

import chisel3._
import chiseltest._
import io.xoc.uart.{UartEcho, UartTx}
import org.scalatest.flatspec.AnyFlatSpec


class UartEchoSpec extends AnyFlatSpec with ChiselScalatestTester {

  val CLKS_PER_BIT = 100

  private def clockSerial(echo: UartEcho): Unit = {
    echo.clock.step(CLKS_PER_BIT)
  }

  def rxByte(echo: UartEcho, expectedByte: UInt): Unit = {
    echo.io.uartRx.poke(0.U) // start bit
    clockSerial(echo)

    expectedByte.asBools.foreach(b => {
      echo.io.uartRx.poke(b)
      clockSerial(echo)
    })

    echo.io.uartRx.poke(1.U) // stop bit
    clockSerial(echo)
  }

  "UartEcho" should "echo a byte" in {
    test(new UartEcho(CLKS_PER_BIT)) { echo =>
      echo.clock.setTimeout(0)

      val byte = "b10101001".U
      echo.io.uartTx.expect(true)

      rxByte(echo, byte)

      // tx start bit
      echo.io.uartTx.expect(false)

      byte.asBools.foreach(b => {
        clockSerial(echo)
        // tx data
        echo.io.uartTx.expect(b)
      })

      // tx stop bit
      clockSerial(echo)
      echo.io.uartTx.expect(true)
    }
  }
}
