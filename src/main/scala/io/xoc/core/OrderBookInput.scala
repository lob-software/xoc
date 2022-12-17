package io.xoc.core

import chisel3._


class OrderBookInput extends Bundle {
  val isBid = Input(Bool())
  val price = Input(UInt(64.W))
  val size = Input(UInt(64.W))
}

