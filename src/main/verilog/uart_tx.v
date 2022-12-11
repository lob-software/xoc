`timescale 1ns / 1ps

module uart_tx
#(parameter CLKS_PER_BIT = 10417)
  (
   input       clock,
   input       tx_dv,
   input [7:0] tx_data, 
   output reg  tx_active,
   output reg  tx_serial,
   output reg  tx_done
   );
 
  localparam IDLE  = 2'b00;
  localparam START = 2'b01;
  localparam DATA  = 2'b10;
  localparam STOP  = 2'b11;
  
  reg [$clog2(CLKS_PER_BIT):0] reg_clks_cnt = 0;
  reg [2:0]  tx_state;
  reg [2:0]  tx_data_idx;
  reg [7:0]  reg_tx_data;


  // Purpose: Control TX state machine
  always @(posedge clock)
  begin

      tx_done <= 1'b0;

      case (tx_state)
      IDLE :
        begin
          tx_serial   <= 1'b1;         // Drive Line High for Idle
          reg_clks_cnt <= 0;
          tx_data_idx   <= 0;
          
          if (tx_dv == 1'b1)
          begin
            tx_active <= 1'b1;
            reg_tx_data   <= tx_data;
            tx_state   <= START;
          end
          else
            tx_state <= IDLE;
        end
      
      
      // Send out Start Bit. Start bit = 0
      START :
        begin
          tx_serial <= 1'b0;
          
          // Wait CLKS_PER_BIT-1 clock cycles for start bit to finish
          if (reg_clks_cnt < CLKS_PER_BIT-1)
          begin
            reg_clks_cnt <= reg_clks_cnt + 1;
            tx_state     <= START;
          end
          else
          begin
            reg_clks_cnt <= 0;
            tx_state     <= DATA;
          end
        end
      
      
      // Wait CLKS_PER_BIT-1 clock cycles for data bits to finish         
      DATA :
        begin
          tx_serial <= reg_tx_data[tx_data_idx];
          
          if (reg_clks_cnt < CLKS_PER_BIT-1)
          begin
            reg_clks_cnt <= reg_clks_cnt + 1;
            tx_state     <= DATA;
          end
          else
          begin
            reg_clks_cnt <= 0;
            
            // Check if we have sent out all bits
            if (tx_data_idx < 7)
            begin
              tx_data_idx <= tx_data_idx + 1;
              tx_state   <= DATA;
            end
            else
            begin
              tx_data_idx <= 0;
              tx_state   <= STOP;
            end
          end 
        end
      
      
      // Send out Stop bit.  Stop bit = 1
      STOP:
        begin
          tx_serial <= 1'b1;
          
          // Wait CLKS_PER_BIT-1 clock cycles for Stop bit to finish
          if (reg_clks_cnt < CLKS_PER_BIT-1)
          begin
            reg_clks_cnt <= reg_clks_cnt + 1;
            tx_state     <= STOP;
          end
          else
          begin
            tx_done     <= 1'b1;
            reg_clks_cnt <= 0;
            tx_state     <= IDLE;
            tx_active   <= 1'b0;
          end 
        end    
      
      default:
        tx_state <= IDLE;
      
    endcase
    end
endmodule
