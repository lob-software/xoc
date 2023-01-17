package io.xoc.uart

import chisel3._
import chisel3.util._

class UartTx(CLKS_PER_BIT: Int = 10417) extends Module {

  val io = IO(new Bundle {
    val txDataValid = Input(Bool())
    val txData = Input(UInt(8.W))
    val txActive = Output(Bool())
    val uartTx = Output(Bool())
  })

  val count = Counter(CLKS_PER_BIT)
  val idle :: start :: data :: stop :: Nil = Enum(4)
  val txState = RegInit(idle)
  val txDataIdx = RegInit(0.U(3.W))
  val txData = RegInit(0.U(8.W))
  val txActive = RegInit(false.B)

  io.txActive := txActive
  io.uartTx := false.B

  switch(txState) {

    is(idle) {
      io.uartTx := true.B
      count.reset()
      txDataIdx := 0.U

      when(io.txDataValid) {
        txActive := true.B
        txData := io.txData
        txState := start
      }.otherwise {
        txState := idle
      }
    }

    is(start) {
      // start bit
      io.uartTx := false.B

      when(count.value < (CLKS_PER_BIT - 1).U) {
        count.inc()
        txState := start
      }.otherwise {
        count.reset()
        txState := data
      }
    }

    is(data) {

      io.uartTx := txData(txDataIdx)

      when(count.value < (CLKS_PER_BIT - 1).U) {
        count.inc()
        txState := data
      }.otherwise {
        count.reset()

        when(txDataIdx < 7.U) {
          txDataIdx := txDataIdx + 1.U
          txState := data
        }.otherwise {
          txDataIdx := 0.U
          txState := stop
        }
      }
    }

    is(stop) {
      // stop bit
      io.uartTx := true.B

      when(count.value < (CLKS_PER_BIT - 1).U) {
        count.inc()
        txState := stop
      }.otherwise {
        count.reset()
        txState := idle
        txActive := false.B
      }
    }
  }
}

object UartTx extends App {
  println(getVerilogString(new UartTx(10417)))
  emitVerilog(new UartTx(10417), Array(
    "--target-dir", "generated",
    "--emission-options=disableMemRandomization,disableRegisterRandomization"
  ))
}
