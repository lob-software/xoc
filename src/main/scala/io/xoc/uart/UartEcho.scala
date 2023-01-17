package io.xoc.uart

import chisel3._

class UartEcho(CLKS_PER_BIT: Int = 10417) extends Module {
  val io = IO(new Bundle {
    val uartRx = Input(Bool())
    val uartTx = Output(Bool())
  })

  private val disabledReset: Bool = false.B

  withClockAndReset(clock, disabledReset) {
    val rxDataValid = WireDefault(false.B)
    val txActive = WireDefault(false.B)
    val uartTx = WireDefault(false.B)
    val rxData = WireDefault(0.U(8.W))

    val rx = Module(new UartRx(CLKS_PER_BIT))
    rx.io.uartRx := io.uartRx
    rxDataValid := rx.io.rxDataValid
    rxData := rx.io.rxDataOut

    val tx = Module(new UartTx(CLKS_PER_BIT))
    tx.io.txDataValid := rxDataValid
    tx.io.txData := rxData
    txActive := tx.io.txActive
    uartTx := tx.io.uartTx

    io.uartTx := Mux(txActive, uartTx, true.B)
  }
}


object UartEcho extends App {
  println(getVerilogString(new UartEcho()))
  emitVerilog(new UartEcho(), Array(
    "--target-dir", "generated",
    "--emission-options=disableMemRandomization,disableRegisterRandomization"
  ))
}
