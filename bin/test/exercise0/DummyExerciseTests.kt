package exercise0

import de.hpi.dbs2.exercise0.DummyExercise
import de.hpi.dbs2.exerciseframework.getChosenImplementation
import io.kotest.matchers.string.shouldMatch
import kotlin.test.Test

class DummyExerciseTests {
    @Test
    fun `yourGroupIdentifier returns a group identifier`() {
        getChosenImplementation<DummyExercise>(DummyExerciseJava(), DummyExerciseKotlin())
            .yourGroupIdentifier
            .shouldMatch("[A-Z]")
    }
}
