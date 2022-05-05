package fr.geonature.commons.fp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * Unit tests about [Either].
 */
class EitherTest {
    @Test
    fun `Either Right should return correct type`() {
        val result = Either.Right("right_value")

        assertTrue(result.isRight)
        assertFalse(result.isLeft)

        result.fold({},
            { right ->
                assertEquals(
                    "right_value",
                    right
                )
            })
    }

    @Test
    fun `Either Left should return correct type`() {
        val result = Either.Left("left_value")

        assertFalse(result.isRight)
        assertTrue(result.isLeft)

        result.fold({ left ->
            assertEquals(
                "left_value",
                left
            )
        },
            {})
    }

    @Test
    fun `Either orNull should return value if it is Right type`() {
        val result = Either
            .Right("right_value")
            .orNull()
        assertEquals(
            "right_value",
            result
        )
    }

    @Test
    fun `Either orNull should return null value if it is Left type`() {
        val result = Either
            .Left("left_value")
            .orNull<String, Nothing>()
        assertNull(result)
    }

    @Test
    fun `Either getOrElse should ignore default value if it is Right type`() {
        val success = "Success"
        val result = Either
            .Right(success)
            .getOrElse { "Default" }

        assertEquals(
            success,
            result
        )
    }

    @Test
    fun `Either getOrElse should return default value if it is Left type`() {
        assertEquals("default",
            Either
                .Left("failure")
                .getOrElse { "default" })
        assertNull(
            Either
                .Left("failure")
                .getOrElse { null })
    }

    @Test
    fun `Either getOrHandle should return value`() {
        assertEquals(12,
            Either
                .Right(12)
                .getOrHandle { 17 })
        assertEquals(17,
            Either
                .Left(12)
                .getOrHandle { it + 5 })
    }

    @Test
    fun `Given fold is called, when Either is Right, applies fnR and returns its result`() {
        val either = Either.Right("Success")
        val result = either.fold({ fail("Shouldn't be executed") }) { 5 }

        assertEquals(
            5,
            result
        )
    }

    @Test
    fun `Given fold is called, when Either is Left, applies fnL and returns its result`() {
        val either = Either.Left(12)
        val foldResult = "fold_result"
        val result = either.fold({ foldResult }) { fail("Shouldn't be executed") }

        assertEquals(
            foldResult,
            result
        )
    }

    @Test
    fun `Given flatMap is called, when Either is Right, applies function and returns new Either`() {
        val either = Either.Right("Success")
        val result = either.flatMap {
            assertEquals(
                "Success",
                it
            )
            Either.Left("Error")
        }

        assertEquals(
            Either.Left("Error"),
            result
        )
        assertTrue(result.isLeft)
    }

    @Test
    fun `Given flatMap is called, when Either is Left, doesn't invoke function and returns original Either`() {
        val either = Either.Left(12)
        val result = either.flatMap { Either.Right(20) }

        assertTrue(result.isLeft)
        assertEquals(
            either,
            result
        )
    }

    @Test
    fun `Given onFailure is called, when Either is Right, doesn't invoke function and returns original Either`() {
        val success = "Success"
        val either = Either.Right(success)
        val result = either.onFailure { fail("Shouldn't be executed") }

        assertEquals(
            either,
            result
        )
        assertEquals(success,
            either.getOrElse { "Failure" })
    }

    @Test
    fun `Given onFailure is called, when Either is Left, invokes function with left value and returns original Either`() {
        val either = Either.Left(12)
        var onFailureCalled = false
        val result = either.onFailure {
            assertEquals(
                12,
                it
            )
            onFailureCalled = true
        }

        assertEquals(
            either,
            result
        )
        assertTrue(onFailureCalled)
    }

    @Test
    fun `Given onSuccess is called, when Either is Right, invokes function with right value and returns original Either`() {
        val success = "Success"
        val either = Either.Right(success)
        var onSuccessCalled = false
        val result = either.onSuccess {
            assertEquals(
                success,
                it
            )
            onSuccessCalled = true
        }

        assertEquals(
            either,
            result
        )
        assertTrue(onSuccessCalled)
    }

    @Test
    fun `Given onSuccess is called, when Either is Left, doesn't invoke function and returns original Either`() {
        val either = Either.Left(12)
        val result = either.onSuccess { fail("Shouldn't be executed") }

        assertEquals(
            either,
            result
        )
    }

    @Test
    fun `Given map is called, when Either is Right, invokes function with right value and returns a new Either`() {
        val success = "Success"
        val resultValue = "Result"
        val either = Either.Right(success)
        val result = either.map {
            assertEquals(
                success,
                it
            )
            resultValue
        }

        assertEquals(
            Either.Right(resultValue),
            result
        )
    }

    @Test
    fun `Given map is called, when Either is Left, doesn't invoke function and returns original Either`() {
        val either = Either.Left(12)
        val result = either.map { Either.Right(20) }

        assertTrue(result.isLeft)
        assertEquals(
            either,
            result
        )
    }
}