package example

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

private val DBNAME = "mydb"

fun main(args: Array<String>) {
    Class.forName("org.h2.Driver")

    val conn = DriverManager.getConnection("jdbc:h2:mem:$DBNAME", "sa", "sa") // (2)

    conn.use {

        conn.createStatement().use { stmt ->
            with(stmt) {
                executeUpdate("create table users(id int primary key, name varchar(255))")
                executeUpdate("insert into users values(1, 'Hello')")
                executeUpdate("insert into users values(2, 'World')")
            }
        }

        printColumnNames(conn)

        printAliasNames(conn)
    }

}

private fun printColumnNames(conn: Connection) {
    conn.createStatement().use { stmt ->
        val rs = stmt.executeQuery("select * from users")

        getColumnNames(rs).forEach { name -> print("$name\t") }.also {
            println()
        }

        printRsRows(rs, getColumnNames(rs))
    }
}

private fun printAliasNames(conn: Connection) {
    conn.createStatement().use { stmt ->
        val rs = stmt.executeQuery("select id as id_alias, name as name_alias from users")
        getColumnAliasNames(rs).forEach { name -> print("$name\t") }.also {
            println()
        }

        printRsRows(rs, getColumnAliasNames(rs))
    }
}

private fun printRsRows(rs: ResultSet, columnNames: List<String>) {
    while (rs.next()) {
        columnNames.forEach { name ->
            print(rs.getObject(name).toString() + "\t")
        }
        println()
    }
}

private fun getColumnNames(rs: ResultSet): List<String> {
    val metaData = rs.metaData
    return (1..metaData.columnCount).map { metaData.getColumnName(it) }.toList()
}

private fun getColumnAliasNames(rs: ResultSet): List<String> {
    val metaData = rs.metaData
    return (1..metaData.columnCount).map { metaData.getColumnLabel(it) }.toList()
}

fun <T : AutoCloseable?, R> T.use(block: (T) -> R): R {
    try {
        return block(this)
    } finally {
        try {
            this?.close()
        } catch (e: Exception) {
            println(e.toString())
        }
    }
}