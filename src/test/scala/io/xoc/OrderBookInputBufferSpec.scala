package io.xoc

import chisel3._
import chiseltest._
import io.xoc.core.OrderBookInputBuffer
import org.scalatest.flatspec.AnyFlatSpec


class OrderBookInputBufferSpec extends AnyFlatSpec with ChiselScalatestTester {

  "OrderBookInputBuffer" should "parse data" in {
    test(new OrderBookInputBuffer()) { obi =>
      obi.io.rxDataValid.poke(true)
      obi.io.rxData.poke(0.U)

      obi.clock.step()
      obi.io.input.bits.isBid.expect(true.B)

      val price = 100.U

      // TODO: LSB of a long should come first?
      val priceAsUInts = price.asBools
//        .reverse
//        .padTo(64, false.B)
//        .reverse
//        .grouped(8)
        //        .map(bits => {
        //          var result = "b"
        //          bits.map(b => if (b.litToBoolean) "1" else "0").foreach(bit => result += bit)
        //          result
        //        })
        .toSeq

      println(priceAsUInts)
    }
  }
}
