package fr.geonature.commons.input

import android.os.Parcel
import fr.geonature.commons.data.model.InputObserver
import fr.geonature.commons.data.model.Taxon
import fr.geonature.commons.data.model.Taxonomy
import fr.geonature.commons.util.toDate
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Calendar

/**
 * Unit tests about [AbstractInput].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class InputTest {

    @Test
    fun testHasDefaultId() {
        // given an empty Input
        val input = DummyInput()

        // then
        assertTrue(input.id > 0)
    }

    @Test
    fun testHasDefaultStatus() {
        // given an empty Input
        val input = DummyInput()

        // then
        assertTrue(input.status == AbstractInput.Status.DRAFT)
    }

    @Test
    fun testSetDate() {
        // given an empty Input
        val input = DummyInput()

        // when setting the input date
        input.setDate("2016-10-28T08:15:00Z")

        // then
        assertEquals(
            toDate("2016-10-28T08:15:00Z"),
            input.date
        )
    }

    @Test
    fun testGetPrimaryInputObserver() {
        // given an empty Input
        val input = DummyInput()

        // then
        assertNull(input.getPrimaryObserverId())

        // when adding some input observers
        input.setAllInputObservers(
            listOf(
                InputObserver(
                    4,
                    "",
                    ""
                ),
                InputObserver(
                    3,
                    "",
                    ""
                ),
                InputObserver(
                    5,
                    "",
                    ""
                )
            )
        )

        // then
        assertEquals(
            4L,
            input.getPrimaryObserverId()
        )
    }

    @Test
    fun testGetInputObservers() {
        // given an empty Input
        val input = DummyInput()

        // then
        assertNull(input.getPrimaryObserverId())
        assertTrue(
            input
                .getAllInputObserverIds()
                .isEmpty()
        )
        assertTrue(
            input
                .getInputObserverIds()
                .isEmpty()
        )

        // when setting the primary input observer
        input.setPrimaryInputObserver(
            InputObserver(
                6,
                "",
                ""
            )
        )

        // then
        assertEquals(
            6L,
            input.getPrimaryObserverId()
        )
        assertArrayEquals(
            longArrayOf(6),
            input
                .getAllInputObserverIds()
                .toLongArray()
        )
        assertTrue(
            input
                .getInputObserverIds()
                .isEmpty()
        )

        // when adding some input observers
        input.also {
            it.addInputObserverId(3L)
            it.addInputObserverId(4L)
        }

        // then
        assertEquals(
            6L,
            input.getPrimaryObserverId()
        )
        assertArrayEquals(
            longArrayOf(
                6,
                3,
                4
            ),
            input
                .getAllInputObserverIds()
                .toLongArray()
        )
        assertArrayEquals(
            longArrayOf(
                3,
                4
            ),
            input
                .getInputObserverIds()
                .toLongArray()
        )
    }

    @Test
    fun testSetPrimaryInputObserver() {
        // given an empty Input
        val input = DummyInput()

        // when setting the primary input observer
        input.setPrimaryInputObserver(
            InputObserver(
                1,
                "",
                ""
            )
        )

        // then
        assertArrayEquals(
            longArrayOf(1),
            input
                .getAllInputObserverIds()
                .toLongArray()
        )

        // when adding additional input observers
        input.setAllInputObservers(
            listOf(
                InputObserver(
                    2,
                    "",
                    ""
                )
            )
        )

        // then
        assertArrayEquals(
            longArrayOf(
                1,
                2
            ),
            input
                .getAllInputObserverIds()
                .toLongArray()
        )

        // when adding additional input observers first and then setting the primary input observer
        input.clearAllInputObservers()
        input.setAllInputObservers(
            listOf(
                InputObserver(
                    6,
                    "",
                    ""
                )
            )
        )
        input.setPrimaryInputObserverId(5)

        // then
        assertArrayEquals(
            longArrayOf(
                5,
                6
            ),
            input
                .getAllInputObserverIds()
                .toLongArray()
        )
    }

    @Test
    fun testSetAllInputObserver() {
        // given an Input with existing input observers
        val input = DummyInput()
        input.setAllInputObservers(
            listOf(
                InputObserver(
                    4,
                    "",
                    ""
                ),
                InputObserver(
                    3,
                    "",
                    ""
                ),
                InputObserver(
                    5,
                    "",
                    ""
                )
            )
        )

        // when setting primary input observer
        input.setPrimaryInputObserver(
            InputObserver(
                1,
                "",
                ""
            )
        )

        // then
        assertArrayEquals(
            longArrayOf(
                1,
                4,
                3,
                5
            ),
            input
                .getAllInputObserverIds()
                .toLongArray()
        )

        // when adding additional input observers with one duplicate
        input.setAllInputObservers(
            listOf(
                InputObserver(
                    8,
                    "",
                    ""
                ),
                InputObserver(
                    1,
                    "",
                    ""
                ),
                InputObserver(
                    6,
                    "",
                    ""
                )
            )
        )

        // then
        assertArrayEquals(
            longArrayOf(
                1,
                8,
                6
            ),
            input
                .getAllInputObserverIds()
                .toLongArray()
        )
    }

    @Test
    fun testAddInputObserverId() {
        // given an Input with existing input observers
        val input = DummyInput()
        input.setAllInputObservers(
            listOf(
                InputObserver(
                    4,
                    "",
                    ""
                ),
                InputObserver(
                    3,
                    "",
                    ""
                ),
                InputObserver(
                    5,
                    "",
                    ""
                )
            )
        )

        // when adding existing input observer
        input.addInputObserverId(4)

        // then
        assertArrayEquals(
            longArrayOf(
                4,
                3,
                5
            ),
            input
                .getAllInputObserverIds()
                .toLongArray()
        )

        // when adding new input observer
        input.addInputObserverId(7)

        // then
        assertArrayEquals(
            longArrayOf(
                4,
                3,
                5,
                7
            ),
            input
                .getAllInputObserverIds()
                .toLongArray()
        )
    }

    @Test
    fun testAddInputTaxon() {
        // given an Input with no input taxa
        val input = DummyInput()

        // when adding new input taxon
        input.addInputTaxon(
            DummyInputTaxon(
                Taxon(
                    1234L,
                    "taxon_01",
                    Taxonomy(
                        "Animalia",
                        "Ascidies"
                    )
                )
            )
        )

        // then
        assertArrayEquals(
            arrayOf(
                DummyInputTaxon(
                    Taxon(
                        1234L,
                        "taxon_01",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            ),
            input
                .getInputTaxa()
                .toTypedArray()
        )

        // when adding existing input taxon
        input.addInputTaxon(
            DummyInputTaxon(
                Taxon(
                    1234L,
                    "taxon_02",
                    Taxonomy(
                        "Animalia",
                        "Ascidies"
                    )
                )
            )
        )

        // then
        assertArrayEquals(
            arrayOf(
                DummyInputTaxon(
                    Taxon(
                        1234L,
                        "taxon_02",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            ),
            input
                .getInputTaxa()
                .toTypedArray()
        )
    }

    @Test
    fun testRemoveInputTaxon() {
        // given an Input with some input taxa
        val input = DummyInput().apply {
            addInputTaxon(
                DummyInputTaxon(
                    Taxon(
                        1234L,
                        "taxon_01",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            )
            addInputTaxon(
                DummyInputTaxon(
                    Taxon(
                        1236L,
                        "taxon_03",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            )
            addInputTaxon(
                DummyInputTaxon(
                    Taxon(
                        1235L,
                        "taxon_02",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            )
        }

        // when remove input taxon different from current selected input taxon
        input.removeInputTaxon(1236L)

        // then
        assertEquals(
            Taxon(
                1235L,
                "taxon_02",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                )
            ),
            input.getCurrentSelectedInputTaxon()?.taxon
        )
        assertArrayEquals(
            arrayOf(
                DummyInputTaxon(
                    Taxon(
                        1234L,
                        "taxon_01",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                ),
                DummyInputTaxon(
                    Taxon(
                        1235L,
                        "taxon_02",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            ),
            input
                .getInputTaxa()
                .toTypedArray()
        )

        // when remove the current selected input taxon
        input.removeInputTaxon(1235L)

        // then
        assertNull(input.getCurrentSelectedInputTaxon())
        assertArrayEquals(
            arrayOf(
                DummyInputTaxon(
                    Taxon(
                        1234L,
                        "taxon_01",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            ),
            input
                .getInputTaxa()
                .toTypedArray()
        )
    }

    @Test
    fun testCurrentSelectedInputTaxon() {
        // given an Input with no input data
        val input = DummyInput()

        // when fetching the current selected input taxon
        // then
        assertNull(input.getCurrentSelectedInputTaxon())

        // when setting the current selected input taxon with non existing input taxon id
        input.setCurrentSelectedInputTaxonId(2L)

        // then
        assertNull(input.getCurrentSelectedInputTaxon())

        // when adding some input Taxa
        input.apply {
            addInputTaxon(
                DummyInputTaxon(
                    Taxon(
                        1234L,
                        "taxon_01",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            )
            addInputTaxon(
                DummyInputTaxon(
                    Taxon(
                        1236L,
                        "taxon_03",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            )
            addInputTaxon(
                DummyInputTaxon(
                    Taxon(
                        1235L,
                        "taxon_02",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            )
        }

        // then
        assertEquals(
            Taxon(
                1235L,
                "taxon_02",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                )
            ),
            input.getCurrentSelectedInputTaxon()?.taxon
        )

        // when setting the current selected input taxon
        input.setCurrentSelectedInputTaxonId(1236L)

        // then
        assertEquals(
            Taxon(
                1236L,
                "taxon_03",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                )
            ),
            input.getCurrentSelectedInputTaxon()?.taxon
        )
    }

    @Test
    fun testLastAddedInputTaxon() {
        // given an Input with no input data
        val input = DummyInput()

        // when fetching the last added input taxon
        // then
        assertNull(input.getLastAddedInputTaxon())

        // when adding some input taxa
        input.apply {
            addInputTaxon(
                DummyInputTaxon(
                    Taxon(
                        1234L,
                        "taxon_01",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            )
            addInputTaxon(
                DummyInputTaxon(
                    Taxon(
                        1236L,
                        "taxon_03",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            )
            addInputTaxon(
                DummyInputTaxon(
                    Taxon(
                        1235L,
                        "taxon_02",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            )
        }

        // then
        assertEquals(
            Taxon(
                1235L,
                "taxon_02",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                )
            ),
            input.getLastAddedInputTaxon()?.taxon
        )
    }

    @Test
    fun testClearCurrentSelectedInputTaxonId() {
        // given an Input with no input data
        val input = DummyInput()

        // when adding some input taxa
        input.apply {
            addInputTaxon(
                DummyInputTaxon(
                    Taxon(
                        1234L,
                        "taxon_01",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            )
            addInputTaxon(
                DummyInputTaxon(
                    Taxon(
                        1236L,
                        "taxon_03",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            )
            addInputTaxon(
                DummyInputTaxon(
                    Taxon(
                        1235L,
                        "taxon_02",
                        Taxonomy(
                            "Animalia",
                            "Ascidies"
                        )
                    )
                )
            )
        }

        // then
        assertEquals(
            Taxon(
                1235L,
                "taxon_02",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                )
            ),
            input.getCurrentSelectedInputTaxon()?.taxon
        )

        // when clearing the current selected input taxon
        input.clearCurrentSelectedInputTaxon()

        // then
        assertNull(input.getCurrentSelectedInputTaxon())
    }

    @Test
    fun testParcelable() {
        // given an Input
        val input = DummyInput().apply {
            id = 1234
            date = Calendar.getInstance().time
            datasetId = 17
            setAllInputObservers(
                listOf(
                    InputObserver(
                        1,
                        "",
                        ""
                    ),
                    InputObserver(
                        2,
                        "",
                        ""
                    )
                )
            )
            setInputTaxa(
                listOf(
                    DummyInputTaxon(
                        Taxon(
                            1234,
                            "taxon_01",
                            Taxonomy(
                                "Animalia",
                                "Ascidies"
                            )
                        )
                    )
                )
            )
        }

        // when we obtain a Parcel object to write the Taxon instance to it
        val parcel = Parcel.obtain()
        input.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            input,
            DummyInput.createFromParcel(parcel)
        )
    }
}
