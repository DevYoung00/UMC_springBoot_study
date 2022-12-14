package com.example.demo.src.user;

import com.example.demo.config.BaseException;
import com.example.demo.config.BaseResponse;
import com.example.demo.src.user.model.*;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.example.demo.config.BaseResponseStatus.*;
import static com.example.demo.utils.ValidationRegex.isRegexEmail;

@RestController
@RequestMapping("/users")
public class UserController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final UserProvider userProvider;
    @Autowired
    private final UserService userService;
    @Autowired
    private final JwtService jwtService;


    public UserController(UserProvider userProvider, UserService userService, JwtService jwtService) {
        this.userProvider = userProvider;
        this.userService = userService;
        this.jwtService = jwtService;
    }


    /**
     * 회원 조회 API
     * [GET] /users
     * 이메일 검색 조회 API
     * [GET] /users? Email=
     *
     * @return BaseResponse<GetUserRes>
     */
    //Query String
    @ResponseBody
    @GetMapping("") // (GET) 127.0.0.1:9000/users
    public BaseResponse<GetUserRes> getUsers(@RequestParam(required = true) String Email) {
        try {
            // TODO: email 관련한 짧은 validation 예시입니다. 그 외 더 부가적으로 추가해주세요!
            if (Email.length() == 0) {
                return new BaseResponse<>(POST_USERS_EMPTY_EMAIL);
            }
            // 이메일 정규표현
            if (!isRegexEmail(Email)) {
                return new BaseResponse<>(POST_USERS_INVALID_EMAIL);
            }
            GetUserRes getUsersRes = userProvider.getUsersByEmail(Email);
            return new BaseResponse<>(getUsersRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    @ResponseBody
    @GetMapping("/{userIdx}") // (GET) 127.0.0.1:9000/users/:userIdx
    public BaseResponse<GetUserRes> getUserByIdx(@PathVariable("userIdx") int userIdx) {
        try {
            GetUserRes getUsersRes = userProvider.getUsersByIdx(userIdx);
            return new BaseResponse<>(getUsersRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 회원가입 API
     * [POST] /users
     *
     * @return BaseResponse<PostUserRes>
     */
    // Body
    @ResponseBody
    @PostMapping("") // (POST) 127.0.0.1:9000/users
    public BaseResponse<PostUserRes> createUser(@RequestBody PostUserReq postUserReq) {
        // TODO: email 관련한 짧은 validation 예시입니다. 그 외 더 부가적으로 추가해주세요!
        if (postUserReq.getEmail() == null) {
            return new BaseResponse<>(POST_USERS_EMPTY_EMAIL);
        }
        // 이메일 정규표현
        if (!isRegexEmail(postUserReq.getEmail())) {
            return new BaseResponse<>(POST_USERS_INVALID_EMAIL);
        }
        try {
            PostUserRes postUserRes = userService.createUser(postUserReq);
            return new BaseResponse<>(postUserRes);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 유저정보변경 API
     * [PATCH] /users/:userIdx
     *
     * @return BaseResponse<String>
     */
    @ResponseBody
    @PatchMapping("/{userIdx}") // (PATCH) 127.0.0.1:9000/users/:userIdx
    public BaseResponse<String> modifyUserName(@PathVariable("userIdx") int userIdx, @RequestBody User user) {
        try {
            PatchUserReq patchUserReq = new PatchUserReq(userIdx, user.getNickName());
            userService.modifyUserName(patchUserReq);

            String result = "";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 유저 삭제 api
     */

    @ResponseBody
    @PatchMapping("/{userIdx}/status") // (Patch) 127.0.0.1:9000/users/:userIdx/status
    // status 를 active에서 inactive로 바꿈
    public BaseResponse<String> deleteUsersByIdx(@PathVariable("userIdx") int userIdx) {
        try {
            userService.deleteUsersByIdx(userIdx);
            String result = "유저가 삭제.";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
    * 피드 조회
    * */
    @ResponseBody
    @GetMapping("/{userIdx}/X") // (GET) 127.0.0.1:9000/users
    public BaseResponse<GetUserFeedRes> getUserFeed(@PathVariable("userIdx") int userIdx) {
        try{
            GetUserFeedRes getUserFeedRes = userProvider.retrieveUserFeed(userIdx, userIdx);
            return new BaseResponse<>(getUserFeedRes);
        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    //login은 post로 구현하면 된다.
    @ResponseBody
    @PostMapping("/login")
    public BaseResponse<PostLoginRes> logIn(@RequestBody PostLoginReq postLoginReq) {
        try{
            if(postLoginReq.getEmail() == null) {
                return new BaseResponse<>(POST_USERS_EMPTY_EMAIL);
            }
            if(postLoginReq.getPwd() == null) {
                return new BaseResponse<>(POST_USERS_EMPTY_PASSWORD);
            }
            if(!isRegexEmail(postLoginReq.getEmail())) { //정규식
                return new BaseResponse<>(POST_USERS_INVALID_EMAIL);
            }
            //service에 넘겨줌
            PostLoginRes postLoginRes = userService.logIn(postLoginReq);
            return new BaseResponse<>(postLoginRes);

        } catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }


}
