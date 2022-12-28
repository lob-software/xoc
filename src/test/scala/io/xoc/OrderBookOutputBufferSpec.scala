package io.xoc

import chisel3._
import chiseltest._
import io.xoc.core.OrderBookOutputBuffer
import org.scalatest.flatspec.AnyFlatSpec


class OrderBookOutputBufferSpec extends AnyFlatSpec with ChiselScalatestTester {

  "OrderBookOutputBuffer" should "buffer data" in {
    test(new OrderBookOutputBuffer()) { obo =>
      obo.io.output.bits.bidPrice.poke(100.U)
      obo.io.output.bits.bidSize.poke(100.U)
      obo.io.output.bits.askPrice.poke(100.U)
      obo.io.output.bits.askSize.poke(100.U)


    }
  }
}
