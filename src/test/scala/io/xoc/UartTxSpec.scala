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
      tx.io.txData.poke("b00101001".U)
      tx.io.uartTx.expect(true.B)
      tx.clock.step()

      // start bit
      tx.io.uartTx.expect(false.B)
      tx.io.txActive.expect(true.B)


      // data
      clockSerial(tx)
      tx.io.uartTx.expect(true.B)
      tx.io.txActive.expect(true.B)

      clockSerial(tx)
      tx.io.uartTx.expect(false.B)
      tx.io.txActive.expect(true.B)

      clockSerial(tx)
      tx.io.uartTx.expect(false.B)
      tx.io.txActive.expect(true.B)

      clockSerial(tx)
      tx.io.uartTx.expect(true.B)
      tx.io.txActive.expect(true.B)

      clockSerial(tx)
      tx.io.uartTx.expect(false.B)
      tx.io.txActive.expect(true.B)

      clockSerial(tx)
      tx.io.uartTx.expect(true.B)
      tx.io.txActive.expect(true.B)

      clockSerial(tx)
      tx.io.uartTx.expect(false.B)
      tx.io.txActive.expect(true.B)

      clockSerial(tx)
      tx.io.uartTx.expect(false.B)
      tx.io.txActive.expect(true.B)
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
}
