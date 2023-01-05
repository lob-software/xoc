package io.xoc

import chisel3._
import chiseltest._
import io.xoc.core.{OrderBookOutput, OrderBookOutputBuffer}
import org.scalatest.flatspec.AnyFlatSpec


class OrderBookOutputBufferSpec extends AnyFlatSpec with ChiselScalatestTester {

  "OrderBookOutputBuffer" should "buffer data" in {
    test(new OrderBookOutput()) { obo =>
      assertOutput(obo, 111, 121, 144, 211)
      assertOutput(obo, 222, 111, 11, 43)
    }
  }

  private def assertOutput(obo: OrderBookOutput, bidPrice: Int, bidSize: Int, askPrice: Int, askSize: Int): Unit = {
    obo.io.output.valid.poke(true)
    obo.io.output.bits.bidPrice.poke(bidPrice.U)
    obo.io.output.bits.bidSize.poke(bidSize.U)
    obo.io.output.bits.askPrice.poke(askPrice.U)
    obo.io.output.bits.askSize.poke(askSize.U)

    obo.clock.step()
    obo.io.txData.expect(bidPrice)

    obo.clock.step()
    obo.io.txData.expect(bidSize)

    obo.clock.step()
    obo.io.txData.expect(askPrice)

    obo.clock.step()
    obo.io.txData.expect(askSize)
  }
}
