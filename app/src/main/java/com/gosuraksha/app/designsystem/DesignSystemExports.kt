package com.gosuraksha.app.designsystem

import com.gosuraksha.app.design.components.ButtonStyles
import com.gosuraksha.app.design.components.CardStyles
import com.gosuraksha.app.design.layouts.NavigationConfig
import com.gosuraksha.app.design.layouts.ScreenLayouts
import com.gosuraksha.app.design.tokens.ColorTokens
import com.gosuraksha.app.design.tokens.ElevationTokens
import com.gosuraksha.app.design.tokens.ShapeTokens
import com.gosuraksha.app.design.tokens.SpacingTokens
import com.gosuraksha.app.design.tokens.TypographyTokens

object DesignSystemExports {
    val colors = ColorTokens
    val typography = TypographyTokens
    val spacing = SpacingTokens
    val shapes = ShapeTokens
    val elevation = ElevationTokens
    val buttons = ButtonStyles
    val cards = CardStyles
    val navigation = NavigationConfig
    val layouts = ScreenLayouts
}
