package fr.geonature.commons.data.helper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [SQLiteSelectQueryBuilder].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class SQLiteSelectQueryBuilderTest {

    @Test
    fun testDefaultSelect() {
        // given a simple query builder
        var sqLiteQuery = SQLiteSelectQueryBuilder.from("user")
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT *
            FROM user
        """.trimIndent(),
            sqLiteQuery.sql
        )

        // given a simple query builder with alias defined for table
        sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT *
            FROM user u
        """.trimIndent(),
            sqLiteQuery.sql
        )
    }

    @Test
    fun testSelectWithColumns() {
        // given a simple query builder with one column selected
        var sqLiteQuery = SQLiteSelectQueryBuilder.from("user")
            .column("email")
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT email
            FROM user
        """.trimIndent(),
            sqLiteQuery.sql
        )

        // given a simple query builder with one column with alias selected
        sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .column(
                "u.email",
                "user_email"
            )
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email AS user_email
            FROM user u
        """.trimIndent(),
            sqLiteQuery.sql
        )

        // given a simple query builder with some columns selected
        sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .columns(
                Pair(
                    "u.email",
                    null
                ),
                Pair(
                    "u.login",
                    null
                )
            )
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login
            FROM user u
        """.trimIndent(),
            sqLiteQuery.sql
        )

        // given a simple query builder with some columns selected
        sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .column("u.email")
            .column("u.login")
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login
            FROM user u
        """.trimIndent(),
            sqLiteQuery.sql
        )
    }

    @Test
    fun testSelectDistinct() {
        // given a simple query builder with no column selected with distinct
        var sqLiteQuery = SQLiteSelectQueryBuilder.from("user")
            .distinct()
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT *
            FROM user
        """.trimIndent(),
            sqLiteQuery.sql
        )

        // given a simple query builder with some columns selected with distinct
        sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .distinct()
            .column("u.email")
            .column("u.login")
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT DISTINCT u.email, u.login
            FROM user u
        """.trimIndent(),
            sqLiteQuery.sql
        )
    }

    @Test
    fun testFrom() {
        // given a simple query builder with two tables selected
        val sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .from(
                "profile",
                "p"
            )
            .column("u.email")
            .column("u.login")
            .column("p.role")
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login, p.role
            FROM user u, profile p
        """.trimIndent(),
            sqLiteQuery.sql
        )
    }

    @Test
    fun testJoin() {
        // given a simple query builder with left join
        var sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .column("u.id")
            .column("u.email")
            .column("u.login")
            .column("p.role")
            .leftJoin(
                "profile",
                "p.user_id = u.id",
                "p"
            )
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.id, u.email, u.login, p.role
            FROM user u
            LEFT JOIN profile AS p ON p.user_id = u.id
        """.trimIndent(),
            sqLiteQuery.sql
        )

        // given a simple query builder with joins and params
        sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .column("u.id")
            .column("u.email")
            .column("u.login")
            .column("p.role")
            .join(
                SQLiteSelectQueryBuilder.JoinOperator.DEFAULT,
                "group",
                "g.name = ?",
                "g",
                "admin"
            )
            .join(
                SQLiteSelectQueryBuilder.JoinOperator.LEFT_OUTER,
                "user_group",
                "ug.group_id = g.id AND ug.user_id = u.id",
                "ug"
            )
            .leftJoin(
                "profile",
                "p.user_id = u.id",
                "p"
            )
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.id, u.email, u.login, p.role
            FROM user u
            JOIN group AS g ON g.name = ?
            LEFT OUTER JOIN user_group AS ug ON ug.group_id = g.id AND ug.user_id = u.id
            LEFT JOIN profile AS p ON p.user_id = u.id
        """.trimIndent(),
            sqLiteQuery.sql
        )
        assertEquals(
            1,
            sqLiteQuery.argCount
        )
    }

    @Test
    fun testWhere() {
        // given a simple query builder with where clause
        var sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .column("u.email")
            .column("u.login")
            .where(
                "u.email = ?",
                "user@mail.com"
            )
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login
            FROM user u
            WHERE u.email = ?
        """.trimIndent(),
            sqLiteQuery.sql
        )
        assertEquals(
            1,
            sqLiteQuery.argCount
        )

        // given a simple query builder with simple and where clause
        sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .column("u.email")
            .column("u.login")
            .andWhere(
                "u.email = ?",
                arrayOf("user@mail.com")
            )
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login
            FROM user u
            WHERE (u.email = ?)
        """.trimIndent(),
            sqLiteQuery.sql
        )
        assertEquals(
            1,
            sqLiteQuery.argCount
        )

        // given a simple query builder with simple or where clause
        sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .column("u.email")
            .column("u.login")
            .andWhere(
                "u.email = ?",
                "user@mail.com"
            )
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login
            FROM user u
            WHERE (u.email = ?)
        """.trimIndent(),
            sqLiteQuery.sql
        )
        assertEquals(
            1,
            sqLiteQuery.argCount
        )

        // given a simple query builder with and where clauses
        sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .column("u.email")
            .column("u.login")
            .andWhere(
                "u.email = ?",
                "user@mail.com"
            )
            .andWhere(
                "u.login = ?",
                "user"
            )
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login
            FROM user u
            WHERE (u.email = ?)
              AND (u.login = ?)
        """.trimIndent(),
            sqLiteQuery.sql
        )
        assertEquals(
            2,
            sqLiteQuery.argCount
        )

        // given a simple query builder with and/or where clauses
        sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .column("u.email")
            .column("u.login")
            .andWhere(
                "u.email = ?",
                "user@mail.com"
            )
            .orWhere("u.login = 'user'")
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login
            FROM user u
            WHERE (u.email = ?)
              OR (u.login = 'user')
        """.trimIndent(),
            sqLiteQuery.sql
        )
        assertEquals(
            1,
            sqLiteQuery.argCount
        )

        // given a simple query builder with wheres clauses
        sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .column("u.email")
            .column("u.login")
            .andWhere(
                "u.email = ?",
                arrayOf("user@mail.com")
            )
            .where(
                "u.login = ?",
                arrayOf("user")
            )
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login
            FROM user u
            WHERE u.login = ?
        """.trimIndent(),
            sqLiteQuery.sql
        )
        assertEquals(
            1,
            sqLiteQuery.argCount
        )

        // given a simple query builder with wheres clauses
        sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .column("u.email")
            .column("u.login")
            .orWhere(
                "u.email = ?",
                arrayOf("user@mail.com")
            )
            .where(
                "u.login = ?",
                arrayOf("user")
            )
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login
            FROM user u
            WHERE u.login = ?
        """.trimIndent(),
            sqLiteQuery.sql
        )
        assertEquals(
            1,
            sqLiteQuery.argCount
        )
    }

    @Test
    fun testWhereWithNullArgs() {
        // given a simple query builder with where clause
        val sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .column("u.email")
            .column("u.login")
            .where(
                "u.email = ?",
                null
            )
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login
            FROM user u
            WHERE u.email = ?
        """.trimIndent(),
            sqLiteQuery.sql
        )
        assertEquals(
            1,
            sqLiteQuery.argCount
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testGroupByFromNonExistingColumn() {
        SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .column("u.email")
            .groupBy("login")
            .build()
    }

    @Test
    fun testGroupBy() {
        // given a simple query builder with group by clause
        val sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .column("u.email")
            .column(
                "u.login",
                "login"
            )
            .groupBy("login")
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login AS login
            FROM user u
            GROUP BY login
        """.trimIndent(),
            sqLiteQuery.sql
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testHavingWithNoGroupBy() {
        // given a simple query builder with having clause with no group by clause defined
        SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .leftJoin(
                "input",
                "i.user_id = p.id",
                "i"
            )
            .column("u.email")
            .column("u.login")
            .having("SUM(i.id) > 0")
            .build()
    }

    @Test
    fun testHaving() {
        // given a simple query builder with having clause
        val sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .leftJoin(
                "input",
                "i.user_id = p.id",
                "i"
            )
            .column("u.email")
            .column("u.login")
            .column("SUM(i.id)")
            .groupBy("u.email")
            .having("SUM(i.id) > 0")
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login, SUM(i.id)
            FROM user u
            LEFT JOIN input AS i ON i.user_id = p.id
            GROUP BY u.email
            HAVING SUM(i.id) > 0
        """.trimIndent(),
            sqLiteQuery.sql
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testOrderByFromNonExistingColumnName() {
        // given a simple query builder with order by clause from non existing column
        SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .leftJoin(
                "input",
                "i.user_id = p.id",
                "i"
            )
            .column("u.login")
            .orderBy("u.email")
            .build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testOrderByFromNonExistingColumnAlias() {
        // given a simple query builder with order by clause from non existing column
        SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .leftJoin(
                "input",
                "i.user_id = p.id",
                "i"
            )
            .column("u.email")
            .column("u.login")
            .orderBy("count")
            .build()
    }

    @Test
    fun testOrderByAsc() {
        // given a simple query builder with order by clause
        val sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .innerJoin(
                "input",
                "i.user_id = p.id",
                "i"
            )
            .column("u.email")
            .column("u.login")
            .column(
                "SUM(i.id)",
                "count"
            )
            .orderBy("count")
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login, SUM(i.id) AS count
            FROM user u
            INNER JOIN input AS i ON i.user_id = p.id
            ORDER BY count ASC
        """.trimIndent(),
            sqLiteQuery.sql
        )
    }

    @Test
    fun testOrderByAscCaseNonSensitive() {
        // given a simple query builder with order by clause
        val sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .column("u.email")
            .column("u.login")
            .orderBy(
                "u.email",
                SQLiteSelectQueryBuilder.OrderingTerm.ASC,
                false
            )
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login
            FROM user u
            ORDER BY u.email COLLATE NOCASE ASC
        """.trimIndent(),
            sqLiteQuery.sql
        )
    }

    @Test
    fun testOrderByDesc() {
        // given a simple query builder with order by clause
        val sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .innerJoin(
                "input",
                "i.user_id = p.id",
                "i"
            )
            .column("u.email")
            .column("u.login")
            .column(
                "SUM(i.id)",
                "count"
            )
            .orderBy(
                "count",
                SQLiteSelectQueryBuilder.OrderingTerm.DESC
            )
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login, SUM(i.id) AS count
            FROM user u
            INNER JOIN input AS i ON i.user_id = p.id
            ORDER BY count DESC
        """.trimIndent(),
            sqLiteQuery.sql
        )
    }

    @Test
    fun testOrderByDescCaseNonSensitive() {
        // given a simple query builder with order by clause
        val sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .column("u.email")
            .column("u.login")
            .orderBy(
                "u.login",
                SQLiteSelectQueryBuilder.OrderingTerm.DESC,
                false
            )
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login
            FROM user u
            ORDER BY u.login COLLATE NOCASE DESC
        """.trimIndent(),
            sqLiteQuery.sql
        )
    }

    @Test
    fun testOrderByFromOrderByClause() {
        // given a simple query builder with order by clause
        val sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .column("u.email")
            .column("u.login")
            .orderBy(
                "u.login  asc"
            )
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login
            FROM user u
            ORDER BY u.login ASC
        """.trimIndent(),
            sqLiteQuery.sql
        )
    }

    @Test
    fun testOrderByFromOrderByClauseNonSensitive() {
        // given a simple query builder with order by clause
        val sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .column("u.email")
            .column("u.login")
            .orderBy(
                "u.email, u.login collate  nocase   desc"
            )
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login
            FROM user u
            ORDER BY u.email ASC, u.login COLLATE NOCASE DESC
        """.trimIndent(),
            sqLiteQuery.sql
        )
    }

    @Test
    fun testLimit() {
        // given a simple query builder with limit clause
        var sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .column("u.email")
            .column("u.login")
            .limit(10)
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login
            FROM user u
            LIMIT 10
        """.trimIndent(),
            sqLiteQuery.sql
        )

        // given a simple query builder with limit and offset clause
        sqLiteQuery = SQLiteSelectQueryBuilder.from(
            "user",
            "u"
        )
            .column("u.email")
            .column("u.login")
            .limit(
                10,
                3
            )
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT u.email, u.login
            FROM user u
            LIMIT 10, 3
        """.trimIndent(),
            sqLiteQuery.sql
        )
    }
}
