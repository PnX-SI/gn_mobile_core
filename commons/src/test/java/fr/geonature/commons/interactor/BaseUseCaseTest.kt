package fr.geonature.commons.interactor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.CoroutineTestRule
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests about [BaseUseCase].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class BaseUseCaseTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var useCase: DummyUseCase

    @Before
    fun setUp() {
        useCase = DummyUseCase()
    }

    @Test
    fun `should return 'Either' of use case type`() =
        runTest {
            val result = useCase.run(DummyParams(name = "test"))

            assertEquals(
                Either.Right(DummyType("test")),
                result
            )
        }

    @Test
    fun `should return correct data when executing use case`() = runTest(coroutineTestRule.testDispatcher) {
        var result: Either<Failure, DummyType>? = null

        val params = DummyParams(name = "test")
        val onResult = { dummyResult: Either<Failure, DummyType> -> result = dummyResult }

        useCase(
            params,
            CoroutineScope(coroutineTestRule.testDispatcher),
            onResult
        )

        assertEquals(
            Either.Right(DummyType("test")),
            result
        )
    }

    data class DummyType(val name: String)
    data class DummyParams(val name: String)

    private inner class DummyUseCase : BaseUseCase<DummyType, DummyParams>() {
        override suspend fun run(params: DummyParams) =
            Either.Right(DummyType(params.name))
    }
}