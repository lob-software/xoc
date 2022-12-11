package io.xoc

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
    val isBid = Input(Bool())
    val price = Input(UInt(64.W))
    val size = Input(UInt(64.W))

    val bidPriceOut = Output(UInt(64.W))
    val bidSizeOut = Output(UInt(64.W))

    val askPriceOut = Output(UInt(64.W))
    val askSizeOut = Output(UInt(64.W))
  })

  val currentBidPrice = RegInit(UInt(64.W), 0.U)
  val currentBidSize = RegInit(UInt(64.W), 0.U)

  val currentAskPrice = RegInit(UInt(64.W), Long.MaxValue.U)
  val currentAskSize = RegInit(UInt(64.W), 0.U)

  val incomingBidBetter = io.isBid && io.price > currentBidPrice
  val incomingBidPriceTheSameAndSizeBigger = io.isBid && io.price === currentBidPrice && io.size > currentBidSize
  io.bidPriceOut := Mux(incomingBidBetter, io.price, currentBidPrice)
  io.bidSizeOut := Mux(incomingBidBetter, io.size, Mux(incomingBidPriceTheSameAndSizeBigger, io.size, currentBidSize))

  val incomingAskBetter = !io.isBid && io.price < currentAskPrice
  val incomingAskPriceTheSameAndSizeBigger = !io.isBid && io.price === currentAskPrice && io.size > currentAskSize
  io.askPriceOut := Mux(incomingAskBetter, io.price, currentAskPrice)
  io.askSizeOut := Mux(incomingAskBetter, io.size, Mux(incomingAskPriceTheSameAndSizeBigger, io.size, currentAskSize))

  currentBidPrice := io.bidPriceOut
  currentBidSize := io.bidSizeOut
  currentAskPrice := io.askPriceOut
  currentAskSize := io.askSizeOut

  val priceMatch = currentBidPrice >= currentAskPrice

  when (priceMatch) {
    // match
    when (io.isBid) {
      // aggressive bid
      io.bidPriceOut := 0.U
      io.bidSizeOut := 0.U
      io.askSizeOut := currentAskSize - io.size
    }.otherwise {
      // aggressive ask
      io.askPriceOut := Long.MaxValue.U
      io.askSizeOut := 0.U
      io.bidSizeOut := currentBidSize - io.size
    }
  }
}

object OrderBook extends App {
  println(getVerilogString(new OrderBook()))
}
