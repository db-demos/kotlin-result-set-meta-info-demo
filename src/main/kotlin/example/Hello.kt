package example

import java.sql.DriverManager
import java.sql.ResultSet

private val DBNAME = "mydb"

fun main(args: Array<String>) {
    Class.forName("org.h2.Driver")

    val conn = DriverManager.getConnection("jdbc:h2:mem:$DBNAME", "sa", "sa") // (2)

    conn.use {

        conn.createStatement().use { stmt ->
            with(stmt) {
                executeUpdate("create table mytbl(id int primary key, name varchar(255))")
                executeUpdate("insert into mytbl values(1, 'Hello')")
                executeUpdate("insert into mytbl values(2, 'World')")
            }
        }

        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery("select * from mytbl")
            val columnNames = getColumnNames(rs)

            columnNames.forEach { name -> print("$name\t") }
            println()

            while (rs.next()) {
                columnNames.forEach { name ->
                    print(rs.getObject(name).toString() + "\t")
                }
                println()
            }
        }
    }

}

private fun getColumnNames(rs: ResultSet): List<String> {
    val metaData = rs.metaData
    return (1..metaData.columnCount).map { metaData.getColumnName(it) }.toList()
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