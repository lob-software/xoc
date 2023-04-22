import serial
import sys

port = "/dev/ttyUSB1"


def number_to_ascii(n):
    return bytes(chr(n), encoding='ascii')


def bid(price, size, s):
    s.write(number_to_ascii(0))
    s.write(number_to_ascii(price))
    s.write(number_to_ascii(size))


def ask(price, size, s):
    s.write(number_to_ascii(1))
    s.write(number_to_ascii(price))
    s.write(number_to_ascii(size))


def transmit_book_data(s):
    ret = s.read(4)
    if ret:
        bid_price = ret[0]
        bid_size = ret[1]
        ask_price = ret[2]
        ask_size = ret[3]
        print(f"Bid Price: {bid_price} | Bid Size: {bid_size} | Ask Price: {ask_price} | Ask Size: {ask_size}")


if __name__ == '__main__':
    args = sys.argv
    side = args[1]
    size, price = map(int, args[2].split("@"))

    print(f"Received command to place a {side} of size {size} at price {price}")

    with serial.Serial(port, timeout=2, baudrate=9600) as ser:
        if side == 'bid':
            bid(price, size, ser)

        elif side == 'ask':
            ask(price, size, ser)

        else:
            raise RuntimeError(f"unknown side {side} provided")

        transmit_book_data(ser)
