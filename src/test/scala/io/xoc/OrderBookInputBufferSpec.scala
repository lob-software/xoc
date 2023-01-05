package io.xoc

import chisel3._
import chiseltest._
import io.xoc.core.{OrderBookInput, OrderBookInputBuffer}
import org.scalatest.flatspec.AnyFlatSpec


class OrderBookInputBufferSpec extends AnyFlatSpec with ChiselScalatestTester {

  "OrderBookInputBuffer" should "buffer data" in {
    test(new OrderBookInput()) { obi =>
      obi.io.rxDataValid.poke(true)

      val side = "b00000000".U
      obi.io.rxData.poke(side)
      obi.clock.step()
      obi.io.input.valid.expect(false.B)

      val price = 100.U
      obi.io.rxData.poke(price)
      obi.clock.step()
      obi.io.input.valid.expect(false.B)

      val size = 101.U
      obi.io.rxData.poke(size)
      obi.clock.step()
      obi.io.input.valid.expect(true.B)

      obi.io.input.bits.isBid.expect(true.B)
      obi.io.input.bits.price.expect(price)
      obi.io.input.bits.size.expect(size)
    }
  }
}
