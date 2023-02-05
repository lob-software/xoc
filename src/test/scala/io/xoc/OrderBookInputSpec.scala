package io.xoc

import chisel3._
import chiseltest._
import io.xoc.core.{OrderBookInput, OrderBookInputBuffer}
import org.scalatest.flatspec.AnyFlatSpec


class OrderBookInputSpec extends AnyFlatSpec with ChiselScalatestTester {

  "OrderBookInput" should "buffer data" in {
    test(new OrderBookInput()) { obi =>
      obi.io.rxDataValid.poke(true)

      val side = "b00000000".U
      obi.io.rxData.poke(side)
      obi.clock.step()

      obi.io.input.valid.expect(false.B)
      assertBits(obi, true.B, 0.U, 0.U)

      val price = 100.U
      obi.io.rxData.poke(price)
      obi.clock.step()

      obi.io.input.valid.expect(false.B)
      assertBits(obi, true.B, price, 0.U)

      val size = 101.U
      obi.io.rxData.poke(size)
      obi.clock.step()

      obi.io.input.valid.expect(true.B)
      assertBits(obi, true.B, price, size)
    }
  }

  private def assertBits(obi: OrderBookInput, isBid: Bool, price: UInt, size: UInt): Unit = {
    obi.io.input.bits.isBid.expect(isBid)
    obi.io.input.bits.price.expect(price)
    obi.io.input.bits.size.expect(size)
  }
}
