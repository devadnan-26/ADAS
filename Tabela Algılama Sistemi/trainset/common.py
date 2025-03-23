import cv2
import numpy as np

def preprocess_image(image_path, target_size=(64, 64)):
    """
    Resize the image to the target size and normalize pixel values.
    """
    image = cv2.imread(image_path)
    # Resize the image
    resized_image = cv2.resize(image, target_size)
    
    # Normalize pixel values to [0, 1]
    normalized_image = resized_image / 255.0
    
    # Add batch dimension
    normalized_image = np.expand_dims(normalized_image, axis=0)
    
    return normalized_image