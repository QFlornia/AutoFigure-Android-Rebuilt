# AutoFigure Android — clean rebuild v0.4.0

Fresh Kotlin/Jetpack Compose implementation inspired by the AutoFigure scientific-illustration workflow.

## Build an APK
Open this folder in Android Studio (JDK 17, Android SDK 35), or push it to GitHub and run the included **Build AutoFigure Android** Actions workflow. The debug APK is uploaded as `AutoFigure-Android-debug-apk`.

## AI setup
Configure an OpenAI-compatible base URL, API key and model. The app calls `<base-url>/chat/completions`. The selected model must return SVG source code. For reference images, it must support vision input and data-URL images.

No dedicated application server is required for this architecture; the phone calls the model provider directly. Do not commit API keys. A key saved on a personal phone can still be extracted by someone with sufficient device access.

## Implemented
Text-to-SVG generation, AI critique/revision loop, reference image multimodal input, text-based PDF extraction, local history, SVG source editor/preview/export, draw.io SVG-image wrapper export, enhancement prompt generation.

## Not yet implemented
SAM3/RMBG, automatic vector reconstruction, editable native draw.io cells, node-level canvas editor, OCR, dedicated image-generation endpoints and high-resolution PNG/PDF export. See `docs/IMPLEMENTATION-STATUS.md`.

Upstream source and attribution notices are preserved in `licenses/`.
