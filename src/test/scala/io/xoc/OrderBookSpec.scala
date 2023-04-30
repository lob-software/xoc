package io.xoc

import chiseltest._
import io.xoc.core.OrderBook
import org.scalatest.flatspec.AnyFlatSpec


class OrderBookSpec extends AnyFlatSpec with ChiselScalatestTester {

  def bid(ob: OrderBook, price: Int, size: Int): Unit = {
    ob.io.input.valid.poke(true)
    val input = ob.io.input.bits

    input.isBid.poke(true)
    input.price.poke(price)
    input.size.poke(size)
    ob.clock.step(1)
  }

  def ask(ob: OrderBook, price: Int, size: Int): Unit = {
    ob.io.input.valid.poke(true)
    val input = ob.io.input.bits

    input.isBid.poke(false)
    input.price.poke(price)
    input.size.poke(size)
    ob.clock.step(1)
  }

  "OrderBook" should "initialize prices and sizes to 0" in {
    test(new OrderBook()) { ob =>
      ob.io.input.valid.poke(true)

      ob.io.output.bits.bidPrice.expect(0)
      ob.io.output.bits.bidSize.expect(0)

      ob.io.output.bits.askPrice.expect(0)
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

      ob.io.output.bits.bidPrice.expect(100)
      ob.io.output.bits.bidSize.expect(90)
      ob.io.output.bits.askPrice.expect(0)
      ob.io.output.bits.askSize.expect(0)
    }
  }

  "OrderBook" should "match incoming bid with resting ask when prices equal and incoming size smaller and leave existing bid" in {
    test(new OrderBook()) { ob =>
      bid(ob, 90, 100)
      ask(ob, 100, 10)

      bid(ob, 100, 5)

      ob.io.output.bits.bidPrice.expect(90)
      ob.io.output.bits.bidSize.expect(100)
      ob.io.output.bits.askPrice.expect(100)
      ob.io.output.bits.askSize.expect(5)
    }
  }

  "OrderBook" should "match incoming ask with resting bid when prices equal and incoming size smaller and leave existing ask" in {
    test(new OrderBook()) { ob =>
      bid(ob, 90, 100)
      ask(ob, 100, 10)

      ask(ob, 90, 5)

      ob.io.output.bits.bidPrice.expect(90)
      ob.io.output.bits.bidSize.expect(95)
      ob.io.output.bits.askPrice.expect(100)
      ob.io.output.bits.askSize.expect(10)
    }
  }

  "OrderBook" should "re-establish bid at lower price then the bid fully consumed" in {
    test(new OrderBook()).withAnnotations(Seq(WriteVcdAnnotation)) { ob =>
      bid(ob, 90, 100)
      ask(ob, 100, 10)

      // consume bid
      ask(ob, 90, 100)

      // re-establish bid at lower price
      bid(ob, 80, 100)

      ob.io.output.bits.bidPrice.expect(80)
      ob.io.output.bits.bidSize.expect(100)
      ob.io.output.bits.askPrice.expect(100)
      ob.io.output.bits.askSize.expect(10)
    }
  }

  "OrderBook" should "re-establish ask at higher price then the ask fully consumed" in {
    test(new OrderBook()).withAnnotations(Seq(WriteVcdAnnotation)) { ob =>
      bid(ob, 90, 100)
      ask(ob, 100, 10)

      // consume ask
      bid(ob, 100, 10)

      // re-establish ask at higher price
      ask(ob, 110, 100)

      ob.io.output.bits.bidPrice.expect(90)
      ob.io.output.bits.bidSize.expect(100)
      ob.io.output.bits.askPrice.expect(110)
      ob.io.output.bits.askSize.expect(100)
    }
  }
}
