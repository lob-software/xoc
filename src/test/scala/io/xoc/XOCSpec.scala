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
    xoc.io.rx.poke(0.U) // start bit
    clockSerial(xoc)

    byte.asBools.padTo(8, false.B).foreach(b => {
      xoc.io.rx.poke(b)
      clockSerial(xoc)
    })

    xoc.io.rx.poke(1.U) // stop bit
    clockSerial(xoc)
  }

  "XOC" should "do its thing" in {
    test(new XOC(CLKS_PER_BIT)).withAnnotations(Seq(WriteVcdAnnotation)) { xoc =>
      xoc.clock.setTimeout(0)
      xoc.io.rx.poke(1.U) // keep uart line high
      clockSerial(xoc)

      // bid
      rxByte(xoc, 0.U)
      rxByte(xoc, 131.U)
      rxByte(xoc, 141.U)

      // ask
      rxByte(xoc, 1.U)
      rxByte(xoc, 166.U)
      rxByte(xoc, 222.U)

      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)


      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)

      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)

      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)
      clockSerial(xoc)


    }
  }
}
