import tensorflow as tf
import numpy as np
import pandas as pd
import os
import cv2
import keras._tf_keras.keras
import ai

model = keras.models.load_model("traffic_sign_model.h5")

test_df = pd.read_csv("Test.csv")
test_images_path = test_df["Path"].values

test_images = np.array([ai.load_and_prepropress_images(path) for path in test_images_path])

predictions = model.predict(test_images)

predicted_classes = np.argmax(predictions, axis=1)

results_df = pd.DataFrame({
    "Image": test_images_path,  # Image filenames
    "Predicted_Class": predicted_classes  # Model's predicted class
})

results_df.to_csv("Prediction.csv", index = False)
print("Predictions saved to Predictions.csv!")