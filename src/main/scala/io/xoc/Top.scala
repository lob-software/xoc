package io.xoc

import chisel3._
import io.xoc.uart.UartRx

class Top extends Module {

}

object Top extends App {
  emitVerilog(new Top(), Array(
    "--target-dir", "generated",
    "--emission-options=disableMemRandomization,disableRegisterRandomization"
  ))

  emitVerilog(new UartRx(10417), Array(
    "--target-dir", "generated",
    "--emission-options=disableMemRandomization,disableRegisterRandomization"
  ))
}
