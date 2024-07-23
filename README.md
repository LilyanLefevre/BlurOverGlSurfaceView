# Blur over GlSurfaceView
This demo application showcases a custom ConstraintLayout that blurs its background using a GLSurfaceView that shows a camera preview. 
The layout dynamically updates to reflect the blurred view of the content behind it.

https://github.com/user-attachments/assets/26cbc9c2-d19e-4368-97cc-78e558892867

## Features
- Custom ConstraintLayout: **BlurredConstraintLayout** that creates a blurred effect based on the content behind it.
- Camera Preview Integration: Uses a GLSurfaceView to display a camera feed.
- Dynamic Blurring: Continuously updates the background with a blurred image of the content under the view.
- Rounded Corners with Border: The blurred background has rounded corners and a customizable border.

## Principle
Each time a frame is rendered on the GLSurfaceView, a variable stores it as a Bitmap. The **BlurredConstraintLayout** access this 
variable and extract only the part that is below it. The layout then apply a blur with RenderScript (it may be overkill, it's just for POC purpose) 
and convert it to a drawable to be able to set it as the background of the layout.

## How to use
- Just use it as any other layout in your .xml layout:
```xml
<com.lilyanlefevre.camerademo.blur.BlurredConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:id="@+id/blurredBackgroundLayout">
    <!-- Your other layout elements here -->
</com.lilyanlefevre.camerademo.blur.BlurredConstraintLayout>
```

- Then you have to give a reference to an object implementing the **BitmapProvider** interface that is able to 
provide an access to a bitmap representing the current image displayed:
```kotlin
val blurredLayout = findViewById<BlurredConstraintLayout>(R.id.blurredLayout)
blurredLayout.setBitmapProvider(bitmapProvider)
```

In this example, the **BitmapProvider** is the GLSurfaceView.Renderer as it has direct access to OpenGL.

## Limitation
In its current implementation and on my Samsung tablet with 4Go RAM and a Samsung Exynos 9810 Octo-Core CPU, the background blur seems laggy. 
This laggy effect increases with the number of **BlurredConstraintLayout** instances. However with my Samsung Galaxy Fold 4 with 12Go Ram and a Snapdragon 8+ Gen 1 it works fine (or least better! :D).

## Credits
The boilerplate to use Camera2 and OpenGL comes from [Guamola's Camera-GLSurfaceView](https://github.com/gumaola/Camera-GLSurfaceView).
