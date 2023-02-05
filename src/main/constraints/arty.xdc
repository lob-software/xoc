# FPGA Configuration I/O Options
set_property CONFIG_VOLTAGE 3.3 [current_design]
set_property CFGBVS VCCO [current_design]

# Clock and Reset
set_property -dict { PACKAGE_PIN E3 IOSTANDARD LVCMOS33 } [get_ports {clock}]
create_clock -period 10.0 [get_ports {clock}]
set_property -dict { PACKAGE_PIN C2 IOSTANDARD LVCMOS33 } [get_ports { reset }]

# LEDs
set_property -dict { PACKAGE_PIN H5    IOSTANDARD LVCMOS33 } [get_ports { io_led[0] }]; #IO_L24N_T3_35 Sch=led[4]
set_property -dict { PACKAGE_PIN J5    IOSTANDARD LVCMOS33 } [get_ports { io_led[1] }]; #IO_25_35 Sch=led[5]
set_property -dict { PACKAGE_PIN T9    IOSTANDARD LVCMOS33 } [get_ports { io_led[2] }]; #IO_L24P_T3_A01_D17_14 Sch=led[6]
set_property -dict { PACKAGE_PIN T10   IOSTANDARD LVCMOS33 } [get_ports { io_led[3] }]; #IO_L24N_T3_A00_D16_14 Sch=led[7]

## RGB LEDs

set_property -dict { PACKAGE_PIN G6    IOSTANDARD LVCMOS33 } [get_ports { io_led_r[0] }]; #IO_L19P_T3_35 Sch=led0_r
set_property -dict { PACKAGE_PIN G3    IOSTANDARD LVCMOS33 } [get_ports { io_led_r[1] }]; #IO_L20N_T3_35 Sch=led1_r
set_property -dict { PACKAGE_PIN J3    IOSTANDARD LVCMOS33 } [get_ports { io_led_r[2] }]; #IO_L22P_T3_35 Sch=led2_r
set_property -dict { PACKAGE_PIN K1    IOSTANDARD LVCMOS33 } [get_ports { io_led_r[3] }]; #IO_L23N_T3_35 Sch=led3_r

set_property -dict { PACKAGE_PIN F6    IOSTANDARD LVCMOS33 } [get_ports { io_led_g[0] }]; #IO_L19N_T3_VREF_35 Sch=led0_g
set_property -dict { PACKAGE_PIN J4    IOSTANDARD LVCMOS33 } [get_ports { io_led_g[1] }]; #IO_L21P_T3_DQS_35 Sch=led1_g
set_property -dict { PACKAGE_PIN J2    IOSTANDARD LVCMOS33 } [get_ports { io_led_g[2] }]; #IO_L22N_T3_35 Sch=led2_g
set_property -dict { PACKAGE_PIN H6    IOSTANDARD LVCMOS33 } [get_ports { io_led_g[3] }]; #IO_L24P_T3_35 Sch=led3_g

set_property -dict { PACKAGE_PIN E1    IOSTANDARD LVCMOS33 } [get_ports { io_led_b[0] }]; #IO_L18N_T2_35 Sch=led0_b
set_property -dict { PACKAGE_PIN G4    IOSTANDARD LVCMOS33 } [get_ports { io_led_b[1] }]; #IO_L20P_T3_35 Sch=led1_b
set_property -dict { PACKAGE_PIN H4    IOSTANDARD LVCMOS33 } [get_ports { io_led_b[2] }]; #IO_L21N_T3_DQS_35 Sch=led2_b
set_property -dict { PACKAGE_PIN K2    IOSTANDARD LVCMOS33 } [get_ports { io_led_b[3] }]; #IO_L23P_T3_35 Sch=led3_b

# UART
set_property -dict { PACKAGE_PIN A9    IOSTANDARD LVCMOS33 } [get_ports { io_uartRx }];
set_property -dict { PACKAGE_PIN D10   IOSTANDARD LVCMOS33 } [get_ports { io_uartTx }];
