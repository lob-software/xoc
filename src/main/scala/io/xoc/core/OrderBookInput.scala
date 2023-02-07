package io.xoc.core

import chisel3._
import chisel3.util.{DecoupledIO, Enum, is, switch}


class OrderBookInputBundle extends Bundle {
  val isBid = Input(Bool())
  val price = Input(UInt(8.W))
  val size = Input(UInt(8.W))
}

class OrderBookInput extends Module {
  val io = IO(new Bundle() {
    val input = DecoupledIO(new OrderBookInputBundle())
    val rxDataValid = Input(Bool())
    val rxData = Input(UInt(8.W))
  })

  val isBid :: price :: size :: Nil = Enum(3)
  val expectByte = RegInit(isBid)

  val validReg = RegInit(false.B)
  val isBidReg = RegInit(false.B)
  val priceReg = RegInit(0.U(8.W))
  val sizeReg = RegInit(0.U(8.W))

  io.input.valid := validReg
  io.input.bits.isBid := isBidReg
  io.input.bits.price := priceReg
  io.input.bits.size := sizeReg

  when(io.rxDataValid) {
    switch(expectByte) {
      is(isBid) {
        validReg := false.B
        isBidReg := io.rxData === 0.U
        expectByte := price
      }

      is(price) {
        priceReg := io.rxData
        expectByte := size
      }

      is(size) {
        sizeReg := io.rxData
        expectByte := isBid
        validReg := true.B
      }
    }
  }.otherwise {
    validReg := false.B
  }
}

object OrderBookInputBuffer extends App {
  emitVerilog(new OrderBookInput(), Array(
    "--target-dir", "generated",
    "--emission-options=disableMemRandomization,disableRegisterRandomization"
  ))
}
