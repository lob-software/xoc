set_param board.repoPaths "src/main/boards/"

set board [lindex $argv 0]
set design [lindex $argv 1]

switch $board {
    "arty-a7-35" {
        puts "Setting up project for Arty A7 35"
        create_project -in_memory -part xc7a35ticsg324-1L
        set_property board_part digilentinc.com:arty-a7-35:part0:1.0 [current_project]
    }
    "arty-a7-100" {
        puts "Setting up project for Arty A7 100"
        create_project -in_memory -part xc7a100tcsg324-1
        set_property board_part digilentinc.com:arty-a7-100:part0:1.0 [current_project]
    }
    default {
        puts "Provide valid BOARD parameter. E.g.: BOARD={arty-a7-35|arty-a7-100}"
        exit 1
    }
}


set output_dir "./outputs"

# exec rm -rf outputs/ips
# exec cp -r ips/ outputs/ips
# read_ip -verbose outputs/ips/design_1_clk_wiz_0_0/design_1_clk_wiz_0_0.xci


puts "Synthesising generated/$design.v"

read_verilog generated/$design.v

# upgrade_ip [get_ips]
# set_property generate_synth_checkpoint false [get_files outputs/ips/design_1_clk_wiz_0_0/design_1_clk_wiz_0_0.xci]
# set_property generate_synth_checkpoint false [get_files ips/hardcaml_arty_tri_mode_ethernet_mac_0/hardcaml_arty_tri_mode_ethernet_mac_0.xci]
generate_target all [get_ips]
validate_ip [get_ips]

synth_design -top {$design}
write_checkpoint -force "${::output_dir}/post_synth.dcp"
