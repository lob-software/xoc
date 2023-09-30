#!/bin/sh

sbt "runMain io.xoc.XOC"

vivado -mode batch -nolog -nojournal -source src/main/tcl/build.tcl

djtgcfg prog --file outputs/top.bit -d Arty -i 0

rm usage_statistics_webtalk.html usage_statistics_webtalk.xml