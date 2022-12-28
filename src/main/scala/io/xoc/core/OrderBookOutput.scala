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
    val txDataValid = Output(Bool())
    val txData = Output(UInt(8.W))
  })

  val output = io.output.bits
  val bidPrice :: bidSize :: askPrice :: askSize :: Nil = Enum(4)
  val expectByte = RegInit(bidPrice)

  val validReg = RegInit(false.B)

  val bidPriceReg = RegInit(0.U(8.W))
  val bidSizeReg = RegInit(0.U(8.W))
  val askPriceReg = RegInit(0.U(8.W))
  val askSizeReg = RegInit(0.U(8.W))

  io.txDataValid := validReg
  io.txData := 111.U
  io.output.ready := true.B
//  output.bidPrice := bidPriceReg
//  output.bidSize := bidSizeReg
//  output.askPrice := askPriceReg
//  output.askSize := askSizeReg

//  when(io.output.)
}

object OrderBookOutputBuffer extends App {
  emitVerilog(new OrderBookOutputBuffer(), Array(
    "--target-dir", "generated",
    "--emission-options=disableMemRandomization,disableRegisterRandomization"
  ))
}
