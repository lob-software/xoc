package io.xoc

import chisel3._

class Top extends Module {

}

object Top extends App {
  emitVerilog(new Top(), Array(
    "--target-dir", "generated",
    "--emission-options=disableMemRandomization,disableRegisterRandomization"
  ))
}
