#!/bin/sh

sbt "runMain io.xoc.XOC"

mkdir -p "outputs"

vivado -mode batch -nolog -nojournal -source src/main/tcl/build.tcl

openFPGALoader -b arty outputs/top.bit

rm -rf .Xil usage_statistics_webtalk.html usage_statistics_webtalk.xml
