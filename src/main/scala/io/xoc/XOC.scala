package io.xoc

import Chisel.Module
import chisel3._
import io.xoc.core.{OrderBook, OrderBookInputBuffer, OrderBookOutputBuffer}
import io.xoc.uart.{UartRx, UartTx}

class XOC(CLKS_PER_BIT: Int = 10417) extends Module {
  val io = IO(new Bundle() {
    val rx = Input(UInt(1.W))
    val tx = Output(UInt(1.W))
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
  val orderBookInputBuffer = Module(new OrderBookInputBuffer)
  val orderBookOutputBuffer = Module(new OrderBookOutputBuffer)
  val orderBook = Module(new OrderBook)

  // RX
  val rxDataValid = WireDefault(false.B)
  uartRx.io.uartRx := io.rx
  rxDataValid := uartRx.io.rxDataValid
  orderBookInputBuffer.io.rxDataValid := rxDataValid
  orderBookInputBuffer.io.rxData := uartRx.io.rxDataOut
  orderBook.io.input <> orderBookInputBuffer.io.input

  // TX
  val txActive = WireDefault(false.B)
  val txBit = WireDefault(false.B)
  txActive := uartTx.io.txActive
  txBit := uartTx.io.uartTx
  io.tx := Mux(txActive, txBit, true.B)

  uartTx.io.txDataValid := orderBookOutputBuffer.io.txDataValid
  uartTx.io.txData := orderBookOutputBuffer.io.txData
  orderBookOutputBuffer.io.output <> orderBook.io.output
  orderBookOutputBuffer.io.txActive := txActive
}

object XOC extends App {
  emitVerilog(new XOC(), Array(
    "--target-dir", "generated",
    "--emission-options=disableMemRandomization,disableRegisterRandomization"
  ))
}
