package io.xoc.uart

import chisel3._
import chisel3.util._

class UartRx(CLKS_PER_BIT: Int) extends Module {

  val io = IO(new Bundle {
    val uartRx = Input(Bool())
    val rxDataValid = Output(Bool())
    val rxDataOut = Output(UInt(8.W))
  })

  private val disabledReset: Bool = false.B

  withClockAndReset(clock, disabledReset) {

    val count = Counter(CLKS_PER_BIT)
    val idle :: start :: data :: stop :: cleanup :: Nil = Enum(5)
    val rxState = RegInit(idle)
    val rxDataValid = RegInit(false.B)
    val rxData = RegInit(0.U(8.W))
    val rxDataIdx = RegInit(0.U(3.W))

    switch(rxState) {
      is(idle) {

        count.reset()
        rxDataValid := false.B
        rxDataIdx := 0.U

        when(!io.uartRx) {
          rxState := start
        }.otherwise {
          rxState := idle
        }
      }

      is(start) {
        when(count.value === ((CLKS_PER_BIT - 1) / 2).U) {
          when(!io.uartRx) {
            count.reset()
            rxState := data
          }.otherwise {
            rxState := idle
          }
        }.otherwise {
          count.inc()
          rxState := start
        }
      }

      is(data) {
        when(count.value < (CLKS_PER_BIT - 1).U) {
          count.inc()
          rxState := data
        }.otherwise {
          count.reset()
          rxData := rxData.bitSet(rxDataIdx, io.uartRx)

          when(rxDataIdx < 7.U) {
            rxDataIdx := rxDataIdx + 1.U
            rxState := data
          }.otherwise {
            rxDataIdx := 0.U
            rxState := stop
          }
        }
      }

      is(stop) {
        when(count.value < (CLKS_PER_BIT - 1).U) {
          count.inc()
          rxState := stop
        }.otherwise {
          rxDataValid := true.B
          count.reset()
          rxState := cleanup
        }
      }

      is(cleanup) {
        rxDataValid := false.B
        rxState := idle
      }
    }

    io.rxDataValid := rxDataValid
    io.rxDataOut := rxData
  }
}

object UartRx extends App {
  println(getVerilogString(new UartRx(10417)))
  emitVerilog(new UartRx(10417), Array(
    "--target-dir", "generated",
    "--emission-options=disableMemRandomization,disableRegisterRandomization"
  ))
}
