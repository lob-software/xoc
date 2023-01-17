package io.xoc

import Chisel.Module
import chisel3._
import io.xoc.core.{Buffer, OrderBook, OrderBookInput, OrderBookInputBuffer, OrderBookOutput, OrderBookOutputBuffer}
import io.xoc.uart.{UartRx, UartTx}

class XOC(CLKS_PER_BIT: Int = 10417) extends Module {
  val io = IO(new Bundle() {
    val uartRx = Input(Bool())
    val uartTx = Output(Bool())
  })

  // RX transformation: rx -> OrderBookInput
  // Receive 17 bytes in total (UART MTU is 1 byte):
  // 1 byte: isBid
  // 8 bytes: price
  // 8 bytes: size
  // some kind of buffer
  // ready and valid interfacing with OrderBook (and with UART?)

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
}

object XOC extends App {
  emitVerilog(new XOC(), Array(
    "--target-dir", "generated",
    "--emission-options=disableMemRandomization,disableRegisterRandomization"
  ))
}
