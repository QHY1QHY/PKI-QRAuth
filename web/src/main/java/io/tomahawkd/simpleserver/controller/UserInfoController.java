package io.tomahawkd.simpleserver.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.tomahawkd.simpleserver.exceptions.NotFoundException;
import io.tomahawkd.simpleserver.model.SystemLogModel;
import io.tomahawkd.simpleserver.model.UserInfoModel;
import io.tomahawkd.simpleserver.model.UserPasswordModel;
import io.tomahawkd.simpleserver.service.SystemLogService;
import io.tomahawkd.simpleserver.service.UserInfoService;
import io.tomahawkd.simpleserver.service.UserPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user/info")
public class UserInfoController {

    @Resource
    private UserInfoService userInfoService;
    @Resource
    private SystemLogService systemLogService;
    @Resource
    private UserPasswordService userPasswordService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // http://127.0.0.1/user/info/liucheng
    @GetMapping("/data")
    public String getInfoPageById(HttpServletRequest request) throws NotFoundException {
        System.out.println("getinfo");
        String user =(String)request.getSession().getAttribute("username");

        systemLogService.insertLogRecord(UserInfoController.class.getName(),
                "getInfoPageById", SystemLogModel.DEBUG, "Accept username: " + user);
        UserInfoModel model = userInfoService.getUserInfo(user);
        if (model == null) {
            systemLogService.insertLogRecord(UserInfoController.class.getName(), "getInfoPageById",
                    SystemLogModel.WARN, "User not found");
            throw new NotFoundException("User not found");
        }
        systemLogService.insertLogRecord(UserInfoController.class.getName(), "getInfoPageById",
                SystemLogModel.OK, "User found: " + model.toString());
        return model.toString();
    }




    @PostMapping("/update/info")
    public String updateUserInfo(HttpServletRequest request,@RequestBody String body) throws Exception {

        Map<String, String> bodyData =
                new Gson().fromJson(body, new TypeToken<Map<String, String>>() {
                }.getType());

        String username = (String)request.getSession().getAttribute("username");
        String name = bodyData.get("name");
        int sex = Integer.parseInt(bodyData.get("sex"));
        String email = bodyData.get("email");
        String phone = bodyData.get("phone");
        String bio = bodyData.get("bio");

        Map<String, MultipartFile> imageData =
                new Gson().fromJson(body, new TypeToken<Map<String, MultipartFile>>() {
                }.getType());

        String image_path = this.getImagePath(username, imageData.get("image"));

        UserInfoModel model = new UserInfoModel(1, username, name, sex, email, phone, bio, image_path);
        systemLogService.insertLogRecord(UserInfoController.class.getName(),
                "changeUserInfo", SystemLogModel.DEBUG,  " changingInfo:" + model.toString());
        boolean result = userInfoService.changeUserInfo(model);

        if(result )
        {
            systemLogService.insertLogRecord(UserInfoController.class.getName(),
                    "changeUserInfo", SystemLogModel.OK,  " change Info successfully");

            return  "{\"status\": 0, \"message\": \"success\"}" ;

        }
        else {
            systemLogService.insertLogRecord(UserInfoController.class.getName(),
                    "changeUserInfo", SystemLogModel.WARN,  " change Info failed");
            return "{\"status\": 1, \"message\": \"failed\"}";

        }
    }

    @PostMapping("/update/password")
    public String updateUserPassword(HttpServletRequest request,@RequestBody String body) throws Exception {

        Map<String, String> bodyData =
                new Gson().fromJson(body, new TypeToken<Map<String, String>>() {
                }.getType());

        String username = (String)request.getSession().getAttribute("username");
        String password = bodyData.get("password");
        String new_password = bodyData.get("new_password");

        UserPasswordModel model = new UserPasswordModel(username,password);
        systemLogService.insertLogRecord(UserInfoController.class.getName(),
                "updateUserPassword", SystemLogModel.DEBUG,  " updateUserPassword:" + model.toString());
      boolean result = userPasswordService.changePassword(model,new_password);

        if(result )
        {
            systemLogService.insertLogRecord(UserInfoController.class.getName(),
                    "updateUserPassword", SystemLogModel.OK,  " update user password successfully");

            return  "{\"status\": 0, \"message\": \"success\"}" ;

        }
        else {
            systemLogService.insertLogRecord(UserInfoController.class.getName(),
                    "updateUserPassword", SystemLogModel.WARN,  " update user password failed");
            return "{\"status\": 1, \"message\": \"failed\"}";

        }
    }


    private String getImagePath(String username, MultipartFile file) {
        String path = "/user/image";    //图像存储路径
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String id = UUID.randomUUID().toString();
        String fileName = file.getOriginalFilename();
        String img = id + fileName.substring(fileName.lastIndexOf("."));

        FileOutputStream imgOut = null;//根据 dir 抽象路径名和 img 路径名字符串创建一个新 File 实例。
        try {
            imgOut = new FileOutputStream(new File(dir, img));
            /* System.out.println(file.getBytes());*/
            imgOut.write(file.getBytes());//返回一个字节数组文件的内容
            systemLogService.insertLogRecord(UserInfoController.class.getName(), "getImagePath",
                    SystemLogModel.OK, username + " image store success..");
            imgOut.close();
            return img;
        } catch (Exception e) {
            e.printStackTrace();
            systemLogService.insertLogRecord(UserInfoController.class.getName(), "getImagePath",
                    SystemLogModel.WARN, username + " image store failed.");
        }
        return null;
    }


}
