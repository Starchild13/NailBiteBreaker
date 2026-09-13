package com.nailbitebreaker.ui

sealed class Screen(val route: String) {
    object Main : Screen("home")
    object Paywall : Screen("tip_jar")
    object Coach : Screen("coach")
    object Progress : Screen("progress")
    object Breathe : Screen("breathe")
    object Social : Screen("social_chat")
    object ReactionGame : Screen("reaction_game")
    object PatternGame : Screen("pattern_game")
}
