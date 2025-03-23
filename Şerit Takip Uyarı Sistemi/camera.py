import cv2
import threading
def open_camera():
    cap = cv2.VideoCapture(0)

    _, red = cap.read()