import numpy as np
from ultralytics import YOLO
import time
import os
import cv2
import sys
import websockets
from Websocket.websocket import Websocket

lib_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', 'lib'))
sys.path.append(lib_path)


i = 0
def TAS(image, model_path="yolo_model.pt", conf=0.5, iou=0.5):
    # Load the YOLO model
    model = YOLO(model_path)

    signs = list()
    # Perform inference
    results = model(image, conf=conf, iou=iou)

    # Extract bounding boxes and confidence scores
    detections = []
    for result in results:
        for box in result.boxes:
            detections.append({
                'bbox': box.xyxy[0].cpu().numpy(),
                'confidence': box.conf.cpu().item(),
                'class_id': int(box.cls.cpu().item())
            })
            opened_image = cv2.imread(image)
            name = result.names[int(box.cls.cpu().item())]
            confidence = box.conf.cpu().item()
            x1, y1, x2, y2 = [int(coord) for coord in box.xyxy[0].tolist()]
            cropped_image = opened_image[y1:y2, x1:x2]
            signs.append(name)
            # Generate filename with timestamp and class info
            timestamp = int(time.time() * 1000)  # milliseconds
            crop_filename = f"{result.names[int(box.cls.cpu().item())]}_{i}.jpg"
            crop_path = os.path.join("trainset", crop_filename)
            
            # Save cropped image
            cv2.imwrite(crop_path, cropped_image)
            
    return linear_search(signs)   

def linear_search(data: list):
    best_name = ""
    best_confidence = 0
    signs = data
    best_signs = list()
    for i in range(2):
        for d in signs:
            if d[1] > best_confidence:
                best_name = d[0]
                best_confidence = d[1]
        best = (best_name, best_confidence)        
        best_signs.append(best)
        signs.remove(best)
        best_name = ""
        best_confidence = 0

    return best_signs

async def main():
    websocket = Websocket()
    front_camera = Websocket.FrontCamera(websocket)
    ws = await websockets.connect("ws://127.0.0.1:8080/front-camera")
    while True:
        front_camera.recieve_camera_frames(ws)
        list = TAS(image=front_camera)
        if len(list) > 0:
            ws.send(list)