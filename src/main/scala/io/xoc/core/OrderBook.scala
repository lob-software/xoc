package io.xoc.core

import chisel3._
import chisel3.util._


class OrderBook extends Module {
  val io = IO(new Bundle {
    val input = Flipped(DecoupledIO(new OrderBookInputBundle))
    val output = DecoupledIO(new OrderBookOutputBundle)
    val validSeq = Output(UInt(8.W))
  })

  io.input.ready := true.B

  val input = io.input.bits
  val output = io.output.bits

  val currentBidPrice = RegInit(0.U(8.W))
  val currentBidSize = RegInit(0.U(8.W))

  val currentAskPrice = RegInit(0.U(8.W)) // TODO: find out why emitted verilog does not respect init value
  val currentAskSize = RegInit(0.U(8.W))

  val validSeq = Counter(Byte.MaxValue)
  io.validSeq := validSeq.value

  val inputValidDelayed = RegNext(false.B)
  inputValidDelayed := io.input.valid
  val orderBookDataValid = currentBidPrice =/= 0.U & currentBidSize =/= 0.U & currentAskPrice =/= 0.U & currentAskSize =/= 0.U

  when(inputValidDelayed && orderBookDataValid) {
    validSeq.inc()
  }

  // TODO: orderBookDataValid - DRY
  io.output.valid := true.B

  when(io.input.valid) {
    val incomingBidBetter = input.isBid && input.price > currentBidPrice
    val incomingBidPriceTheSameAndSizeBigger = input.isBid && input.price === currentBidPrice && input.size > currentBidSize
    currentBidPrice := Mux(incomingBidBetter, input.price, currentBidPrice)
    currentBidSize := Mux(incomingBidBetter, input.size, Mux(incomingBidPriceTheSameAndSizeBigger, input.size, currentBidSize))

    val incomingAskBetter = !input.isBid && (input.price < currentAskPrice || currentAskPrice === 0.U)
    val incomingAskPriceTheSameAndSizeBigger = !input.isBid && input.price === currentAskPrice && input.size > currentAskSize
    currentAskPrice := Mux(incomingAskBetter, input.price, currentAskPrice)
    currentAskSize := Mux(incomingAskBetter, input.size, Mux(incomingAskPriceTheSameAndSizeBigger, input.size, currentAskSize))
  }

  output.bidPrice := currentBidPrice
  output.bidSize := currentBidSize
  output.askPrice := currentAskPrice
  output.askSize := currentAskSize

  val priceMatch = (currentBidPrice >= currentAskPrice) && (currentBidPrice =/= 0.U && currentAskPrice =/= 0.U)

  when(priceMatch) {
    // match
    when(input.isBid) {
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
