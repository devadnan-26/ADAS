import tensorflow as tf
from keras._tf_keras.keras.preprocessing.image import ImageDataGenerator
from keras._tf_keras.keras import Sequential
import pandas as pd
import numpy as np
import os
import cv2
from sklearn.model_selection import train_test_split

train_df = pd.read_csv("Train.csv")

image_paths = train_df["Path"].values
labels = train_df["ClassId"].values

train_paths, val_paths, train_labels, val_labels = train_test_split(image_paths, labels, test_size=0.2, random_state=42)

IMG_SIZE = (64, 64)

def load_and_prepropress_images(image_path):
    image = cv2.imread(image_path)
    image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
    image = cv2.resize(image, IMG_SIZE)
    image = image / 255.0
    return image

train_images = np.array([load_and_prepropress_images(path) for path in train_paths])
val_images = np.array([load_and_prepropress_images(path) for path in val_paths])    

train_labels = np.array(train_labels)
val_labels = np.array(val_labels)

datagen = ImageDataGenerator(
    rotation_range=15,
    width_shift_range=0.1,
    height_shift_range=0.1,
    zoom_range=0.3,
    horizontal_flip=True,
    brightness_range=[0.5, 1.5]  # Simulates different lighting
)
datagen.fit(train_images)


model = Sequential([
    tf.keras.layers.Conv2D(32, (3,3), activation="relu", input_shape=(64, 64, 3)),
    tf.keras.layers.BatchNormalization(),
    tf.keras.layers.MaxPooling2D(2,2),
    tf.keras.layers.Dropout(0.4),

    tf.keras.layers.Conv2D(64, (3,3), activation="relu"),
    tf.keras.layers.BatchNormalization(),
    tf.keras.layers.MaxPooling2D(2,2),
    tf.keras.layers.Dropout(0.4),

    tf.keras.layers.Conv2D(128, (3,3), activation="relu"),
    tf.keras.layers.BatchNormalization(),
    tf.keras.layers.MaxPooling2D(2,2),
    tf.keras.layers.Dropout(0.5),

    tf.keras.layers.Flatten(),
    tf.keras.layers.Dense(128, activation="relu"),
    tf.keras.layers.Dropout(0.5),
    tf.keras.layers.Dense(len(np.unique(labels)), activation="softmax")  # Kaç farklı tabela varsa ona göre çıktı boyutu
])

model.compile(optimizer="adam", loss="sparse_categorical_crossentropy", metrics=["accuracy"])

history = model.fit(
    train_images, train_labels,
    validation_data=(val_images, val_labels),
    epochs=10,
    batch_size=32
)

model.save("traffic_sign_model.h5")