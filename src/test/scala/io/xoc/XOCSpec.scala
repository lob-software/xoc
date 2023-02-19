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

  private def bid(xoc: XOC, price: Int, size: Int): Unit = {
    rxByte(xoc, 0.U)
    rxByte(xoc, price.U)
    rxByte(xoc, size.U)
  }

  private def ask(xoc: XOC, price: Int, size: Int): Unit = {
    rxByte(xoc, 1.U)
    rxByte(xoc, price.U)
    rxByte(xoc, size.U)
  }

  private def assertOrderBookDataTransmitted(xoc: XOC, bidPrice: Int, bidSize: Int, askPrice: Int, askSize: Int): Unit = {
    // bid side
    assertByteTransmitted(xoc, bidPrice.U)
    assertByteTransmitted(xoc, bidSize.U)

    // ask side
    assertByteTransmitted(xoc, askPrice.U)
    assertByteTransmitted(xoc, askSize.U)
  }

  "XOC" should "rest orders and emit data" in {
    test(new XOC(CLKS_PER_BIT)).withAnnotations(Seq(WriteVcdAnnotation)) { xoc =>
      xoc.clock.setTimeout(0)
      xoc.io.uartRx.poke(1.U) // keep uart line high
      clockSerial(xoc)

      bid(xoc, 131, 141)
      ask(xoc, 166, 222)

      assertOrderBookDataTransmitted(xoc, 131, 141, 166, 222)
    }
  }

  "XOC" should "accept better bid and emit data" in {
    test(new XOC(CLKS_PER_BIT)).withAnnotations(Seq(WriteVcdAnnotation)) { xoc =>
      xoc.clock.setTimeout(0)
      xoc.io.uartRx.poke(1.U) // keep uart line high
      clockSerial(xoc)

      bid(xoc, 100, 120)
      ask(xoc, 200, 220)

      assertOrderBookDataTransmitted(xoc, 100, 120, 200, 220)

      bid(xoc, 101, 111)

      assertOrderBookDataTransmitted(xoc, 101, 111, 200, 220)
    }
  }

  "XOC" should "accept better ask and emit data" in {
    test(new XOC(CLKS_PER_BIT)).withAnnotations(Seq(WriteVcdAnnotation)) { xoc =>
      xoc.clock.setTimeout(0)
      xoc.io.uartRx.poke(1.U) // keep uart line high
      clockSerial(xoc)

      bid(xoc, 100, 120)
      ask(xoc, 200, 220)

      assertOrderBookDataTransmitted(xoc, 100, 120, 200, 220)

      ask(xoc, 190, 111)

      assertOrderBookDataTransmitted(xoc, 100, 120, 190, 111)
    }
  }

  "XOC" should "match aggressive bid" in {
    test(new XOC(CLKS_PER_BIT)).withAnnotations(Seq(WriteVcdAnnotation)) { xoc =>
      xoc.clock.setTimeout(0)
      xoc.io.uartRx.poke(1.U) // keep uart line high
      clockSerial(xoc)

      bid(xoc, 100, 120)
      ask(xoc, 200, 220)

      assertOrderBookDataTransmitted(xoc, 100, 120, 200, 220)

      bid(xoc, 200, 20)

      assertOrderBookDataTransmitted(xoc, 100, 120, 200, 200)
    }
  }

  "XOC" should "match aggressive ask" in {
    test(new XOC(CLKS_PER_BIT)).withAnnotations(Seq(WriteVcdAnnotation)) { xoc =>
      xoc.clock.setTimeout(0)
      xoc.io.uartRx.poke(1.U) // keep uart line high
      clockSerial(xoc)

      bid(xoc, 100, 120)
      ask(xoc, 200, 220)

      assertOrderBookDataTransmitted(xoc, 100, 120, 200, 220)

      ask(xoc, 100, 20)

      assertOrderBookDataTransmitted(xoc, 100, 100, 200, 220)
    }
  }
}
