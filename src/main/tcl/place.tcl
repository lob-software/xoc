set_param board.repoPaths "src/main/boards/"
set output_dir "outputs/"

open_checkpoint $output_dir/post_synth.dcp

########################
# Various pin placements
########################

read_xdc src/main/constraints/arty.xdc

opt_design
place_design
phys_opt_design

write_checkpoint -force $output_dir/post_place
report_timing_summary -file $output_dir/post_place_timing_summary.rpt
