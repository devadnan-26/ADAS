import cv2
import numpy as np

# Load the image
image = cv2.imread('test.jpg')

# Convert to grayscale
gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

# Apply GaussianBlur to reduce noise and improve edge detection
blurred = cv2.GaussianBlur(gray, (5, 5), 0)

# Use Canny edge detection
edges = cv2.Canny(blurred, 50, 150)

# Find contours in the edge-detected image
contours, _ = cv2.findContours(edges, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

# Filter contours based on area and shape
min_area = 500  # Minimum area of the contour
bounding_boxes = []

for cnt in contours:
    area = cv2.contourArea(cnt)
    if area > min_area:
        # Approximate the contour to a polygon
        epsilon = 0.02 * cv2.arcLength(cnt, True)
        approx = cv2.approxPolyDP(cnt, epsilon, True)
        
        # Check if the contour is a polygon with 3-8 sides (common for traffic signs)
        if 3 <= len(approx) <= 8:
            x, y, w, h = cv2.boundingRect(cnt)
            bounding_boxes.append((x, y, w, h))

# Crop the image using the largest bounding box
if bounding_boxes:
    # Sort bounding boxes by area (largest first)
    bounding_boxes = sorted(bounding_boxes, key=lambda box: box[2] * box[3], reverse=True)
    x, y, w, h = bounding_boxes[0]  # Use the largest bounding box
    cropped_image = image[y:y+h, x:x+w]
    cv2.imwrite('cropped_traffic_sign.jpg', cropped_image)
    cv2.imshow('Cropped Traffic Sign', cropped_image)
    cv2.waitKey(0)
    cv2.destroyAllWindows()
else:
    print("No traffic sign detected.")