package ru.taganhorn.svalka.android

import androidx.fragment.app.Fragment


abstract class AbstFragment : Fragment() {
    open fun allowBack(): Boolean = true
}