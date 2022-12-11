`timescale 1ns / 1ps

module top(
  input        clock,
  output[0:3]  led,
  output[0:3]  led_g,
  input        i_uart_rx,
  output       o_uart_tx
);

  wire rx_dv;
  wire tx_active, tx_serial;
  wire[7:0] rx_data;

  uart_rx #(.CLKS_PER_BIT(10417)) uart_rx_inst
    (.clock(clock),
     .uart_rx(i_uart_rx),
     .rx_dv(rx_dv),
     .rx_data(rx_data));

  uart_tx #(.CLKS_PER_BIT(10417)) uart_tx_inst
    (.clock(clock),
     .tx_dv(rx_dv),
     .tx_data(rx_data),
     .tx_active(tx_active),
     .tx_serial(tx_serial),
     .tx_done());

  assign o_uart_tx = tx_active ? tx_serial : 1'b1; 

  // display data on LEDs  
  reg [3:0]  led_reg_1;
  reg [3:0]  led_reg_2; 
  
  always @ (posedge clock)
  begin
    led_reg_1[3:0] <= rx_data[3:0];
    led_reg_2[3:0] <= rx_data[7:4];
  end
  
  assign led = led_reg_1;
  assign led_g = led_reg_2;

endmodule