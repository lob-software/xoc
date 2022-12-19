package io.xoc

import Chisel.Module
import chisel3._
import io.xoc.core.OrderBookInputBuffer
import io.xoc.uart.UartRx

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
  val orderBookInputBuffer = Module(new OrderBookInputBuffer)

  // TODO can be a channel
  orderBookInputBuffer.io.rxDataValid := uartRx.io.rxDataValid
  orderBookInputBuffer.io.rxData := uartRx.io.rxDataOut


}

object XOC extends App {
  emitVerilog(new XOC(), Array(
    "--target-dir", "generated",
    "--emission-options=disableMemRandomization,disableRegisterRandomization"
  ))
}
