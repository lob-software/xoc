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

  io.output.valid := orderBookDataValid

  when(io.input.valid) {
    when(input.isBid) {
      val priceBetter = input.price > currentBidPrice
      val priceTheSameAndSizeGreater = input.price === currentBidPrice && input.size > currentBidSize
      currentBidPrice := Mux(priceBetter, input.price, currentBidPrice)
      currentBidSize := Mux(priceBetter, input.size, Mux(priceTheSameAndSizeGreater, input.size, currentBidSize))

      when(input.price >= currentAskPrice && currentAskPrice =/= 0.U) {
        currentBidPrice := 0.U
        currentBidSize := 0.U
        currentAskSize := currentAskSize - input.size
      }
    }.otherwise {
      val priceBetter = input.price < currentAskPrice || currentAskPrice === 0.U
      val priceTheSameAndSizeGreater = input.price === currentAskPrice && input.size > currentAskSize
      currentAskPrice := Mux(priceBetter, input.price, currentAskPrice)
      currentAskSize := Mux(priceBetter, input.size, Mux(priceTheSameAndSizeGreater, input.size, currentAskSize))

      when(input.price <= currentBidPrice && currentBidPrice =/= 0.U) {
        currentAskPrice := 0.U
        currentAskSize := 0.U
        currentBidSize := currentBidSize - input.size
      }
    }
  }

  output.bidPrice := currentBidPrice
  output.bidSize := currentBidSize
  output.askPrice := currentAskPrice
  output.askSize := currentAskSize
}

object OrderBook extends App {
  println(getVerilogString(new OrderBook()))
}
