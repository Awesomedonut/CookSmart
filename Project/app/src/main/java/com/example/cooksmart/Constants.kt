package com.example.cooksmart

object Constants {
    const val CAMERA_PERMISSION_REQUEST_CODE = 101
    const val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 123
    const val AUDIO_TEXT_SIZE = 50
    const val PACKAGE_NAME = "com.example.cooksmart"
    const val INGRE_IMG_FILE_NAME = "ingre_img.jpg"
    const val SELECTED_INGREDIENTS = "selectedIngredients"
    const val GENERATE_BUTTON_PREFIX = "Tap to generate for:"
    const val MODEL_NAME = "gpt-4-1106-preview"
    const val TEXT_PROMPT = "Create a recipe along with cooking instructions based " +
            "on the ingredients provided, the instructions should be less than " +
            "5 steps, don't return special characters like " +
            "#, *, I need to read it, start with here is: "
    const val IMAGE_PROMPT = "Generate a beautiful dish with these details:"
    const val AVAILABLE_INGREDIENTS = "These ingredients are available:"
}
