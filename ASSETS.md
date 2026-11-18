# HydroBuddy — Asset Credits & Attributions

This document lists the sources, tools, and attributions for all media, 3D models, textures, and animations bundled with HydroBuddy.

---

## 1. 3D Character Model: Pip

- **Asset Path**: `app/src/main/assets/models/pip.glb`
- **Description**: Low-poly animated water droplet mascot equipped with accessories (removable kitchen knife and water glass).
- **Creation & Pipeline**:
  - **Base Mesh & Concept**: Custom design generated with AI-assisted 3D base mesh reconstruction (HyperHuman Rodin / Gemini concepts).
  - **Modeling, Rigging & Shading**: Sculpted, retopologized, rigged, and animated in Blender.
  - **PBR Texturing**: Custom metallic/roughness PBR workflow with stylized vertex and texture colors.
  - **Animations Included**:
    - `A_IDLE`: Ambient breathing hover loop.
    - `B_WADDLE`: Expressive waddling walk-in cycle.
    - `C_DRINK`: Interactive hydration drinking animation.
    - `D_HAPPY`: Cheerful victory reaction.
    - `E_SAD`: Dejected reaction upon postponed reminders.
    - `F_CELEBRATING`: Full celebration milestone jump.

---

## 2. 2D Mascot Sprites & Expressions

- **Asset Path**: `app/src/main/assets/sprites/pip_master_spritesheet.png`
- **Metadata**: `app/src/main/assets/sprites/spritesheet_data.json`
- **Description**: Master 2D sprite atlas containing multi-angle expressions, mood faces, and HUD icon states (neutral, happy, grumpy, celebration, sleeping).
- **Format**: Optimized PNG atlas with coordinate mapping.

---

## 3. Splash Video & Motion Elements

- **Asset Path**: `app/src/main/assets/video/hydrobuddy_water_splash.mp4`
- **Description**: Fluid surface tension collapse motion choreographed for HydroBuddy's animated entrance.
- **Rendering**: Rendered and encoded with H.264 video compression for real-time hardware decoding via Android MediaCodec.

---

## 4. Audio & Fonts

- **Typography**: Google Fonts (Plus Jakarta Sans & Outfit) loaded via `androidx.compose.ui:ui-text-google-fonts`.
- **Icons**: Material Design Icons (Rounded variant) via `androidx.compose.material.icons.rounded`.

---

## 5. Usage & Rights

All 3D character models, rigs, textures, and sprites were custom designed and created for the HydroBuddy project. They are distributed for personal and educational use under the project license.
