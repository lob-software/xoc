#!/bin/sh

DESIGN=$1
MAIN_CLASS=$2

echo programming with "$DESIGN"

sbt "runMain $MAIN_CLASS"

vivado -nojournal -mode batch -source src/main/tcl/synth.tcl -tclargs arty-a7-100 "$DESIGN" outputs/synth.log

vivado -nojournal -mode batch -source src/main/tcl/place.tcl -log outputs/place.log

vivado -nojournal -mode batch -source src/main/tcl/route.tcl -log outputs/route.log

djtgcfg prog --file outputs/top.bit -d Arty -i 0

rm usage_statistics_webtalk.html usage_statistics_webtalk.xml