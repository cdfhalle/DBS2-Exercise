package de.hpi.dbs2.exerciseframework

import de.hpi.dbs2.ChosenImplementation

fun <T : Any> getChosenImplementation(vararg classes: Class<T>): Class<T> = classes.firstOrNull { clazz ->
    clazz.getAnnotation(ChosenImplementation::class.java)?.value ?: false
} ?: throw NoSubmissionImplementationChosen()

fun <T : Any> getChosenImplementation(vararg objects: T): T = objects.firstOrNull { obj ->
    obj.javaClass.getAnnotation(ChosenImplementation::class.java)?.value ?: false
} ?: throw NoSubmissionImplementationChosen()

class NoSubmissionImplementationChosen: IllegalStateException(
    "Chose an implementation for your exercise by setting @ChosenImplementation(true)"
)
