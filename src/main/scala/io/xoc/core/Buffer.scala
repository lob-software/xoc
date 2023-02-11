package io.xoc.core

import chisel3._
import chisel3.util._

class UartIO extends DecoupledIO(UInt(8.W)) {
}

class Buffer extends Module {
  val io = IO(new Bundle {
    val in = Flipped(new UartIO())
    val out = new UartIO()
  })
  val empty :: full :: Nil = Enum(2)
  val stateReg = RegInit(empty)
  val bufferDataReg = RegInit(0.U(8.W))
  io.in.ready := stateReg === empty
  io.out.valid := stateReg === full
  when(stateReg === empty) {
    when(io.in.valid) {
      bufferDataReg := io.in.bits
      stateReg := full
    }
  }.otherwise { // full
    when(io.out.ready) {
      stateReg := empty
    }
  }
  io.out.bits := bufferDataReg
}