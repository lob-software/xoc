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
    val input = Input(new OrderBookInput())

    val bidPriceOut = Output(UInt(64.W))
    val bidSizeOut = Output(UInt(64.W))

    val askPriceOut = Output(UInt(64.W))
    val askSizeOut = Output(UInt(64.W))
  })

  val currentBidPrice = RegInit(UInt(64.W), 0.U)
  val currentBidSize = RegInit(UInt(64.W), 0.U)

  val currentAskPrice = RegInit(UInt(64.W), Long.MaxValue.U)
  val currentAskSize = RegInit(UInt(64.W), 0.U)

  val incomingBidBetter = io.input.isBid && io.input.price > currentBidPrice
  val incomingBidPriceTheSameAndSizeBigger = io.input.isBid && io.input.price === currentBidPrice && io.input.size > currentBidSize
  io.bidPriceOut := Mux(incomingBidBetter, io.input.price, currentBidPrice)
  io.bidSizeOut := Mux(incomingBidBetter, io.input.size, Mux(incomingBidPriceTheSameAndSizeBigger, io.input.size, currentBidSize))

  val incomingAskBetter = !io.input.isBid && io.input.price < currentAskPrice
  val incomingAskPriceTheSameAndSizeBigger = !io.input.isBid && io.input.price === currentAskPrice && io.input.size > currentAskSize
  io.askPriceOut := Mux(incomingAskBetter, io.input.price, currentAskPrice)
  io.askSizeOut := Mux(incomingAskBetter, io.input.size, Mux(incomingAskPriceTheSameAndSizeBigger, io.input.size, currentAskSize))

  currentBidPrice := io.bidPriceOut
  currentBidSize := io.bidSizeOut
  currentAskPrice := io.askPriceOut
  currentAskSize := io.askSizeOut

  val priceMatch = currentBidPrice >= currentAskPrice

  when (priceMatch) {
    // match
    when (io.input.isBid) {
      // aggressive bid
      io.bidPriceOut := 0.U
      io.bidSizeOut := 0.U
      io.askSizeOut := currentAskSize - io.input.size
    }.otherwise {
      // aggressive ask
      io.askPriceOut := Long.MaxValue.U
      io.askSizeOut := 0.U
      io.bidSizeOut := currentBidSize - io.input.size
    }
  }
}

object OrderBook extends App {
  println(getVerilogString(new OrderBook()))
}