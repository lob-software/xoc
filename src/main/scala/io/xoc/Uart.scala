package io.xoc

import chisel3._
import io.xoc.uart.UartRx

class Uart(clksPerBit: Int) extends Module {
  val io = IO(new Bundle {
    val uartRx = Input(UInt(1.W))
    val led = Output(UInt(8.W))
  })

  val data = WireDefault(0.U(8.W))
  val rx = Module(new UartRx(clksPerBit))

  private val disabledReset: Bool = false.B

  withClockAndReset(clock, disabledReset) {
    rx.io.uartRx := io.uartRx
    data := rx.io.rxData
    io.led := rx.io.rxDv & data
  }
}

object Uart extends App {

  emitVerilog(new Uart(10417), Array(
    "--target-dir", "generated",
    "--emission-options=disableMemRandomization,disableRegisterRandomization"
  ))
}
