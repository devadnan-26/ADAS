import websockets
import cv2
import websockets
import asyncio
import base64
import logging
import numpy as np
import sys
import os
from TAS.TAS import TAS

file = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', 'lib'))
sys.path.append(file)
# Enable logging to debug connection issues
class Websocket:
    def __init__(self):
        self.host = "127.0.0.1"
        self.port = 8080

    class FrontCamera():
        def __init__(self ,websocket):
            self.host = websocket.host
            self.port = websocket.port
            self.path = "/front-camera"
            self.is_open = False
            self.logging = logging.basicConfig(level=logging.INFO)


        async def handle_camera_stream(self, websocket): 
            print(f"New connection to path: {self.path}")
            
            if self.path != "/front-camera":
                await websocket.close(code=1008, reason="Invalid endpoint")
                print(f"Invalid endpoint: {self.path}")
                return
            
            cam = cv2.VideoCapture(1, cv2.CAP_DSHOW)
            
            if not cam.isOpened():
                print("Failed to open camera")
                await websocket.close(code=1011, reason="Camera unavailable")
                return
            
            try:
                print("Camera opened successfully, starting stream...")
                
                await asyncio.sleep(0.1)
                
                while True:
                    ret, frame = cam.read()
                    if not ret:
                        print("Failed to grab frame")
                        break
                    list = TAS(image=frame)
                    if len(list) > 0:
                        websocket.send(list) 
            except Exception as e:
                print(f"Error in camera stream: {e}")
            finally:
                print("Releasing camera and closing connection")
                cam.release()
                cv2.destroyAllWindows()
                exit()

        async def main(self):
            
            print(f"Starting WebSocket server on {self.host}:{self.port}")
            
            try:
                async with websockets.serve(self.handle_camera_stream, self.host, self.port):
                    print("WebSocket server started successfully")
                    print("Waiting for connections...")
                    await asyncio.Future()
            except Exception as e:
                print(f"Failed to start server: {e}")


    class BackCamera:
        def __init__(self, websocket):
            self.host = websocket.host
            self.port = websocket.port
            self.path = "/back-camera"
            self.is_open = False
            self.logging = logging.basicConfig(level=logging.INFO)

        async def handle_camera_stream(self, websocket): 
            print(f"New connection to path: {self.path}")
            
            if self.path != "/back-camera":
                await websocket.close(code=1008, reason="Invalid endpoint")
                print(f"Invalid endpoint: {self.path}")
                return
            
            cam = cv2.VideoCapture(0, cv2.CAP_DSHOW)
            
            if not cam.isOpened():
                print("Failed to open camera")
                await websocket.close(code=1011, reason="Camera unavailable")
                return
            
            try:
                print("Camera opened successfully, starting stream...")
                
                await asyncio.sleep(0.1)
                
                while True:
                    ret, frame = cam.read()
                    if not ret:
                        print("Failed to grab frame")
                        break
                    
                    ret_encode, buffer = cv2.imencode('.jpg', frame, [cv2.IMWRITE_JPEG_QUALITY, 80])
                    if not ret_encode:
                        print("Failed to encode frame")
                        continue

                    encoded = base64.b64encode(buffer).decode('utf-8')
                    try:
                        await websocket.send(encoded)  
                    except websockets.exceptions.ConnectionClosed:
                        print("Client disconnected")
                        break
                    except Exception as e:
                        print(f"Error sending frame: {e}")
                        break
                        
                    # Control frame rate (50 FPS = 0.02 seconds)
                    await asyncio.sleep(0.02)
                    
            except Exception as e:
                print(f"Error in camera stream: {e}")
            finally:
                print("Releasing camera and closing connection")
                cam.release()
                cv2.destroyAllWindows()
                exit()

        async def main(self):
            
            print(f"Starting WebSocket server on {self.host}:{self.port}")
            
            try:
                async with websockets.serve(self.handle_camera_stream, self.host, self.port):
                    print("WebSocket server started successfully")
                    print("Waiting for connections...")
                    await asyncio.Future()
            except Exception as e:
                print(f"Failed to start server: {e}")


