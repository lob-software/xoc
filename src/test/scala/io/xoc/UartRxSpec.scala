package io.xoc

import chiseltest._
import chisel3._
import io.xoc.uart.UartRx
import org.scalatest.flatspec.AnyFlatSpec


class UartRxSpec extends AnyFlatSpec with ChiselScalatestTester {

  private def clockSerial(rx: UartRx): Unit = {
    rx.clock.step(100)
  }

  "UartRx" should "receive bytes" in {
    test(new UartRx(100)) { rx =>
      val expectedBytes = Seq("h41".U, "h6A".U, "h74".U, "h6F".U, "h6E".U)
      expectedBytes.foreach(expectedByte => assertByteReceived(rx, expectedByte))
    }
  }

  private def assertByteReceived(rx: UartRx, expectedByte: UInt): Unit = {
    rx.io.uartRx.poke(0.U) // start bit
    clockSerial(rx)

    expectedByte.asBools.foreach(b => {
      rx.io.uartRx.poke(b)
      clockSerial(rx)
    })

    rx.io.uartRx.poke(1.U) // stop bit
    clockSerial(rx)

    rx.io.rxDataOut.expect(expectedByte)

    clockSerial(rx)
    clockSerial(rx)
    clockSerial(rx)
  }
}
