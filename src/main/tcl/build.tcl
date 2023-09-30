# read design sources (add one line for each file)
read_verilog "generated/XOC.v"

# read constraints
read_xdc src/main/constraints/arty.xdc

# synth
synth_design -top "XOC" -part "xc7a100tcsg324-1"

# place and route
opt_design
place_design
route_design

# write bitstream
write_bitstream -force "outputs/top.bit"