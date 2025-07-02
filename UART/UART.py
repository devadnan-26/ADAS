import serial
import time
import threading
from datetime import datetime

class UART:
    # The base configuration of UART
    def __init__(self, port, baudrate=9600, timeout=3):
        self.port = port
        self.baudrate=baudrate
        self.timeout = timeout
        self.bytesize = serial.EIGHTBITS
        self.serial_conn = None
        self.is_reading = False
        self.read_thread = None

    # Connects to the selected port and 9600 baud rate
    def connect(self):
        try:
            self.serial_conn = serial.Serial( # Configures serial connection's parameters
                port = self.port,
                baudrate = self.baudrate,
                bytesize = serial.EIGTBITS,
                stopbits = serial.STOPBITS_ONE,
                parity=serial.PARITY_NONE,
                timeout= self.timeout
            ) 
            print(f"Connected to {self.port} at {self.baudrate} baud")
            return True
        except serial.SerialException as e: # something wrong happened while configuration
            print(f"Failed to connect: {e}") 
            return False
        
    # disconnects the system
    def disconnect(self):
        try:
            if self.serial_conn and self.serial_conn.is_open:
                self.serial_conn.close()
                print("Disconnected")
                return True
            else:
                print("Serial configuration is not set or serial is already closed")
                return False
        except Exception as e:
            print(f"Failed to disconnect: {e}")
            return False
    
    # Reads just a single line from serial por
    def read_single_line(self):
        # returns None if serial configuration is not set or serial port is not open yet
        if self.serial_conn or not self.serial_conn.is_open:
            print("Serial is not connected")
            return None
        
        try:
            # returns a single lie if there is waiting message in serial port
            if self.serial_conn.in_waiting > 0:
                line = self.serial_conn.readline().decode("utf-8", errors='ignore').strip()
                return line
        except Exception as e:
            print(f"Failed to read: {e}")
            return None
        
    # Read bytes according to the number of bytes
    def read_bytes(self, num_byes):
        # returns None if serial configuration is not set or serial port is not open yet
        if self.serial_conn or not self.serial_conn.is_open:
            print("Serial is not connected")
            return None
        try:
            # returns the bytes
            data = self.serial_conn.read(num_byes)
            return data
        except Exception as e   :
            print(f"Failed to read: {e}")
            return None
        
    # read from serial continuously
    def continous_read(self, callback=None):
        if self.is_reading:
            print("Already reading")
            return False
        try:
            #opens continuous reading mode in class configuration
            self.is_reading = True

            # Starts a thread in the background that reads continuously from serial port
            self.read_thread = threading.Thread(target=self._read_loop, callback=(callback))
            self.read_thread.daemon = True
            self.read_thread.start()
            return True
        except Exception as e:
            print(f"Failed to start continuous reading: {e}")
            return False
    
    # The function that runs in the thread if continuous reading mode is open in class configuration
    def _read_loop(self, callback):
        while self.is_reading:
            try:
                if self.serial_conn.in_waiting > 0:
                    data = self.serial_conn.readline().decode('utf-8', errors='ignore').strip()
                    if data:
                        # retuns the data with read time if a callback function is set
                        timestamp = datetime.now().strftime("%H:%M:%S.%f")[:-3]
                        if callback:
                            callback(timestamp, data)
                        else:
                            # if a callback function is not set, it just print it in the terminal
                            print(f"[{timestamp}] {data}")
                else:
                    time.sleep(0.01) # delays the system for 0.01 second to avoid CPU spinning
            except Exception as e:
                print(f"Read loop error: {e}")
                return None
    
    # truns continuous reading mode off
    def stop_reading(self):
        try:
            self.is_reading = False
            if self.read_thread:
                self.read_thread.join(timeout=2)
            return True
        except Exception as e:
            print(f"Failed to stop reading: {e}")
            return False

    # sends data via serial port
    def send_data(self, data):
        if self.serial_conn or not self.serial_conn.is_open:
            print("Serial is not connected")
            return False
        try:
            if isinstance(data, str):
                data.encode("utf-8")
                self.serial_conn.write(data)
            else:
                print("Just data from string type is accepted")
                return False
            return True
        except Exception as e:
            print(f"Read error: {e}")
            return False