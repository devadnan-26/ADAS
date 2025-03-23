import cv2
import tensorflow as tf
import keras._tf_keras.keras
import numpy as np
import common

model = keras.models.load_model("traffic_sign_model.h5")
cap = cv2.VideoCapture(0)

min_erea = 1000
bounding_boxes = []

while True:
    ret, frame = cap.read()
    processed_frame = frame.copy()

    if not ret:
        break

    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(gray, (5,5), 0)
    edges = cv2.Canny(blurred, 50, 150)
    contour, _ = cv2.findContours(edges, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    for cnt in contour:
        erea = cv2.contourArea(cnt)
        if erea > min_erea:
            epsilon = 0.02 * cv2.arcLength(cnt, True)
            approx = cv2.approxPolyDP(cnt, epsilon, True)
            if 3 <= len(approx) <= 8:
                x, y, w, h = cv2.boundingRect(cnt)
                bounding_boxes.append((x, y, w, h))

    if bounding_boxes != []:
        # for box in bounding_boxes:
        #     x, y, w, h = box
        #     cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)  # Green rectangle with thi
        x, y, w, h = bounding_boxes[0]  # Use the largest bounding box
        cropped_image = frame[y:y+h + 2, x:x+w+ 5]
        cv2.imwrite('cropped_traffic_sign.jpg', cropped_image)

        predictions = model.predict(common.preprocess_image("cropped_traffic_sign.jpg"))

        predicted_classes = np.argmax(predictions, axis=1)
        print(predicted_classes)
        break

    cv2.imshow('Live Traffic Sign Detection', frame)
    
    # Break the loop if 'q' is pressed
    key = cv2.waitKey(1)
    if key == ord('q'):
        break

    # resized_img = cv2.resize(frame, (64, 64))  # Resize to match input_shape
    # resized_img = resized_img / 255.0  # Normalize pixel values (optional, if trained with normalization)

    # input_tensor = tf.convert_to_tensor([resized_img], dtype=tf.float32)
    # input_tensor = input_tensor / 255.0

    
cap.release()
cv2.destroyAllWindows()