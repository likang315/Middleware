package com.xupt.web.controller;

import com.xupt.model.pojo.User;
import com.xupt.service.IUserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: likang
 * @mail: 13571887352@163.com
 * @GitHub：https://github.com/likang315
 * @CreateTime： 2019/1/25 19:54
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private static Logger log=Logger.getLogger(UserController.class);

    @Autowired
    private IUserService ius;

    @RequestMapping("/login")
    public String userHandle(Model model)
    {
        log.info("+++++++ start 查找User +++++++++");
        User user=ius.findById(2);
        System.out.println(user.toString());

        model.addAttribute("user",user);
        log.info("---------- end 查找User --------------");
        return "user/login";

    }


}
