import cv2
import numpy as np
from sklearn.linear_model import RANSACRegressor
import matplotlib.pyplot as plt

def filter_lanes(image):
    hls = cv2.cvtColor(image, cv2.COLOR_BGR2HLS)

    # White mask (light intensity should be high)
    lower_white = np.array([0, 180, 0], dtype=np.uint8)
    upper_white = np.array([255, 255, 255], dtype=np.uint8)

    # Yellow mask (hue between ~10-40 works for most cases)
    lower_yellow = np.array([10, 60, 80], dtype=np.uint8)
    upper_yellow = np.array([40, 255, 255], dtype=np.uint8)

    # Apply masks
    white_mask = cv2.inRange(hls, lower_white, upper_white)
    yellow_mask = cv2.inRange(hls, lower_yellow, upper_yellow)

    # Combine both masks
    mask = cv2.bitwise_or(white_mask, yellow_mask)
    filtered = cv2.bitwise_and(image, image, mask=mask)
    
    return filtered



def preprocess_image(image):
    filtered = filter_lanes(image)
    gray = cv2.cvtColor(filtered, cv2.COLOR_BGR2GRAY)
    blurred = cv2.GaussianBlur(gray, (5, 5), 0)

    # Increase Canny thresholds to make edges stronger
    edges = cv2.Canny(blurred, 50, 150)  # Instead of (50, 150)
    return edges


def region_of_interest(image):
    height, width = image.shape[:2] 
    mask = np.zeros_like(image) # creates a mask with the same type and size

    region = np.array([
        (int( 0.05 * width), height),
        (int(0.45 * width), int(0.6 * height)),
        (int(0.55 * width), int(0.6 * height)),
        (width, height),
    ], dtype=np.int32) # defines a trapezoidal ROI
    cv2.fillPoly(mask, np.int32([region]), 255) 
    return cv2.bitwise_and(image, mask)

def find_lane_pixels(binary_image):
    histogram = np.sum(binary_image[binary_image.shape[0]//2:, :], axis=0) # returns the bottom view of the image
    midpoint = np.int32(histogram.shape[0] / 2) # finds the midpoints (important to find the left and right lane)
    leftx_base = np.argmax(histogram[:midpoint]) # finds the left lane
    rightx_base = np.argmax(histogram[midpoint:]) + midpoint # finds the right lane

    nwindows = 9
    margin = 100
    minpix = 50

    window_height = np.int32(binary_image.shape[0] / nwindows)

    nonzero = binary_image.nonzero()
    nonzeroy = np.array(nonzero[0])
    nonzerox = np.array(nonzero[1])

    leftx_current = leftx_base
    rightx_current = rightx_base

    left_lane_inds = []
    right_lane_inds = []

    for window in range(nwindows):
        win_y_low = binary_image.shape[0] - (window + 1) * window_height
        win_y_high = binary_image.shape[0] - window * window_height

        win_xleft_low = leftx_current - margin
        win_xleft_high = leftx_current + margin
        win_xright_low = rightx_current - margin
        win_xright_high = rightx_current + margin

        good_left_inds = ((nonzeroy >= win_y_low) & (nonzeroy < win_y_high) & (nonzerox >= win_xleft_low) & (nonzerox < win_xleft_high)).nonzero()[0]
        good_right_inds = ((nonzeroy >= win_y_low) & (nonzeroy < win_y_high) & (nonzerox >= win_xright_low) & (nonzerox < win_xright_high)).nonzero()[0]

        left_lane_inds.append(good_left_inds)
        right_lane_inds.append(good_right_inds)

        if len(good_left_inds) > minpix:
            leftx_current = np.int32(np.mean(nonzerox[good_left_inds]))
        if len(good_right_inds) > minpix:
            rightx_current = np.int32(np.mean(nonzerox[good_right_inds]))

    left_lane_inds = np.concatenate(left_lane_inds)
    right_lane_inds = np.concatenate(right_lane_inds)

    leftx = nonzerox[left_lane_inds]
    lefty = nonzeroy[left_lane_inds]
    rightx = nonzerox[right_lane_inds]
    righty = nonzeroy[right_lane_inds]

    return leftx, lefty, rightx, righty

def fit_polynomial(binary_image):
    nonzero = binary_image.nonzero() # finds the nonzero (white) values
    nonzero_y = nonzero[0]
    nonzero_x = nonzero[1]

    left_fit = np.polyfit(nonzero_y, nonzero_x, 2) # applies a polynomial filter (a, b, c coefficients)

    return left_fit

def fit_polynomial_ransac(binary_image):
    leftx, lefty, rightx, righty = find_lane_pixels(binary_image)

    if len(leftx) == 0 or len(lefty) == 0 or len(rightx) == 0 or len(righty) == 0:
        return None

    left_fit = np.polyfit(lefty, leftx, 2)
    right_fit = np.polyfit(righty, rightx, 2)

    return left_fit, right_fit

def check_lane_departure(lane_fit, car_x_position, width):
    lane_center = np.polyval(lane_fit, width//2)

    if abs(car_x_position - lane_center) > 50:
        print("Warning! Lane Departure Detected.")

def draw_lanes(image, left_fit, right_fit):
    """ Draw lane curves on the original image. """
    y_vals = np.linspace(0, image.shape[0]-1, num=100)
    
    if left_fit is not None:
        left_x_vals = left_fit[0] * (y_vals ** 2) + left_fit[1] * y_vals + left_fit[2]
    else:
        left_x_vals = np.zeros_like(y_vals)

    if right_fit is not None:
        right_x_vals = right_fit[0] * (y_vals ** 2) + right_fit[1] * y_vals + right_fit[2]
    else:
        right_x_vals = np.zeros_like(y_vals)
    
    # Convert to integer pixel coordinates
    left_pts = np.array([np.transpose(np.vstack([left_x_vals, y_vals]))], dtype=np.int32)
    right_pts = np.array([np.flipud(np.transpose(np.vstack([right_x_vals, y_vals])))], dtype=np.int32)
    
    lane_image = np.zeros_like(image)
    cv2.polylines(lane_image, [left_pts], isClosed=False, color=(0, 255, 0), thickness=15)
    cv2.polylines(lane_image, [right_pts], isClosed=False, color=(0, 255, 0), thickness=15)
    
    # Create a polygon for the lane area
    pts = np.vstack((left_pts, right_pts))
    pts = pts.reshape((-1, 1, 2))
    cv2.fillPoly(lane_image, [pts], (0, 255, 0))
    
    return cv2.addWeighted(image, 1, lane_image, 0.8, 0)

def process_frame(image):
    edges = preprocess_image(image)
    roi = region_of_interest(edges)
    leftx, lefty, rightx, righty = find_lane_pixels(roi)
    
    # Fit a polynomial using RANSAC
    poly_coeffs = fit_polynomial_ransac(roi)

    if poly_coeffs is None:
        return image
    
    left_fit, right_fit = poly_coeffs
    
    # Draw detected lanes on the original image
    result = draw_lanes(image, left_fit, right_fit)
    return result

# --- Video Capture and Processing ---
def main():
    cap = cv2.VideoCapture(0)  # 0 is usually the default camera

    if not cap.isOpened():
        print("Error: Could not open camera.")
        return

    while True:
        ret, frame = cap.read()  # Read a frame from the camera

        if not ret:
            print("Error: Could not read frame.")
            break

        processed_frame = process_frame(frame)  # Process the frame

        cv2.imshow("Lane Detection", processed_frame)  # Display the processed frame

        # Exit if the 'q' key is pressed
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    cap.release()  # Release the camera
    cv2.destroyAllWindows()  # Close all OpenCV windows

if __name__ == "__main__":
    main()
