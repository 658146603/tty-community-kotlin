package model

import util.Value
import util.conn.MySQLConn
import util.enums.Shortcut
import java.sql.SQLException

class User(
    var id: String,
    var nickname: String,
    var portrait: String,
    var password: String,
    var token: String
) {

    companion object {
        fun getNickname(id: String): String {
            val conn = MySQLConn.connection
            var nickname: String
            try {
                val ps = conn.prepareStatement("select nickname from user where id = ?")
                ps.setString(1, id)
                val rs = ps.executeQuery()
                if (rs.next()) {
                    nickname = rs.getString("nickname")
                } else {
                    nickname = "UNDEFINED"
                }
                rs.close()
                ps.close()
            } catch (e: SQLException) {
                e.printStackTrace()
                nickname = "UNDEFINED"
            }

            return nickname
        }

        fun checkAuthorization(id: String, token: String): Shortcut {
            val conn = MySQLConn.connection
            try {
                val ps = conn.prepareStatement("select * from user where id = ?")
                ps.setString(1, id)
                val rs = ps.executeQuery()
                return if (rs.next()) {
                    if (Value.getMD5(rs.getString("token")) == token) {
                        rs.close()
                        ps.close()
                        Shortcut.OK
                    } else {
                        rs.close()
                        ps.close()
                        Shortcut.TE
                    }
                } else {
                    ps.close()
                    Shortcut.UPE
                }
            } catch (e: SQLException) {
                e.printStackTrace()
                return Shortcut.OTHER
            }
        }
    }
}
