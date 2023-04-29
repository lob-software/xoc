# XOC - Exchange on a Chip

XOC is a project aimed at building a basic order book on the Arty A7 100T FPGA board. The 
UART (Universal Asynchronous Receiver-Transmitter) is used for communication with the board.

## Prerequisites

1. Install Vivado: Download and install Xilinx Vivado Design Suite from the [Xilinx website](https://www.xilinx.com/support/download.html). Make sure to include the required FPGA board files for the Arty A7 100T during the installation process.
2. Install Digilent Adept: Download and install the [Digilent Adept software suite](https://digilent.com/reference/software/adept/start), which includes the `djtgcfg` utility. You may need to run `djtgcfg enum` and `djtgcfg init -d <device_name>`
in order to initialise USB connectivity to the board.

## Usage

To program the board, run

```
./program.sh
```

This will generate idempotently generate verilog and program the board with resulting XOC design.

After the board is programmed with the design, you can use python UART client to send orders to the order book:

```shell
python3 xoc.py bid 100@100
python3 xoc.py ask 100@120
```
You should see an output indicating the current state of the order book:

```
Bid Price: 100 | Bid Size: 100 | Ask Price: 120 | Ask Size: 100
```

## Support

If you encounter any issues or need assistance, please file an issue in the project's issue tracker or contact the project maintainers.

## License

This project is licensed under the [MIT License](https://opensource.org/licenses/MIT).
