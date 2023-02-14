package io.xoc

import chisel3._
import chiseltest._
import io.xoc.core.OrderBookInputBuffer
import org.scalatest.flatspec.AnyFlatSpec


class XOCSpec extends AnyFlatSpec with ChiselScalatestTester {
  val CLKS_PER_BIT = 10

  private def clockSerial(xoc: XOC): Unit = {
    xoc.clock.step(CLKS_PER_BIT)
  }

  private def rxByte(xoc: XOC, byte: UInt): Unit = {
    xoc.io.uartRx.poke(0.U) // start bit
    clockSerial(xoc)

    byte.asBools.padTo(8, false.B).foreach(b => {
      xoc.io.uartRx.poke(b)
      clockSerial(xoc)
    })

    xoc.io.uartRx.poke(1.U) // stop bit
    clockSerial(xoc)
  }

  private def assertByteTransmitted(xoc: XOC, byte: UInt): Unit = {
    xoc.clock.step()
    xoc.clock.step()

    // start bit
    xoc.io.uartTx.expect(false.B)

    byte.asBools.padTo(8, false.B).foreach(b => {
      // data
      clockSerial(xoc)
//      println(xoc.io.uartTx.peek() + " | " + b)
      xoc.io.uartTx.expect(b)
    })

    // last < 7 check cycle in state == data
    clockSerial(xoc)

    // stop bit
    xoc.io.uartTx.expect(true.B)
    clockSerial(xoc)
  }

  "XOC" should "rest orders and emit data" in {
    test(new XOC(CLKS_PER_BIT)).withAnnotations(Seq(WriteVcdAnnotation)) { xoc =>
      xoc.clock.setTimeout(0)
      xoc.io.uartRx.poke(1.U) // keep uart line high
      clockSerial(xoc)

      // bid
      rxByte(xoc, 0.U)
      rxByte(xoc, 131.U)
      rxByte(xoc, 141.U)

      // ask
      rxByte(xoc, 1.U)
      rxByte(xoc, 166.U)
      rxByte(xoc, 222.U)

      // bid side
      assertByteTransmitted(xoc, 131.U)
      assertByteTransmitted(xoc, 141.U)

      // ask side
      assertByteTransmitted(xoc, 166.U)
      assertByteTransmitted(xoc, 222.U)
    }
  }

  "XOC" should "rest orders and emit data in succession" in {
    test(new XOC(CLKS_PER_BIT)).withAnnotations(Seq(WriteVcdAnnotation)) { xoc =>
      xoc.clock.setTimeout(0)
      xoc.io.uartRx.poke(1.U) // keep uart line high
      clockSerial(xoc)

      // bid
      rxByte(xoc, 0.U)
      rxByte(xoc, 100.U)
      rxByte(xoc, 120.U)

      // ask
      rxByte(xoc, 1.U)
      rxByte(xoc, 200.U)
      rxByte(xoc, 220.U)

      // bid side
      assertByteTransmitted(xoc, 100.U)
      assertByteTransmitted(xoc, 120.U)

      // ask side
      assertByteTransmitted(xoc, 200.U)
      assertByteTransmitted(xoc, 220.U)

      // bid
      rxByte(xoc, 0.U)
      rxByte(xoc, 101.U)
      rxByte(xoc, 111.U)

      // bid side
      assertByteTransmitted(xoc, 101.U)
      assertByteTransmitted(xoc, 111.U)

      // ask side
      assertByteTransmitted(xoc, 200.U)
      assertByteTransmitted(xoc, 220.U)
    }
  }
}
