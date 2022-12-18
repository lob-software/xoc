package io.xoc

import chiseltest._
import chisel3._
import io.xoc.uart.UartRx
import org.scalatest.flatspec.AnyFlatSpec


class UartRxSpec extends AnyFlatSpec with ChiselScalatestTester {

  val CLKS_PER_BIT = 100

  private def clockSerial(rx: UartRx): Unit = {
    rx.clock.step(CLKS_PER_BIT)
  }

  "UartRx" should "receive a byte" in {
    test(new UartRx(CLKS_PER_BIT)) { rx =>
      rx.io.uartRx.poke(1.U) // keep uart line high
      clockSerial(rx)

      val expectedBytes = Seq(
        "b00000000".U,
        "b10101001".U,
        "b01101000".U,
        "b10001111".U,
        "b11111111".U,
      )

      expectedBytes.foreach(expectedByte => assertByteReceived(rx, expectedByte))
    }
  }

  private def assertByteReceived(rx: UartRx, expectedByte: UInt): Unit = {
    rx.io.uartRx.poke(0.U) // start bit
    clockSerial(rx)

    expectedByte.asBools.padTo(8, false.B).foreach(b => {
      rx.io.uartRx.poke(b)
      clockSerial(rx)
    })

    rx.io.uartRx.poke(1.U) // stop bit
    clockSerial(rx)

    rx.io.rxDataOut.expect(expectedByte)
  }
}
