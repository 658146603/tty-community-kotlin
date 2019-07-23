package servlet

import com.alibaba.fastjson.JSONObject
import model.Login
import model.RegisterInfo
import util.LoginPlatform
import util.LoginType
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

//TABLE USER


//# `id`: IntegerField()  # 用户的唯一标识符
//# nickname: TextField()  # 用户的昵称
//# password: TextField()  # 密码
//# token: TextField()  # 用户的token信息
//# last_login_ip: TextField()  # 用户最后一次登录的ip地址
//# last_login_time: TextField()  # 用户最后一次登录的时间，格式为'%y-%M-%d'
//# email: TextField()  # 用户邮箱
//# log: TextField()  # 用户登录的日志

//# create database tty_community;
//# create table user(
//#     _id integer primary key auto_increment,
//#     id text not null,
//#     nickname text not null,
//#     token text not null,
//#     password text not null,
//#     last_login_ip text not null,
//#     last_login_time text not null,
//#     email text not null,
//#     log blob not null
//# );

@WebServlet(name = "api", urlPatterns = ["/api/user"])
class API: HttpServlet() {
    private var reqIP: String = "0.0.0.0"
    private var method: ReqType = ReqType.Default
    override fun doGet(req: HttpServletRequest?, resp: HttpServletResponse?) {
        reqIP = getIpAddr(req!!)?:"0.0.0.0"
        resp?.writer?.write("IP: $reqIP\n")
        doPost(req, resp)
    }

    override fun doPost(req: HttpServletRequest?, resp: HttpServletResponse?){
        val out = resp!!.writer
        reqIP = getIpAddr(req!!)?:"0.0.0.0"
        method = when(req.getParameter("method")){
            "login" -> {
                // http://localhost:8080/community/api/user?method=login&platform=web&login_type=id&id=720468899&password=9128639163198r91b
                val json = JSONObject()
                val platform: LoginPlatform = when(req.getParameter("platform")){
                    "mobile" -> LoginPlatform.MOBILE
                    "pc" -> LoginPlatform.PC
                    "web" -> LoginPlatform.WEB
                    "pad" -> LoginPlatform.PAD
                    else -> {
                        json["shortcut"] = "AE"
                        json["msg"] = "platform not allowed."
                        out.write(json.toJSONString())
                        return
                    }
                }
                val login = Login(reqIP, platform)
                when(req.getParameter("login_type")) {
                    "id" -> {
                        login.loginType = LoginType.ID
                        login.id = req.getParameter("id")
                        login.password = req.getParameter("password")
                        out.write(login.submit())
                    }
                    "nickname" -> {
                        login.loginType = LoginType.NICKNAME
                        login.nickname = req.getParameter("nickname")
                        login.password = req.getParameter("password")
                        out.write(login.submit())
                    }
                    "third_party" -> {
                        login.loginType = LoginType.THIRD_PARTY
                        login.id = req.getParameter("id")
                        login.APIKey = req.getParameter("api_key")
                        out.write(login.submit())
                    }
                    else -> {
                        json["shortcut"] = "AE"
                        json["msg"] = "invalid login_type."
                        out.write(json.toJSONString())
                        return
                    }
                }


                ReqType.Login
            }
            "auto" -> {

                ReqType.AutoLogin
            }
            "register" -> {
                // http://localhost:8080/community/api/user?method=register&nickname=wcf&password=******&email=******
                val result = RegisterInfo(req.getParameter("nickname"), reqIP, req.getParameter("email"), req.getParameter("password")).submit()
                out.write(result)
                ReqType.Register
            }
            "check_name" -> {
                // http://localhost:8080/community/api/user?method=check_name&nickname=wcf
                val nickname = req.getParameter("nickname")
                val json = JSONObject()
                if(nickname == null || nickname.isEmpty()){
                    json["shortcut"] = "AE"
                    json["msg"] = "arguments mismatch."
                } else {
                    val result = RegisterInfo.checkNickname(req.getParameter("nickname"))
                    when(result){
                        false -> {
                            json["shortcut"] = "OK"
                            json["msg"] = "The nickname $nickname is not registered"
                        }
                        true -> {
                            json["shortcut"] = "UR"
                            json["msg"] = "The nickname $nickname has been registered"
                        }
                    }
                }
                out.write(json.toJSONString())
                ReqType.CheckName
            }
            else -> {
                val json = JSONObject()
                json["shortcut"] = "AE"
                json["msg"] = "invalid request"
                out.write(json.toJSONString())
                ReqType.Default
            }
        }
    }

    private fun getIpAddr(request: HttpServletRequest): String? {
        var ip: String? = request.getHeader("x-forwarded-for")
        if (ip != null && ip.isNotEmpty() && !"unknown".equals(ip, ignoreCase = true)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.contains(",")) {
                ip = ip.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            }
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("Proxy-Client-IP")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("WL-Proxy-Client-IP")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_CLIENT_IP")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.getHeader("X-Real-IP")
        }
        if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            ip = request.remoteAddr
        }
        return ip
    }

    enum class ReqType{
        Register, Login, AutoLogin, CheckName,
        Default
    }
}