package com.example.demo.src.user;


import com.example.demo.config.BaseException;

import com.example.demo.src.user.model.PostLoginReq;
import com.example.demo.src.user.model.PostLoginRes;
import com.example.demo.src.user.model.*;
import com.example.demo.utils.JwtService;
import com.example.demo.utils.SHA256;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.demo.config.BaseResponseStatus.*;

// Service Create, Update, Delete 의 로직 처리
@Service
public class UserService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserDao userDao;
    private final UserProvider userProvider;
    private final JwtService jwtService;


    @Autowired
    public UserService(UserDao userDao, UserProvider userProvider, JwtService jwtService) {
        this.userDao = userDao;
        this.userProvider = userProvider;
        this.jwtService = jwtService;

    }


    public PostUserRes createUser(@NotNull PostUserReq postUserReq) throws BaseException {
        // 이메일 중복 확인
        if (userProvider.checkEmail(postUserReq.getEmail()) == 1) {
            throw new BaseException(POST_USERS_EXISTS_EMAIL);
        }
        String pwd;
        try {
            //암호화
            pwd = new SHA256().encrypt(postUserReq.getPassword());
            postUserReq.setPassword(pwd);
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);

        }
        try {
            int userIdx = userDao.createUser(postUserReq);
            //jwt 발급.
            // TODO: jwt는 다음주차에서 배울 내용입니다!
            String jwt = jwtService.createJwt(userIdx);
            return new PostUserRes(jwt, userIdx);
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void modifyUserName(PatchUserReq patchUserReq) throws BaseException {
        try {
            int result = userDao.modifyUserName(patchUserReq);
            if (result == 0) {
                throw new BaseException(MODIFY_FAIL_USERNAME);
            }
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void deleteUsersByIdx(int userIdx) throws BaseException {
        try {
            int result = userDao.deleteUsersByIdx(userIdx);
            if(result == 0)
            {
                throw new BaseException(DELETE_FAIL_USERNAME);
            }
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }


    public PostLoginRes logIn(PostLoginReq postLoginReq) throws BaseException {
        User user = userDao.getPwd(postLoginReq);
        String encryptPwd;
        try{
            //암호화, SHA256() class로 암호화.
            encryptPwd = new SHA256().encrypt(postLoginReq.getPwd());
        } catch(Exception exception){
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }
        if(user.getPassword().equals(encryptPwd)) {
            int userIdx = user.getUserIdx();
            String jwt = jwtService.createJwt(userIdx); //jwt 생성 (utils 파일에 있음)
            return new PostLoginRes(userIdx, jwt); //응답값
        }
        else {
            throw new BaseException(FAILED_TO_LOGIN);
        }
    }

}
