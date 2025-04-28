package ees.dlc.application

sealed class Screen(val rout: String) {
    object Home: Screen("home_screen")
    object Timetable: Screen("timetable_screen")
    object StaffProfiles: Screen("staff_profiles_screen")
}