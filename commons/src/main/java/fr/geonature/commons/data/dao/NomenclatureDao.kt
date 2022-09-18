package fr.geonature.commons.data.dao

import androidx.room.Dao
import androidx.room.Query
import fr.geonature.commons.data.entity.DefaultNomenclature
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureTaxonomy
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.data.entity.NomenclatureWithTaxonomy
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.SQLiteSelectQueryBuilder.JoinOperator.DEFAULT
import fr.geonature.commons.data.helper.SQLiteSelectQueryBuilder.OrderingTerm.ASC

/**
 * Data access object for [Nomenclature].
 *
 * @author S. Grimault
 */
@Dao
abstract class NomenclatureDao : BaseDao<Nomenclature>() {

    /**
     * Fetches all [Nomenclature] as default nomenclature values.
     *
     * @return a list of default [Nomenclature]
     */
    @Query(
        """SELECT n.*
        FROM ${Nomenclature.TABLE_NAME} n
        JOIN ${DefaultNomenclature.TABLE_NAME} ON ${DefaultNomenclature.TABLE_NAME}.${DefaultNomenclature.COLUMN_MODULE} = :module
            AND ${DefaultNomenclature.TABLE_NAME}.${DefaultNomenclature.COLUMN_NOMENCLATURE_ID} = n.${Nomenclature.COLUMN_ID}
        """
    )
    abstract suspend fun findAllDefaultNomenclatureValues(module: String): List<Nomenclature>

    /**
     * Fetches all nomenclature values matching given nomenclature type.
     *
     * @param mnemonic the nomenclature type as main filter
     *
     * @return a list of [Nomenclature]
     */
    @Query(
        """SELECT n.*
        FROM ${Nomenclature.TABLE_NAME} n
        JOIN ${NomenclatureType.TABLE_NAME} nt ON nt.${NomenclatureType.COLUMN_ID} = n.${Nomenclature.COLUMN_TYPE_ID}
            AND nt.${NomenclatureType.COLUMN_MNEMONIC} = :mnemonic
        ORDER BY n.${Nomenclature.COLUMN_DEFAULT_LABEL} ASC"""
    )
    abstract suspend fun findAllByNomenclatureType(mnemonic: String): List<Nomenclature>

    /**
     * Fetches all nomenclature values matching given nomenclature type and an optional taxonomy
     * kingdom and group.
     *
     * @param mnemonic the nomenclature type as main filter
     * @param kingdom the taxonomy kingdom ([Taxonomy.ANY] as default filter if not defined)
     * @param group the taxonomy group ([Taxonomy.ANY] as default filter if not defined)
     *
     * @return a list of [Nomenclature] matching given criteria
     */
    @Query(
        """SELECT n.*
        FROM ${Nomenclature.TABLE_NAME} n
        JOIN ${NomenclatureType.TABLE_NAME} nt ON nt.${NomenclatureType.COLUMN_ID} = n.${Nomenclature.COLUMN_TYPE_ID}
            AND nt.${NomenclatureType.COLUMN_MNEMONIC} = :mnemonic
        JOIN ${NomenclatureTaxonomy.TABLE_NAME} nta ON nta.${NomenclatureTaxonomy.COLUMN_NOMENCLATURE_ID} = n.${Nomenclature.COLUMN_ID}
            AND (
                (nta.${Taxonomy.COLUMN_KINGDOM} = CASE WHEN :kingdom IS NULL THEN '${Taxonomy.ANY}' ELSE :kingdom END AND nta.`${Taxonomy.COLUMN_GROUP}` = CASE WHEN :group IS NULL OR :kingdom IS NULL THEN '${Taxonomy.ANY}' ELSE :group END) OR
                (nta.${Taxonomy.COLUMN_KINGDOM} = CASE WHEN :kingdom IS NULL THEN '${Taxonomy.ANY}' ELSE :kingdom END AND nta.`${Taxonomy.COLUMN_GROUP}` = '${Taxonomy.ANY}') OR
                (nta.${Taxonomy.COLUMN_KINGDOM} = '${Taxonomy.ANY}' AND nta.`${Taxonomy.COLUMN_GROUP}` = '${Taxonomy.ANY}')
            )
        ORDER BY n.${Nomenclature.COLUMN_DEFAULT_LABEL} ASC"""
    )
    abstract suspend fun findAllByNomenclatureTypeAndByTaxonomy(
        mnemonic: String,
        kingdom: String? = null,
        group: String? = null
    ): List<Nomenclature>

    /**
     * Internal query builder for [NomenclatureWithTaxonomy].
     */
    inner class QB : BaseDao<Nomenclature>.QB() {

        init {
            selectQueryBuilder
                .columns(*Nomenclature.defaultProjection())
                .orderBy(
                    column(
                        Nomenclature.COLUMN_DEFAULT_LABEL,
                        entityTableName
                    ).second,
                    ASC
                )
        }

        fun withNomenclatureType(mnemonic: String? = null): QB {
            selectQueryBuilder
                .columns(*NomenclatureType.defaultProjection())
                .also {
                    val joinConstraint = "${
                        column(
                            NomenclatureType.COLUMN_ID,
                            NomenclatureType.TABLE_NAME
                        ).second
                    } = ${
                        column(
                            Nomenclature.COLUMN_TYPE_ID,
                            entityTableName
                        ).second
                    }${
                        if (mnemonic.isNullOrBlank()) ""
                        else " AND ${
                            column(
                                NomenclatureType.COLUMN_MNEMONIC,
                                NomenclatureType.TABLE_NAME
                            ).second
                        } = ?"
                    }"

                    if (mnemonic.isNullOrBlank()) {
                        it.join(
                            DEFAULT,
                            NomenclatureType.TABLE_NAME,
                            joinConstraint,
                            NomenclatureType.TABLE_NAME
                        )
                    } else {
                        it.join(
                            DEFAULT,
                            NomenclatureType.TABLE_NAME,
                            joinConstraint,
                            NomenclatureType.TABLE_NAME,
                            mnemonic
                        )
                    }
                }

            return this
        }

        fun withDefaultNomenclature(module: String?): QB {
            if (module.isNullOrBlank()) {
                return this
            }

            selectQueryBuilder
                .columns(*DefaultNomenclature.defaultProjection())
                .join(
                    DEFAULT,
                    DefaultNomenclature.TABLE_NAME,
                    "${
                        column(
                            DefaultNomenclature.COLUMN_NOMENCLATURE_ID,
                            DefaultNomenclature.TABLE_NAME
                        ).second
                    } = ${
                        column(
                            Nomenclature.COLUMN_ID,
                            entityTableName
                        ).second
                    } AND ${
                        column(
                            DefaultNomenclature.COLUMN_MODULE,
                            DefaultNomenclature.TABLE_NAME
                        ).second
                    } = ?",
                    DefaultNomenclature.TABLE_NAME,
                    module
                )

            return this
        }

        fun withTaxonomy(
            kingdom: String?,
            group: String?
        ): QB {
            if (kingdom.isNullOrBlank() && group.isNullOrBlank()) {
                return this
            }

            val filterKingdom = kingdom.takeUnless { it.isNullOrBlank() }
                ?: Taxonomy.ANY
            val filterGroup =
                group.takeUnless { it.isNullOrBlank() || filterKingdom == Taxonomy.ANY }
                    ?: Taxonomy.ANY

            selectQueryBuilder
                .columns(*NomenclatureTaxonomy.defaultProjection())
                .join(
                    DEFAULT,
                    NomenclatureTaxonomy.TABLE_NAME,
                    "${
                        column(
                            NomenclatureTaxonomy.COLUMN_NOMENCLATURE_ID,
                            NomenclatureTaxonomy.TABLE_NAME
                        ).second
                    } = ${
                        column(
                            Nomenclature.COLUMN_ID,
                            entityTableName
                        ).second
                    } AND (((${
                        column(
                            Taxonomy.COLUMN_KINGDOM,
                            NomenclatureTaxonomy.TABLE_NAME
                        ).second
                    } = ?) AND (${
                        column(
                            Taxonomy.COLUMN_GROUP,
                            NomenclatureTaxonomy.TABLE_NAME
                        ).second
                    } = ?)) OR ((${
                        column(
                            Taxonomy.COLUMN_KINGDOM,
                            NomenclatureTaxonomy.TABLE_NAME
                        ).second
                    } = ?) AND (${
                        column(
                            Taxonomy.COLUMN_GROUP,
                            NomenclatureTaxonomy.TABLE_NAME
                        ).second
                    } = ?)) OR ((${
                        column(
                            Taxonomy.COLUMN_KINGDOM,
                            NomenclatureTaxonomy.TABLE_NAME
                        ).second
                    } = ?) AND (${
                        column(
                            Taxonomy.COLUMN_GROUP,
                            NomenclatureTaxonomy.TABLE_NAME
                        ).second
                    } = ?)))",
                    NomenclatureTaxonomy.TABLE_NAME,
                    filterKingdom,
                    filterGroup,
                    filterKingdom,
                    Taxonomy.ANY,
                    Taxonomy.ANY,
                    Taxonomy.ANY
                )

            return this
        }
    }
}
