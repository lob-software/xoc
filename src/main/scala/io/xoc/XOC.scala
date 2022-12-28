package io.xoc

import Chisel.Module
import chisel3._
import io.xoc.core.{OrderBook, OrderBookInputBuffer}
import io.xoc.uart.{UartRx, UartTx}

class XOC extends Module {
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

  val uartRx = Module(new UartRx)
  val uartTx = Module(new UartTx)
  val orderBookInputBuffer = Module(new OrderBookInputBuffer)
  val orderBook = Module(new OrderBook)

  // RX
  uartRx.io.uartRx := io.rx
  orderBookInputBuffer.io.rxDataValid := uartRx.io.rxDataValid
  orderBookInputBuffer.io.rxData := uartRx.io.rxDataOut
  orderBook.io.input <> orderBookInputBuffer.io.input

  // TX
  io.tx := uartTx.io.uartTx
//  uartTx.io.txActive := orderBook.io.input.ready // TODO change
  uartTx.io.txDataValid := orderBook.io.input.valid // TODO change
  uartTx.io.txData := orderBook.io.output.bits.askSize // TODO change
  orderBook.io.output.ready := true.B
}

object XOC extends App {
  emitVerilog(new XOC(), Array(
    "--target-dir", "generated",
    "--emission-options=disableMemRandomization,disableRegisterRandomization"
  ))
}
