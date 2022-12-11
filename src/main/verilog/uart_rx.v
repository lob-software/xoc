`timescale 1ns / 1ps

module uart_rx
#(parameter CLKS_PER_BIT = 10417)
(
    input       clock,
    input       uart_rx,
    output      rx_dv,
    output[7:0] rx_data
);

 localparam IDLE         = 3'b000;
 localparam START        = 3'b001;
 localparam DATA         = 3'b010;
 localparam STOP         = 3'b011;
 localparam CLEANUP      = 3'b101;
 
 reg [31:0] reg_clks_cnt = 0;
 reg        reg_rx_dv;
 reg [7:0]  reg_rx_data;
 reg [3:0]  rx_state;
 reg [2:0]  rx_data_idx;
 
 always @(posedge clock)
 begin
     case(rx_state)
     IDLE:
     begin
        reg_rx_dv    <= 1'b0;
        reg_clks_cnt <= 0;
        rx_data_idx  <= 0;
        if (uart_rx == 1'b0) // start bit
        begin
            rx_state       <= START;
        end
        else
        begin
            rx_state       <= IDLE;
        end     
     end
     START:
     begin
        if (reg_clks_cnt == (CLKS_PER_BIT - 1) / 2)
        begin
            if (uart_rx == 1'b0) // check start bit still low at the middle of BAUD period
            begin
                reg_clks_cnt <= 0;
                rx_state     <= DATA;                 
            end
            else
            begin
                rx_state     <= IDLE;
            end          
        end
        else
        begin
            // still sampling for the middle of start bit
            reg_clks_cnt <= reg_clks_cnt + 1;
            rx_state     <= START;
        end     
     end
     DATA:
     begin
        if (reg_clks_cnt < (CLKS_PER_BIT - 1))
        begin
            reg_clks_cnt <= reg_clks_cnt + 1;
            rx_state     <= DATA;
        end
        else
        begin
            reg_clks_cnt         <= 0;
            reg_rx_data[rx_data_idx] <= uart_rx;
            
            if (rx_data_idx < 7)
            begin
                rx_data_idx <= rx_data_idx + 1;
                rx_state     <= DATA; 
            end
            else
            begin
                rx_data_idx <= 0;
                rx_state    <= STOP; 
            end
        end 
     end
     STOP:
     begin
        if (reg_clks_cnt < CLKS_PER_BIT - 1)
        begin
            reg_clks_cnt <= reg_clks_cnt + 1;
            rx_state     <= STOP;
        end
        else
        begin
            reg_rx_dv    <= 1'b1;
            reg_clks_cnt <= 0;
            rx_state     <= CLEANUP;
        end      
     end
     CLEANUP:
     begin
        reg_rx_dv <= 1'b0;
        rx_state  <= IDLE;
     end
     default:
     begin
        rx_state <= IDLE;
     end
     endcase
  end
  
  assign rx_dv   = reg_rx_dv;
  assign rx_data = reg_rx_data;

endmodule
