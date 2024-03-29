package fr.geonature.commons.data.dao

import androidx.room.Dao
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.SQLiteSelectQueryBuilder.JoinOperator.DEFAULT
import fr.geonature.commons.data.helper.SQLiteSelectQueryBuilder.OrderingTerm.ASC
import fr.geonature.commons.data.entity.DefaultNomenclature
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureTaxonomy
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.data.entity.NomenclatureWithTaxonomy
import fr.geonature.commons.data.entity.Taxonomy

/**
 * Data access object for [Nomenclature].
 *
 * @author S. Grimault
 */
@Dao
abstract class NomenclatureDao : BaseDao<Nomenclature>() {

    /**
     * Internal query builder for [NomenclatureWithTaxonomy].
     */
    inner class QB : BaseDao<Nomenclature>.QB() {

        init {
            selectQueryBuilder.columns(*Nomenclature.defaultProjection())
                .orderBy(
                    column(
                        Nomenclature.COLUMN_DEFAULT_LABEL,
                        entityTableName
                    ).second,
                    ASC
                )
        }

        fun withNomenclatureType(mnemonic: String? = null): QB {
            selectQueryBuilder.columns(*NomenclatureType.defaultProjection())
                .also {
                    val joinConstraint = "${column(
                        NomenclatureType.COLUMN_ID,
                        NomenclatureType.TABLE_NAME
                    ).second} = ${column(
                        Nomenclature.COLUMN_TYPE_ID,
                        entityTableName
                    ).second}${if (mnemonic.isNullOrBlank()) ""
                    else " AND ${column(
                        NomenclatureType.COLUMN_MNEMONIC,
                        NomenclatureType.TABLE_NAME
                    ).second} = ?"}"

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

            selectQueryBuilder.columns(*DefaultNomenclature.defaultProjection())
                .join(
                    DEFAULT,
                    DefaultNomenclature.TABLE_NAME,
                    "${column(
                        DefaultNomenclature.COLUMN_NOMENCLATURE_ID,
                        DefaultNomenclature.TABLE_NAME
                    ).second} = ${column(
                        Nomenclature.COLUMN_ID,
                        entityTableName
                    ).second} AND ${column(
                        DefaultNomenclature.COLUMN_MODULE,
                        DefaultNomenclature.TABLE_NAME
                    ).second} = ?",
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

            val filterKingdom = kingdom.takeUnless { it.isNullOrBlank() } ?: Taxonomy.ANY
            val filterGroup =
                group.takeUnless { it.isNullOrBlank() || filterKingdom == Taxonomy.ANY }
                    ?: Taxonomy.ANY

            selectQueryBuilder.columns(*NomenclatureTaxonomy.defaultProjection())
                .join(
                    DEFAULT,
                    NomenclatureTaxonomy.TABLE_NAME,
                    "${column(
                        NomenclatureTaxonomy.COLUMN_NOMENCLATURE_ID,
                        NomenclatureTaxonomy.TABLE_NAME
                    ).second} = ${column(
                        Nomenclature.COLUMN_ID,
                        entityTableName
                    ).second} AND (((${column(
                        Taxonomy.COLUMN_KINGDOM,
                        NomenclatureTaxonomy.TABLE_NAME
                    ).second} = ?) AND (${column(
                        Taxonomy.COLUMN_GROUP,
                        NomenclatureTaxonomy.TABLE_NAME
                    ).second} = ?)) OR ((${column(
                        Taxonomy.COLUMN_KINGDOM,
                        NomenclatureTaxonomy.TABLE_NAME
                    ).second} = ?) AND (${column(
                        Taxonomy.COLUMN_GROUP,
                        NomenclatureTaxonomy.TABLE_NAME
                    ).second} = ?)) OR ((${column(
                        Taxonomy.COLUMN_KINGDOM,
                        NomenclatureTaxonomy.TABLE_NAME
                    ).second} = ?) AND (${column(
                        Taxonomy.COLUMN_GROUP,
                        NomenclatureTaxonomy.TABLE_NAME
                    ).second} = ?)))",
                    NomenclatureTaxonomy.TABLE_NAME,
                    filterKingdom, filterGroup,
                    filterKingdom, Taxonomy.ANY,
                    Taxonomy.ANY, Taxonomy.ANY
                )

            return this
        }
    }
}
