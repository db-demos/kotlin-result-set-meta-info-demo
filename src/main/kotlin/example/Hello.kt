package example

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.ResultSetMetaData

fun main(args: Array<String>) {
    Class.forName("org.h2.Driver")

    val conn = DriverManager.getConnection("jdbc:h2:mem:mydb", "sa", "sa") // (2)

    conn.use {

        conn.createStatement().use { stmt ->
            with(stmt) {
                executeUpdate("create table users(id int primary key, name varchar(255))")
                executeUpdate("insert into users values(1, 'Hello')")
                executeUpdate("insert into users values(2, 'World')")
            }
        }

        printData(conn)
    }
}

private fun printData(conn: Connection) {
    conn.createStatement().use { stmt ->
        val rs = stmt.executeQuery("select id, name as name_alias from users")
        val meta = getColumnMeta(rs)
        meta.forEach { column -> print("%30s".format("column-name: ${column.columnName}")) }.also { println() }
        meta.forEach { column -> print("%30s".format("display-name: ${column.displayName}")) }.also { println() }
        meta.forEach { column -> print("%30s".format("type: ${column.type}")) }.also { println() }
        meta.forEach { column -> print("%30s".format("required: ${column.required}")) }.also { println() }
        println("============================================================")
        printRows(rs)
    }
}

private fun printRows(rs: ResultSet) {
    while (rs.next()) {
        (1..rs.metaData.columnCount).forEach { index ->
            print("%30s".format(rs.getObject(index).toString()))
        }
        println()
    }
}

// type: `java.sql.Types`
data class ColumnMeta(val columnName: String, val displayName: String, val type: Int, val required: Boolean)

private fun getColumnMeta(rs: ResultSet): List<ColumnMeta> {
    val metaData = rs.metaData
    return (1..metaData.columnCount).map { index ->
        ColumnMeta(
                columnName = metaData.getColumnName(index),
                displayName = metaData.getColumnLabel(index),
                type = metaData.getColumnType(index),
                required = metaData.isNullable(index) == ResultSetMetaData.columnNoNulls
        )
    }.toList()
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