package io.xoc

import chiseltest._
import chisel3._
import io.xoc.core.OrderBook
import org.scalatest.flatspec.AnyFlatSpec


class OrderBookSpec extends AnyFlatSpec with ChiselScalatestTester {

  def bid(ob: OrderBook, price: Int, size: Int): Unit = {
    val input = ob.io.input.bits

    input.isBid.poke(true)
    input.price.poke(price)
    input.size.poke(size)
    ob.clock.step(1)
  }

  def ask(ob: OrderBook, price: Int, size: Int): Unit = {
    val input = ob.io.input.bits

    input.isBid.poke(false)
    input.price.poke(price)
    input.size.poke(size)
    ob.clock.step(1)
  }

  "OrderBook" should "initialize prices and sizes to 0" in {
    test(new OrderBook()) { ob =>
      ob.io.output.bits.bidPrice.expect(0)
      ob.io.output.bits.bidSize.expect(0)

      ob.io.output.bits.askPrice.expect("xff".U) // TODO: find a better way
      ob.io.output.bits.askSize.expect(0)
    }
  }
  "OrderBook" should "accept orders" in {
    test(new OrderBook()) { ob =>
      bid(ob, 100, 101)

      ob.io.output.bits.bidPrice.expect(100)
      ob.io.output.bits.bidSize.expect(101)

      ask(ob, 200, 201)

      ob.io.output.bits.askPrice.expect(200)
      ob.io.output.bits.askSize.expect(201)
    }
  }
  "OrderBook" should "prefer highest bid" in {
    test(new OrderBook()) { ob =>
      bid(ob, 100, 101)

      ob.io.output.bits.bidPrice.expect(100)
      ob.io.output.bits.bidSize.expect(101)

      bid(ob, 200, 201)

      ob.io.output.bits.bidPrice.expect(200)
      ob.io.output.bits.bidSize.expect(201)

      bid(ob, 100, 101)

      // highest bid remains
      ob.io.output.bits.bidPrice.expect(200)
      ob.io.output.bits.bidSize.expect(201)
    }
  }
  "OrderBook" should "prefer lowest ask" in {
    test(new OrderBook()) { ob =>
      ask(ob, 100, 101)

      ob.io.output.bits.askPrice.expect(100)
      ob.io.output.bits.askSize.expect(101)

      ask(ob, 90, 201)

      ob.io.output.bits.askPrice.expect(90)
      ob.io.output.bits.askSize.expect(201)

      ask(ob, 100, 101)

      // lowest ask remains
      ob.io.output.bits.askPrice.expect(90)
      ob.io.output.bits.askSize.expect(201)
    }
  }
  "OrderBook" should "prefer larger bid if price the same" in {
    test(new OrderBook()) { ob =>
      bid(ob, 100, 101)

      ob.io.output.bits.bidPrice.expect(100)
      ob.io.output.bits.bidSize.expect(101)

      bid(ob, 100, 201)

      ob.io.output.bits.bidPrice.expect(100)
      ob.io.output.bits.bidSize.expect(201)

      bid(ob, 100, 50)

      // larger bid remains
      ob.io.output.bits.bidPrice.expect(100)
      ob.io.output.bits.bidSize.expect(201)
    }
  }
  "OrderBook" should "prefer larger ask if price the same" in {
    test(new OrderBook()) { ob =>
      ask(ob, 100, 101)

      ob.io.output.bits.askPrice.expect(100)
      ob.io.output.bits.askSize.expect(101)

      ask(ob, 100, 201)

      ob.io.output.bits.askPrice.expect(100)
      ob.io.output.bits.askSize.expect(201)

      ask(ob, 100, 50)

      // larger ask remains
      ob.io.output.bits.askPrice.expect(100)
      ob.io.output.bits.askSize.expect(201)
    }
  }

  "OrderBook" should "match incoming bid with resting ask when prices equal and incoming size smaller" in {
    test(new OrderBook()) { ob =>
      ask(ob, 100, 101)

      bid(ob, 100, 10)

      ob.io.output.bits.bidPrice.expect(0)
      ob.io.output.bits.bidSize.expect(0)
      ob.io.output.bits.askPrice.expect(100)
      ob.io.output.bits.askSize.expect(91)
    }
  }

  "OrderBook" should "match incoming ask with resting bid when prices equal and incoming size smaller" in {
    test(new OrderBook()) { ob =>
      bid(ob, 100, 100)

      ask(ob, 100, 10)

      ob.io.output.bits.askPrice.expect("xff".U)
      ob.io.output.bits.askSize.expect(0)
      ob.io.output.bits.bidPrice.expect(100)
      ob.io.output.bits.bidSize.expect(90)
    }
  }
}
