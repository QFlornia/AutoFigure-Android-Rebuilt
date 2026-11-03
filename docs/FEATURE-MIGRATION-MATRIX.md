# Feature migration matrix

| Upstream capability / desired feature | v0.3.0 status | Notes |
|---|---|---|
| Text-to-Figure | Implemented (core) | Prompt-driven editable SVG generation |
| Paper-to-Figure | Partial | PDF text extraction + methodology prompt; no scanned-PDF OCR |
| Iterative refinement | Implemented (core) | JSON critique and bounded revision loop |
| SVG output | Implemented (core) | Extraction, basic validation, source editing, preview/export |
| mxGraph XML | Partial | draw.io mxfile wrapper embeds SVG as an image |
| Image enhancement | Partial | Generates a style-preserving enhancement prompt |
| Reference image | Partial | Vision payload support in transport; UI wiring and model-specific testing remain |
| Image generation | Not complete | Requires dedicated image API adapter; chat completions are not image generation |
| SAM3 segmentation | Not complete | Requires remote SAM3 API or separate mobile inference integration |
| RMBG background removal | Not complete | Requires model/provider adapter |
| SVG template reconstruction | Not complete | Needs segmentation masks, geometry extraction and layout reconstruction |
| Native mxGraph cells | Not complete | Current wrapper is not editable per-shape draw.io XML |
| Node-level SVG editor | Not complete | Current editor is source-level, not a graphical object canvas |
| PNG/PDF export | Not complete | SVG is primary output; raster/PDF renderer integration required |
| History / local projects | Implemented | Local app-private persistence |

A feature is not marked complete merely because a prompt or placeholder exists.
