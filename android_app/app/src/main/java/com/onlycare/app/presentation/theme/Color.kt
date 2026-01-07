package com.onlycare.app.presentation.theme

import androidx.compose.ui.graphics.Color

// ===== 🔷 PORTER-LIKE PREMIUM BLUE THEME - FLAT DESIGN (NO GRADIENTS) =====
// Palette (from your codes):
// - Denim:        #0D45D2 (Primary buttons/selected)
// - Persian Blue: #19389D (PrimaryDark top bars/headers)
// - Danube:       #5E80CE (PrimaryLight highlights)
// - Cloud Burst:  #1F2C55 (TextPrimary)
// - Mischka:      #D4D9E2 (Border)

// PRIMARY COLORS (Premium Blue - Flat)
val Primary = Color(0xFF0D45D2)        // Denim - main brand / CTA
val PrimaryDark = Color(0xFF19389D)    // Persian blue - top bars / headers
val PrimaryLight = Color(0xFF5E80CE)   // Danube - highlights/badges

// BACKGROUND & SURFACE COLORS (Clean Blue-White)
val Background = Color(0xFFFFFFFF)     // Pure white background
val Surface = Color(0xFFF7F9FF)        // Subtle blue-white for cards
val Card = Color(0xFFFFFFFF)           // White cards
val CardBackground = Color(0xFFFFFFFF) // White card background

// TEXT COLORS (Premium Navy)
val TextPrimary = Color(0xFF1F2C55)    // Cloud burst - primary text
val TextSecondary = Color(0xFF475569)  // Slate - secondary text
val TextTertiary = Color(0xFF64748B)   // Slate - tertiary labels
val TextDisabled = Color(0xFFD4D9E2)   // Mischka - disabled state

// BORDERS & DIVIDERS (Refined)
val Border = Color(0xFFD4D9E2)         // Mischka - borders
val BorderSecondary = Color(0xFFE9EDF5)// Very subtle divider
val BorderLight = Color(0xFFE9EDF5)    // Alias for subtle borders
val BorderAccent = Color(0xFF0D45D2)   // Denim accent borders

// ACCENT COLORS (Blue family)
val AccentViolet = Color(0xFF5E80CE)   // Use Danube for accents (keeps blue family)
val AccentLavender = Color(0xFFDBEAFE) // Soft ice blue background tint

// Aliases for clarity (matching theme names)
val ThemeBackground = Background
val ThemeSurface = Surface
val ThemeBorder = Border

// Legacy Support (keep for backward compatibility)
val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val PureBlack = Color(0xFF000000)
val SurfaceBlack = Color(0xFF0A0A0A)
val CardBlack = Color(0xFF121212)
val ElevatedSurface = Color(0xFF1A1A1A)
val BorderPrimary = Color(0xFF333333)
val DarkGray = Color(0xFF121212)
val MediumGray = Color(0xFF1E1E1E)
val LightGray = Color(0xFF2C2C2C)
val TextGray = Color(0xFFB3B3B3)
val DividerGray = Color(0xFF333333)

// STATUS COLORS (Luxury Flat)
val StatusOnline = Color(0xFF10B981)   // Emerald green
val StatusOffline = Color(0xFF71717A)  // Medium gray
val StatusBusy = Color(0xFFF59E0B)     // Amber

// FUNCTIONAL COLORS (Premium Flat - No Gradients)
val AccentWhite = Color(0xFFFFFFFF)
val SuccessGreen = Color(0xFF10B981)   // Emerald - flat
val ErrorRed = Color(0xFFEF4444)       // Ruby red - flat
val WarningOrange = Color(0xFFF59E0B)  // Amber - flat
val InfoBlue = Color(0xFF5E80CE)       // Danube - flat info color

// CALL COLORS (Premium Flat)
val CallGreen = Color(0xFF10B981)      // Emerald for accept
val CallRed = Color(0xFFEF4444)        // Ruby for reject
val OnlineGreen = Color(0xFF10B981)    // Emerald for online status



