#!/bin/sh

sbt run

vivado -nojournal -mode batch -source src/main/tcl/synth.tcl -tclargs arty-a7-100 -log outputs/synth.log

vivado -nojournal -mode batch -source src/main/tcl/place.tcl -log outputs/place.log

vivado -nojournal -mode batch -source src/main/tcl/route.tcl -log outputs/route.log

djtgcfg prog --file outputs/top.bit -d Arty -i 0

rm usage_statistics_webtalk.html usage_statistics_webtalk.xml