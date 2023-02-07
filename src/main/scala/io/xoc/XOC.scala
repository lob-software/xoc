package io.xoc

import Chisel.Module
import chisel3._
import io.xoc.core.{Buffer, OrderBook, OrderBookInput, OrderBookOutput}
import io.xoc.uart.{UartRx, UartTx}

class XOC(CLKS_PER_BIT: Int = 10417) extends Module {
  val io = IO(new Bundle() {
    val uartRx = Input(Bool())
    val uartTx = Output(Bool())
    val led = Output(UInt(4.W))
    val led_r = Output(UInt(4.W))
    val led_g = Output(UInt(4.W))
    val led_b = Output(UInt(4.W))
  })

  private val disabledReset: Bool = false.B

  withClockAndReset(clock, disabledReset) {

    val uartRx = Module(new UartRx(CLKS_PER_BIT))
    val uartTx = Module(new UartTx(CLKS_PER_BIT))
    val orderBookInput = Module(new OrderBookInput)
    val orderBookOutput = Module(new OrderBookOutput)
    val outputBuffer = Module(new Buffer)
    val orderBook = Module(new OrderBook)

    // RX
    val validSeq = WireDefault(0.U(8.W))
    val rxDataValid = WireDefault(false.B)
    uartRx.io.uartRx := io.uartRx
    rxDataValid := uartRx.io.rxDataValid
    orderBookInput.io.rxDataValid := rxDataValid
    orderBookInput.io.rxData := uartRx.io.rxDataOut
    orderBook.io.input <> orderBookInput.io.input
    validSeq := orderBook.io.validSeq

    // TX
    val txActive = WireDefault(false.B)
    val txBit = WireDefault(false.B)

    txActive := uartTx.io.txActive
    txBit := uartTx.io.uartTx
    io.uartTx := Mux(txActive, txBit, true.B)

    orderBookOutput.io.output <> orderBook.io.output
    orderBookOutput.io.validSeq := validSeq
    orderBookOutput.io.uart <> outputBuffer.io.in
    uartTx.io.txDataValid := outputBuffer.io.out.valid
    uartTx.io.txData := outputBuffer.io.out.bits
    outputBuffer.io.out.ready := !txActive

    io.led := Mux(orderBook.io.input.valid, "b1111".U, "b0000".U) // orderBookInput.io.input.bits.price(3, 0)
    io.led_g := orderBook.io.output.bits.askPrice // Mux(orderBook.io.output.valid, "b1111".U, "b0000".U) // orderBookInput.io.input.bits.price(3, 0)
//    io.led_g := 0.U //Mux(orderBook.io.output.valid =/= 0.U, "b1111".U, "b0000".U) // orderBookInput.io.input.bits.price(7, 4)
    io.led_r := 0.U //orderBook.io.output.bits.bidPrice //Mux(orderBook.io.output.bits.bidSize =/= 0.U, "b1111".U, "b0000".U) // orderBookInput.io.input.bits.price(3, 0)
    io.led_b := orderBook.io.output.bits.bidPrice  //Mux(orderBook.io.output.bits.askSize =/= 0.U, "b1111".U, "b0000".U) // orderBookInput.io.input.bits.price(3, 0)
  }
}

object XOC extends App {
  emitVerilog(new XOC(), Array(
    "--target-dir", "generated",
    "--emission-options=disableMemRandomization,disableRegisterRandomization"
  ))
}
