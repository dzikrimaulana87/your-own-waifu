# Chatbot with Emotion Settings and Personal Dataset

## Description
This project is a chatbot application that allows users to configure various personal settings, such as nickname and images for specific emotions. The app also supports the use of personal datasets, providing a more personalized experience for the user.

## Key Features

- **Nickname Settings**: Users can input and save their preferred nickname.
- **Emotion Image Upload**: Users can upload images for various emotion categories such as positive, flirty, supportive, etc.
- **Configuration Storage**: All settings made by the user, including nickname and images, will be saved for future use.
- **Use of Personal Dataset**: Users can upload their own dataset to enhance the chatbot experience and make it more personalized. The uploaded dataset will be used to train the model or adjust the chatbot's responses to fit the user's preferences.
- **User Notifications and Messages**: Clear messages are displayed to inform users about the status of their settings or errors, such as when an image cannot be found.

## Technologies Used

- **Android Studio**: The main platform for Android application development.
- **Java**: The programming language used for the app's logic.
- **SharedPreferences**: Used to store nickname settings and image configurations.
- **Intent & ActivityResultLauncher**: Used for handling image selection from the gallery.
- **Toast**: Used to display messages to the user about the status of their settings.

## How to Run the Project

Install the `app-debug.apk` file on your Android device.

## Usage

- **Nickname Settings**: On the main screen, input the desired nickname in the input field, then click **Save Settings** to store the settings.
  
- **Emotion Image Upload**: In the **Upload Emotion Images** section, users can upload images for different emotion categories (positive, flirty, supportive, etc.) based on their preferences.
  
- **Use of Personal Dataset**: In the **Upload Dataset** menu, users can upload their own dataset to personalize the chatbot's experience.

- **Reset All Data**: Users can reset all settings and datasets by clicking the **Reset All Data** button.

## Contribution

If you would like to contribute to this project, please create a pull request with a description of the features or improvements you've made.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
