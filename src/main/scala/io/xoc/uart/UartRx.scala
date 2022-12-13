package io.xoc.uart

import chisel3._
import chisel3.util._

class UartRx(CLKS_PER_BIT: Int) extends Module {

  def createCounter(n: Int) = {
    val cntReg = RegInit(0.U(32.W))
    cntReg := Mux(cntReg === n.U, 0.U, cntReg + 1.U)
    cntReg
  }

  val io = IO(new Bundle {
    val led = Output(UInt(4.W))
    val uartRx = Input(Bool())
  })

  private val disabledReset: Bool = false.B

  withClockAndReset(clock, disabledReset) {

    val count = createCounter(CLKS_PER_BIT)
    val idle :: start :: data :: stop :: cleanup :: Nil = Enum(5)
    val rxState = RegInit(idle)
    val rxDataValid = RegInit(false.B)
    val rxData = RegInit(0.U(8.W))
    val rxDataIdx = RegInit(0.U(3.W))

    switch(rxState) {
      is(idle) {

        count := 0.U
        rxDataValid := false.B
        rxDataIdx := 0.U

        when(!io.uartRx) {
          rxState := start
        }.otherwise {
          rxState := idle
        }
      }

      is(start) {
        when(count === ((CLKS_PER_BIT - 1) / 2).U) {
          when(!io.uartRx) {
            count := 0.U
            rxState := data
          }.otherwise {
            rxState := idle
          }
        }.otherwise {
          count := count + 1.U
          rxState := start
        }
      }

      is(data) {
        when(count < (CLKS_PER_BIT - 1).U) {
          count := count + 1.U
          rxState := data
        }.otherwise {
          count := 0.U
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
        when(count < (CLKS_PER_BIT - 1).U) {
          count := count + 1.U
          rxState := stop
        }.otherwise {
          rxDataValid := true.B
          count := 0.U
          rxState := cleanup
        }
      }

      is(cleanup) {
        rxDataValid := false.B
        rxState := idle
      }
    }

    io.led := rxData
  }
}

object UartRx extends App {
  println(getVerilogString(new UartRx(10417)))
  emitVerilog(new UartRx(10417), Array(
    "--target-dir", "generated",
    "--emission-options=disableMemRandomization,disableRegisterRandomization"
  ))
}
