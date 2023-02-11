package io.xoc.core

import chisel3._
import chisel3.util.{Counter, DecoupledIO, Enum, is, switch}


class OrderBookOutputBundle extends Bundle {
  val bidPrice = Output(UInt(8.W))
  val bidSize = Output(UInt(8.W))
  val askPrice = Output(UInt(8.W))
  val askSize = Output(UInt(8.W))
}

class OrderBookOutput extends Module {
  val io = IO(new Bundle() {
    val output = Flipped(DecoupledIO(new OrderBookOutputBundle))
    val validSeq = Input(UInt(8.W))
    val uart = new UartIO
  })

  val orderBook = io.output.bits
  val bidPrice :: bidSize :: askPrice :: askSize :: none :: Nil = Enum(5)
  val expectByte = RegInit(bidPrice)
  val dataReg = RegInit(0.U(8.W))
  val seqCounter = Counter(Byte.MaxValue)
  val seqValue = RegNext(seqCounter.value)

  io.output.ready := true.B

  val orderBookDataValid = orderBook.bidPrice =/= 0.U & orderBook.bidSize =/= 0.U & orderBook.askPrice =/= 0.U & orderBook.askSize =/= 0.U
  val seqValid = seqValue < io.validSeq

  val valid = orderBookDataValid && seqValid
  io.uart.valid := valid && dataReg =/= 0.U // TODO: second part of the condition prevents UartTX from being engaged.
  // Otherwise we wait all the clocks until 0 byte is transmitted.
  io.uart.bits := dataReg

  when(io.uart.ready && valid) {
    switch(expectByte) {
      is(bidPrice) {
        dataReg := orderBook.bidPrice
        expectByte := bidSize
      }
      is(bidSize) {
        dataReg := orderBook.bidSize
        expectByte := askPrice
      }
      is(askPrice) {
        dataReg := orderBook.askPrice
        expectByte := askSize
      }
      is(askSize) {
        dataReg := orderBook.askSize
        expectByte := none
      }
      is (none) {
        // we don't know if this value got consumed downstream. incrementing early can make data invalid and thus never be read
        // can we increment only when it is guaranteed that the value is consumed downstream?
        seqCounter.inc()
        expectByte := bidPrice
      }
    }
  }
}

object OrderBookOutputBuffer extends App {
  emitVerilog(new OrderBookOutput(), Array(
    "--target-dir", "generated",
    "--emission-options=disableMemRandomization,disableRegisterRandomization"
  ))
}
