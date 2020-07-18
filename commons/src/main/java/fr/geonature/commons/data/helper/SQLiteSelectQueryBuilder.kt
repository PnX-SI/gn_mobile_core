package fr.geonature.commons.data.helper

import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery

/**
 * Simple query builder to create SQL SELECT queries.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class SQLiteSelectQueryBuilder private constructor(private val tables: MutableSet<Pair<String, String?>>) {

    private var distinct: Boolean = false
    private val columns = mutableSetOf<Pair<String, String?>>()
    private val joinClauses = mutableListOf<Pair<String, Array<Any>?>>()
    private val wheres = mutableListOf<Pair<String, Array<Any?>?>>()
    private val groupBy = mutableSetOf<String>()
    private var having: String? = null
    private val orderBy = mutableSetOf<String>()
    private var limit: String = ""

    /**
     * Adds table name to select.
     *
     * @param tableName The table name to query.
     * @param alias The table alias to use (default: `null`).
     *
     * @return this
     */
    fun from(
        tableName: String,
        alias: String? = null
    ): SQLiteSelectQueryBuilder {
        tables.add(
            Pair(
                tableName,
                alias
            )
        )

        return this
    }

    /**
     * Adds DISTINCT keyword to the query.
     *
     * @return this
     */
    fun distinct(): SQLiteSelectQueryBuilder {
        this.distinct = true

        return this
    }

    /**
     * Sets the given list of columns as the columns that will be returned.
     *
     * @param column The column name that should be returned.
     *
     * @return this
     */
    fun columns(vararg column: Pair<String, String?>): SQLiteSelectQueryBuilder {
        this.columns.addAll(column)

        return this
    }

    /**
     * Adds the given column as the column that will be returned.
     *
     * @param columnName The column name that should be returned.
     * @param alias The column alias to use (default: `null`).
     *
     * @return this
     */
    fun column(
        columnName: String,
        alias: String? = null
    ): SQLiteSelectQueryBuilder {
        this.columns(
            Pair(
                columnName,
                alias
            )
        )

        return this
    }

    /**
     * Adds JOIN clause to the query.
     *
     * @return this
     */
    fun join(
        joinOperator: JoinOperator = JoinOperator.DEFAULT,
        tableName: String,
        joinConstraint: String,
        alias: String? = null,
        vararg bindArgs: Any
    ): SQLiteSelectQueryBuilder {
        this.joinClauses.add(
            Pair(
                "${joinOperator.operator.let { if (it.isBlank()) "" else "$it " }}JOIN $tableName${if (alias.isNullOrBlank()) "" else " AS $alias"} ON $joinConstraint",
                bindArgs.toList()
                    .toTypedArray()
            )
        )

        return this
    }

    /**
     * Adds INNER JOIN clause to the query.
     *
     * @return this
     */
    fun innerJoin(
        tableName: String,
        joinConstraint: String,
        alias: String? = null,
        vararg bindArgs: Any
    ): SQLiteSelectQueryBuilder {
        return join(
            JoinOperator.INNER,
            tableName,
            joinConstraint,
            alias,
            *bindArgs
        )
    }

    /**
     * Adds LEFT JOIN clause to the query.
     *
     * @return this
     */
    fun leftJoin(
        tableName: String,
        joinConstraint: String,
        alias: String? = null,
        vararg bindArgs: Any
    ): SQLiteSelectQueryBuilder {
        return join(
            JoinOperator.LEFT,
            tableName,
            joinConstraint,
            alias,
            *bindArgs
        )
    }

    /**
     * Sets the WHERE clause to the query.
     *
     * @return this
     */
    fun where(
        whereClause: String,
        vararg bindArgs: Any?
    ): SQLiteSelectQueryBuilder {
        with(this.wheres) {
            clear()
            add(
                Pair(
                    whereClause,
                    bindArgs.asList()
                        .toTypedArray()
                )
            )
        }

        return this
    }

    /**
     * Adds the WHERE clause with AND condition to the query.
     *
     * @return this
     */
    fun andWhere(
        whereClause: String,
        vararg bindArgs: Any?
    ): SQLiteSelectQueryBuilder {
        this.wheres.add(
            Pair(
                "${if (this.wheres.isEmpty()) "" else " AND "}($whereClause)",
                bindArgs.toList()
                    .toTypedArray()
            )
        )

        return this
    }

    /**
     * Adds the WHERE clause with OR condition to the query.
     *
     * @return this
     */
    fun orWhere(
        whereClause: String,
        vararg bindArgs: Any?
    ): SQLiteSelectQueryBuilder {
        this.wheres.add(
            Pair(
                "${if (this.wheres.isEmpty()) "" else " OR "}($whereClause)",
                bindArgs.toList()
                    .toTypedArray()
            )
        )

        return this
    }

    /**
     * Adds a GROUP BY statement.
     *
     * @param columnName The selected column name or alias of the GROUP BY statement.
     *
     * @return this
     */
    fun groupBy(vararg columnName: String): SQLiteSelectQueryBuilder {
        columnName.all {
            if (this.columns.none { pair -> pair.first == it || pair.second == it }) {
                throw IllegalArgumentException("No selected column found with name or alias '$it' on which to apply ORDER BY")
            }

            true
        }

        this.groupBy.addAll(columnName)

        return this
    }

    /**
     * Adds a HAVING statement.
     *
     * @param expression The having clause.
     *
     * @return this
     */
    fun having(expression: String): SQLiteSelectQueryBuilder {
        this.having = expression

        return this
    }

    /**
     * Adds an ORDER BY statement.
     *
     * @param expression The selected column name or alias or expression on which to apply order clause.
     * @param orderingTerm The ordering sort order (default: `ASC`).
     * @param caseSensitive whether the sorting is case sensitive or not (default: `true`)
     *
     * @return this
     */
    fun orderBy(
        expression: String,
        orderingTerm: OrderingTerm? = null,
        caseSensitive: Boolean? = null
    ): SQLiteSelectQueryBuilder {
        if (orderingTerm == null && caseSensitive == null) {
            this.orderBy.add(expression)
        }

        this.orderBy.add("${expression}${if (caseSensitive != false) "" else " COLLATE NOCASE"}${if (orderingTerm == null) "" else " ${orderingTerm.name}"}")

        return this
    }

    /**
     * Adds a LIMIT statement.
     *
     * @param limit The limit value.
     * @param offset The offset value.
     *
     * @return this
     */
    fun limit(
        limit: Number,
        offset: Number? = null
    ): SQLiteSelectQueryBuilder {
        this.limit = "LIMIT $limit${if (offset == null) "" else ", $offset"}"

        return this
    }

    /**
     * Creates the [SupportSQLiteQuery] that can be passed into
     * SupportSQLiteDatabase#query(SupportSQLiteQuery).
     *
     * @return a new query
     */
    fun build(): SupportSQLiteQuery {
        if (this.groupBy.isEmpty() && !this.having.isNullOrBlank()) {
            throw IllegalArgumentException("HAVING clauses are only permitted when using a GROUP BY clause")
        }

        val bindArgs = mutableListOf<Any?>()
        val selectedColumns =
            this.columns.joinToString(", ") { pair -> "${pair.first}${if (pair.second.isNullOrBlank()) "" else " AS ${pair.second}"}" }
                .ifBlank { "*" }
        val tables =
            this.tables.joinToString(", ") { pair -> "${pair.first}${if (pair.second.isNullOrBlank()) "" else " ${pair.second}"}" }
        val joinClauses = this.joinClauses.joinToString("\n") { pair ->
            pair.second?.toList()
                ?.also { bindArgs.addAll(it) }
            pair.first
        }
        val whereClauses = this.wheres.joinToString("\n ") { pair ->
            pair.second?.toList()
                ?.also { bindArgs.addAll(it) }
            pair.first
        }
            .let { if (it.isEmpty()) it else "WHERE $it" }
        val groupByClauses = this.groupBy.joinToString(", ")
            .let { if (it.isEmpty()) it else "GROUP BY $it" }
        val havingClause = if (this.having.isNullOrBlank()) "" else "HAVING ${this.having}"
        val orderByClauses =
            this.orderBy.joinToString(", ")
                .let { if (it.isEmpty()) it else "ORDER BY $it" }

        val sql = """
                |SELECT ${if (this.columns.isNotEmpty() && this.distinct) "DISTINCT " else ""}$selectedColumns
                |FROM $tables
                |$joinClauses
                |$whereClauses
                |$groupByClauses
                |$havingClause
                |$orderByClauses
                |${this.limit}
            """.trimMargin()
            .trim()
            .replace(
                "\n{2,}".toRegex(RegexOption.MULTILINE),
                "\n"
            )

        Log.d(
            TAG,
            "sql:\n$sql\nargs: ${bindArgs.map { if (it is String) "'$it'" else it }}"
        )

        return SimpleSQLiteQuery(
            sql,
            bindArgs.toTypedArray()
        )
    }

    enum class JoinOperator(val operator: String) {
        DEFAULT(""),
        INNER("INNER"),
        LEFT("LEFT"),
        LEFT_OUTER("LEFT OUTER");
    }

    enum class OrderingTerm {
        ASC,
        DESC
    }

    companion object {

        private val TAG = SQLiteSelectQueryBuilder::class.java.name

        /**
         * Creates a query for the given table name.
         *
         * @param tableName The table name to query.
         * @param alias The table alias to use (default: `null`).
         *
         * @return A builder to create a query
         */
        fun from(
            tableName: String,
            alias: String? = null
        ): SQLiteSelectQueryBuilder {
            return SQLiteSelectQueryBuilder(
                mutableSetOf(
                    Pair(
                        tableName,
                        alias
                    )
                )
            )
        }
    }
}
