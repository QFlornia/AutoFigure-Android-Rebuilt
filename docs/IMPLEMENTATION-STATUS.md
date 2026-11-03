# Implementation status — v0.4.0

Clean Android/Kotlin rebuild. The upstream AutoFigure repository is used as an algorithm/workflow reference; its old Android code is not used.

## Implemented in source
- Jetpack Compose Android application and local API configuration.
- OpenAI-compatible chat-completions transport.
- Text-to-SVG generation prompt and bounded AI critique/revision loop.
- Reference-image picker: selected image is decoded/re-encoded as JPEG and sent as multimodal `image_url` input with the generation request. The configured model must support vision.
- PDF text extraction/import (text-based PDFs; not OCR).
- Local project history.
- SVG extraction, basic safety preflight, source editor, WebView preview and SVG export.
- draw.io mxfile wrapper export (SVG embedded as one image object).
- Enhancement-prompt generation.
- SVG utility unit tests and GitHub Actions debug-APK workflow.

## Not complete / must not be represented as complete
- Dedicated image-generation API adapter (chat completion output is SVG source, not generated raster artwork).
- SAM3 segmentation, local or remote.
- RMBG/background removal.
- automatic icon detection/cropping and SVG template reconstruction.
- native mxGraph cell decomposition (current draw.io output embeds one SVG image).
- graphical node-level SVG editor with drag/resize/connector editing.
- scanned-PDF OCR.
- high-resolution PNG/PDF renderer and font embedding.
- provider-specific native Gemini image generation and non-OpenAI-compatible image APIs.
- encrypted API key storage, robust retry/backoff, cancellation and cost/token accounting.

## Runtime requirements
The endpoint must implement OpenAI-compatible `/chat/completions`. Reference-image input requires a vision-capable model and provider support for data-URL image inputs. API keys entered on a device are not secret from a determined device owner or malware; use restricted keys and quotas.

The project has not been built in this execution environment because Android SDK/Gradle are unavailable. Use the included GitHub Actions workflow to build a debug APK, then install and test it on a real device.
