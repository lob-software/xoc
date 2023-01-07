package io.xoc

import chisel3._
import chiseltest._
import io.xoc.uart.{UartRx, UartTx}
import org.scalatest.flatspec.AnyFlatSpec


class UartTxSpec extends AnyFlatSpec with ChiselScalatestTester {

  val CLKS_PER_BIT = 4

  private def clockSerial(tx: UartTx): Unit = {
    tx.clock.step(CLKS_PER_BIT)
  }

  "UartTx" should "send a byte" in {
    test(new UartTx(CLKS_PER_BIT)) { tx =>
      tx.clock.setTimeout(0)

      tx.io.txDataValid.poke(true.B)

      assertByteTransmitted(tx, "b00101001".U)
      assertByteTransmitted(tx, "b01001111".U)
      assertByteTransmitted(tx, "b11111111".U)
      assertByteTransmitted(tx, "b00000000".U)
    }
  }

  private def assertByteTransmitted(tx: UartTx, byte: UInt): Unit = {
    tx.io.txData.poke(byte)
    tx.io.uartTx.expect(true.B)
    tx.clock.step()

    // start bit
    tx.io.uartTx.expect(false.B)
    tx.io.txActive.expect(true.B)

    byte.asBools.padTo(8, false.B).foreach(b => {
      // data
      clockSerial(tx)
      tx.io.uartTx.expect(b)
      tx.io.txActive.expect(true.B)
    })

    // last < 7 check cycle in state == data
    clockSerial(tx)

    // stop bit
    tx.clock.step()
    tx.io.uartTx.expect(true.B)

    tx.clock.step()
    tx.clock.step()
    tx.clock.step()
    tx.io.txActive.expect(false.B)
  }
}
