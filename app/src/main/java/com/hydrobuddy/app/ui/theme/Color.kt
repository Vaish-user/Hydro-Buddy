package com.hydrobuddy.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// PHASE 5: MATERIAL 3 COLOR SYSTEM
// One accent color (Clean Sky Blue) + neutral surfaces
// ==========================================

// Primary Accent
val HydroAccent = Color(0xFF0284C7)         // Sky 600
val HydroAccentDark = Color(0xFF38BDF8)     // Sky 400 (for Dark Theme)
val HydroAccentContainerLight = Color(0xFFE0F2FE)
val HydroAccentContainerDark = Color(0xFF075985)

// Light Theme Neutrals
val M3LightBackground = Color(0xFFF8FAFC)   // Slate 50
val M3LightSurface = Color(0xFFFFFFFF)      // Pure white card
val M3LightSurfaceVariant = Color(0xFFF1F5F9) // Slate 100
val M3LightOutline = Color(0xFFE2E8F0)      // Slate 200 (1dp outline)
val M3LightOnSurface = Color(0xFF0F172A)    // Slate 900 (primary text)
val M3LightOnSurfaceVariant = Color(0xFF64748B) // Slate 500 (muted text)

// Dark Theme Neutrals
val M3DarkBackground = Color(0xFF0F172A)    // Slate 900
val M3DarkSurface = Color(0xFF1E293B)       // Slate 800 (card)
val M3DarkSurfaceVariant = Color(0xFF334155) // Slate 700
val M3DarkOutline = Color(0xFF334155)       // Slate 700 (1dp outline)
val M3DarkOnSurface = Color(0xFFF8FAFC)     // Slate 50 (primary text)
val M3DarkOnSurfaceVariant = Color(0xFF94A3B8) // Slate 400 (muted text)

// Status / Indicator colors
val StatusActive = Color(0xFF0D9488)        // Teal 600
val StatusPaused = Color(0xFFD97706)        // Amber 600
val StatusSleeping = Color(0xFF6366F1)      // Indigo 500

// Backward-compatible tokens mapped to clean flat solids
val FigmaBackground = M3LightBackground
val FigmaSurfaceWhite = M3LightSurface
val FigmaNavy = M3LightOnSurface
val FigmaSlate = M3LightOnSurfaceVariant
val FigmaLightSlate = M3LightOutline
val FigmaBorder = M3LightOutline
val FigmaBorderSubtle = M3LightOutline

val FigmaBlue = HydroAccent
val FigmaDarkBlue = Color(0xFF0369A1)
val FigmaTeal = Color(0xFF0D9488)
val FigmaSoftBlue = HydroAccentContainerLight
val FigmaProgressTrack = Color(0xFFE2E8F0)

val FigmaOrange = Color(0xFFD97706)
val FigmaOrangeText = Color(0xFF92400E)
val FigmaOrangeContainer = Color(0xFFFEF3C7)
val FigmaGreen = Color(0xFF059669)
val FigmaGreenContainer = Color(0xFFD1FAE5)
val FigmaGreenDot = Color(0xFF0D9488)

val FigmaGlassBg = M3LightSurface
val FigmaGlassBorder = M3LightOutline

// Phase 5 strictly removes gradients: replace with uniform solid brushes
val FigmaPrimaryGradient = Brush.linearGradient(listOf(HydroAccent, HydroAccent))
val FigmaPopularButtonGradient = Brush.linearGradient(listOf(HydroAccent, HydroAccent))
val FigmaPipAtmosphericGlow = Brush.radialGradient(listOf(Color.Transparent, Color.Transparent))

// Legacy Screen Aliases
val MintTealPrimary = HydroAccent
val MintTealDark = M3LightOnSurface
val MintPastelSecondary = Color(0xFF0D9488)
val MintSoftBackground = M3LightBackground
val PureWhiteSurface = M3LightSurface
val SpruceTextPrimary = M3LightOnSurface
val SageTextSecondary = M3LightOnSurfaceVariant
val SunshineAccent = FigmaOrange
val SunshineAccentDark = FigmaOrangeText

val MidnightNavy = M3LightOnSurface
val DeepOceanBlue = Color(0xFF0369A1)
val AquaBlue = HydroAccent
val SoftOceanCyan = Color(0xFF0D9488)
val AquaticMistTint = HydroAccentContainerLight
val CalmSlate = M3LightOnSurfaceVariant
val ActiveGreen = FigmaGreen
val AmberWarning = FigmaOrange
val BorderSubtleBlue = M3LightOutline

val SoftMintContainer = HydroAccentContainerLight
val SoftYellowContainer = FigmaOrangeContainer
val SoftCoralAccent = Color(0xFFF87171)
val BorderSubtle = M3LightOutline
val SurfaceVariant = M3LightSurfaceVariant
val SuccessGreen = FigmaGreen
