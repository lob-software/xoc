package io.xoc.core

import chisel3._
import chisel3.util.{Counter, DecoupledIO, Enum, is, switch}


class OrderBookInputBundle extends Bundle {
  val isBid = Input(Bool())
  val price = Input(UInt(64.W))
  val size = Input(UInt(64.W))
}

class OrderBookInput extends DecoupledIO(new OrderBookInputBundle) {

}


class OrderBookInputBuffer extends Module {
  val io = IO(new Bundle() {
    val input = new OrderBookInput()
    val rxDataValid = Input(Bool())
    val rxData = Input(UInt(8.W))
  })

  val isBid :: price :: size :: Nil = Enum(3)
  val expectRxByte = RegInit(isBid)

  // TODO: find out if registers are still neccessary after all conditions are filled out
  val isBidReg = RegInit(false.B)

  io.input.valid := false.B

  io.input.bits.isBid := isBidReg
  io.input.bits.price := 0.U
  io.input.bits.size := 0.U

  val priceBytesReceived = Counter(7)

  when(io.rxDataValid) {
    switch(expectRxByte) {
      is(isBid) {
        when(io.rxData === 0.U) {
          isBidReg := true.B
        } .otherwise {
          isBidReg := false.B
        }

        expectRxByte := price
      }

      is(price) {
        when(priceBytesReceived.value === 0.U) {
          io.input.bits.price := io.rxData
        } .elsewhen(priceBytesReceived.value < 7.U) {
          io.input.bits.price ## io.rxData
        } .otherwise {
          io.input.bits.price ## io.rxData
          expectRxByte := size
          priceBytesReceived.reset()
        }
      }

      is(size) {
        expectRxByte := isBid
      }
    }
  }
}

object OrderBookInputBuffer extends App {
  emitVerilog(new OrderBookInputBuffer(), Array(
    "--target-dir", "generated",
    "--emission-options=disableMemRandomization,disableRegisterRandomization"
  ))
}
