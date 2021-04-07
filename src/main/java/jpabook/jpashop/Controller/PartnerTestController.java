package jpabook.jpashop.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Controller
public class PartnerTestController {

    @ResponseBody
    @RequestMapping("/groups")
    public String groupList(HttpServletRequest request) {

        return "<table border=1><tr><td>001</td><td>test1</td></tr><tr><td>002</td><td>test2</td></tr></table>";
    }

    @ResponseBody
    @RequestMapping("/count")
    public String groupCount(HttpServletRequest request) {
        return "10";
    }

    @ResponseBody
    @RequestMapping("/ftp")
    public String ftp(HttpServletRequest request) {

        return request.getParameter("post_id");
    }

    @RequestMapping("/ftp_result/{post_id}")
    public String ftpResult(HttpServletRequest request, @PathVariable String post_id) {

        return "http://partners.postman.co.kr:90/parter/module/upload_complete.jsp?user_id=dlrudtn108&post_id=" + post_id;
    }
}
