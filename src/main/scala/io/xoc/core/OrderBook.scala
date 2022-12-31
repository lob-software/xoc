package io.xoc.core

import Chisel.DecoupledIO
import chisel3._


/**
 * Open questions:
 * 1. How deep and how wide should the order book be?
 *   - Can sizing be dynamic?
 * 2. What's the protocol for commands:
 *  Place order:
 *    - in:
 *    - out:
 *  Cancel order:
 *    - in:
 *    - out:
 * 3. How to make sure that the same command is only consumed by the module once?
 *    - ready and valid flags?
 * 4. Which data structures should be used for:
 *    - Orders
 *    - Price levels. Sorting of price levels and also storing of orders pertaining to price levels
 * 5. How will the data be communicated to the module?
 *    - UART?
 *    - Wishbone?
 *    - Something custom?
 */


/**
 * First take.
 *
 * Order book with best bid and best offer, in price and in size.
 *
 */

class OrderBook extends Module {
  val io = IO(new Bundle {
    val input = Flipped(new OrderBookInput)
    val output = new OrderBookOutput
  })

  io.input.ready := true.B

  val input = io.input.bits
  val output = io.output.bits

  val currentBidPrice = RegInit(UInt(8.W), 0.U)
  val currentBidSize = RegInit(UInt(8.W), 0.U)

  val currentAskPrice = RegInit(UInt(8.W), 255.U)
  val currentAskSize = RegInit(UInt(8.W), 0.U)

  val orderBookDataValid = currentBidPrice =/= 0.U & currentBidSize =/= 0.U & currentAskPrice =/= 255.U & currentAskSize =/= 0.U
  io.output.valid := orderBookDataValid

  when(io.input.valid) {
    val incomingBidBetter = input.isBid && input.price > currentBidPrice
    val incomingBidPriceTheSameAndSizeBigger = input.isBid && input.price === currentBidPrice && input.size > currentBidSize
    currentBidPrice := Mux(incomingBidBetter, input.price, currentBidPrice)
    currentBidSize := Mux(incomingBidBetter, input.size, Mux(incomingBidPriceTheSameAndSizeBigger, input.size, currentBidSize))

    val incomingAskBetter = !input.isBid && input.price < currentAskPrice
    val incomingAskPriceTheSameAndSizeBigger = !input.isBid && input.price === currentAskPrice && input.size > currentAskSize
    currentAskPrice := Mux(incomingAskBetter, input.price, currentAskPrice)
    currentAskSize := Mux(incomingAskBetter, input.size, Mux(incomingAskPriceTheSameAndSizeBigger, input.size, currentAskSize))
  }

  output.bidPrice := currentBidPrice
  output.bidSize := currentBidSize
  output.askPrice := currentAskPrice
  output.askSize := currentAskSize

  val priceMatch = currentBidPrice >= currentAskPrice

  when (priceMatch) {
    // match
    when (input.isBid) {
      // aggressive bid
      output.bidPrice := 0.U
      output.bidSize := 0.U
      output.askSize := currentAskSize - input.size
    }.otherwise {
      // aggressive ask
      output.askPrice := Long.MaxValue.U
      output.askSize := 0.U
      output.bidSize := currentBidSize - input.size
    }
  }
}

object OrderBook extends App {
  println(getVerilogString(new OrderBook()))
}
