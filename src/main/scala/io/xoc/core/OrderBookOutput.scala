package io.xoc.core

import chisel3._
import chisel3.util.{DecoupledIO, Enum, is, switch}


class OrderBookOutputBundle extends Bundle {
  val bidPrice = Output(UInt(8.W))
  val bidSize = Output(UInt(8.W))
  val askPrice = Output(UInt(8.W))
  val askSize = Output(UInt(8.W))
}

class OrderBookOutput extends DecoupledIO(new OrderBookOutputBundle) {

}

class OrderBookOutputBuffer extends Module {
  val io = IO(new Bundle() {
    val output = Flipped(new OrderBookOutput)
    val txData = Output(UInt(8.W))
    val txDataValid = Output(Bool())
    val txActive = Input(Bool())
  })

  val orderBook = io.output.bits
  val bidPrice :: bidSize :: askPrice :: askSize :: Nil = Enum(4)
  val expectByte = RegInit(bidPrice)

  val validReg = RegInit(false.B)
  val dataReg = RegInit(0.U(8.W))

  io.txDataValid := validReg
  io.txData := dataReg

  io.output.ready := true.B

  when(io.output.valid && !io.txActive) {

    validReg := true.B

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
        expectByte := bidPrice
        validReg := false.B
      }
    }
  }
}

object OrderBookOutputBuffer extends App {
  emitVerilog(new OrderBookOutputBuffer(), Array(
    "--target-dir", "generated",
    "--emission-options=disableMemRandomization,disableRegisterRandomization"
  ))
}
