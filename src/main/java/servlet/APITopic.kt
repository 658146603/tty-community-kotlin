package servlet

import exception.ProjThrowable
import model.Message
import model.Topic
import model.Topic.Companion.similarTopic
import util.CONF
import util.Value.getFields
import util.enums.Shortcut
import util.parse.IP
import java.io.PrintWriter
import java.sql.SQLException
import java.util.ArrayList
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@WebServlet(name = "api_topic", urlPatterns = ["/api/topic/*"])
class APITopic : HttpServlet() {
    private var ip: String = "0.0.0.0"
    private lateinit var out: PrintWriter


    override fun doGet(req: HttpServletRequest?, resp: HttpServletResponse?) {
        doPost(req, resp)
    }

    override fun doPost(req: HttpServletRequest?, resp: HttpServletResponse?) {
        resp?.characterEncoding = "utf-8"
        req?.characterEncoding = "utf-8"
        out = resp!!.writer
        ip = IP.getIPAddr(req!!)
        val route = try {
            req.requestURI.substring(21)
        } catch (e: StringIndexOutOfBoundsException) {
            out.write(Message(Shortcut.AE, "invalid request",null).json())
            return
        }

        val fields = req.parameterMap.getFields()

        when (route) {

            "create" -> {
                /**
                 * @http http://47.102.200.155:8080/community/api/topic/create
                 * @values id, token, name, parent, introduction
                 * @intro
                 * Only user group > 1 can create the topic, or the system will notify the message
                 * the board, and users can decide whether to create the topic, the user whose
                 * user_group > 3 can create the topic directly
                 *  Every topic must have a parent, except root topic ALL(id = "000000")
                 */

                val id = fields["id"]
                val token = fields["token"]
                val name = fields["name"]
                val parent = fields["parent"]
                val introduction = fields["introduction"]
                TODO()

            }

            "find" -> {
                /**
                 * http://47.102.200.155:8080/community/api/topic/find?name=ALL
                 * @field name
                 * find the topic which match to the name
                */
                val name = fields["name"]
                find(name)
            }

            "similar" -> {
                /**
                 * http://47.102.200.155:8080/community/api/topic/similar
                 * find the list of topic whose name or introduction similar to the name for user to select
                 */

                val name = fields["name"]
                similar(name)
            }

            "list" -> {
                /**
                 * http://47.102.200.155:8080/community/api/topic/list
                 * @field id
                 * the following topic list of the user's id, the info is public for everyone
                 */
                TODO()
            }

            "children" -> {
                TODO()
            }

            "parent" -> {
                /**
                 * @field id
                 */
                val id = fields["id"]
                parent(id)
            }

            "child" -> {
                val id = fields["id"]
                child(id)
            }

            "follow" -> {
                /**
                 * @field id, token, topic_id
                 * the action that the user want to follow the topic that he can view the
                 * content of the topic, the id and token is necessary to ensure the request
                 * is posted by user himself
                 */
            }

            "unfollow" -> {
                TODO()
            }

            "info" -> {
                TODO()
            }

            "picture" -> {
                TODO()
            }

            "change_pic" -> {

            }

            "change_info" -> {
                TODO()
            }

            "test" -> {
                // http://47.102.200.155:8080/community/api/topic/test
                test()
            }

            else -> {
                out.write(Message(Shortcut.AE, "invalid request.", null).json())
            }
        }
    }

    private fun create(id: String, token: String, name: String, parent: String, introduction: String): Shortcut {
        TODO()
    }

    private fun find(name: String?) {
        try {
            if (name.isNullOrEmpty()) {
                throw ProjThrowable.AE()
            }

            throw findTopicOutlineByName(name, FindType.EQUALS)

        } catch (e: SQLException) {
            e.printStackTrace()
            out.write(Message(Shortcut.OTHER, "SQL ERROR", null).json())
            return
        } catch (info: ProjThrowable) {
            out.write(info.json())
        }

    }

    private fun similar(name: String?) {
        try {
            if (name.isNullOrEmpty()) {
                throw ProjThrowable.AE()
            }

            throw findTopicOutlineByName(name, FindType.LIKE)

        } catch (e: SQLException) {
            e.printStackTrace()
            out.write(Message(Shortcut.OTHER, "SQL ERROR", null).json())
        } catch (info: ProjThrowable) {
            out.write(info.json())
        }
    }

    private fun parent(id: String?) {
        try {
            if (id.isNullOrEmpty()) {
                throw ProjThrowable.AE()
            }

            throw findTopicOutlineById(id, FindType.Parent)

        } catch (e: SQLException) {
            e.printStackTrace()
            out.write(Message(Shortcut.OTHER, "SQL ERROR", null).json())
        } catch (info: ProjThrowable) {
            out.write(info.json())
        }
    }

    private fun child(id: String?) {
        try {
            if (id.isNullOrEmpty()) {
                throw ProjThrowable.AE()
            }

            throw findTopicOutlineById(id, FindType.Child)

        } catch (e: SQLException) {
            e.printStackTrace()
            out.write(Message(Shortcut.OTHER, "SQL ERROR", null).json())
        } catch (info: ProjThrowable) {
            out.write(info.json())
        }
    }

    private fun test() {
        out.println(CONF.root)
        out.println(CONF.conf.server)
    }

    private fun findTopicOutlineByName(name: String, type: FindType): ProjThrowable {
        var info: ProjThrowable

        when(type) {
            FindType.EQUALS -> {
                try {
                    val topic = Topic.findOutlineByName(name)
                    info = ProjThrowable.OK("success get topic", topic)
                } catch (e: ProjThrowable) {
                    info = e
                }
            }
            FindType.LIKE -> {
                val list: ArrayList<Topic.Outline> = similarTopic(name)
                info = ProjThrowable.OK("success get topic list", list)
            }

            FindType.Child -> {
                try {
                    val list = Topic.findChildrenByName(name)
                    info = ProjThrowable.OK("success get parent topic", list)
                } catch (e: ProjThrowable) {
                    info = e
                }
            }

            FindType.Parent -> {
                try {
                    val topic = Topic.findParentByName(name)
                    info = ProjThrowable.OK("success get parent topic", topic.map())
                } catch (e: ProjThrowable) {
                    info = e
                }
            }
        }


        return info
    }

    private fun findTopicOutlineById(id: String, type: FindType): ProjThrowable {
        return try {
            val topic = Topic.findOutlineById(id)
            findTopicOutlineByName(topic.name, type)
        } catch (e: ProjThrowable) {
            e
        }
    }

    enum class FindType {
        EQUALS, LIKE, Child, Parent
    }

}