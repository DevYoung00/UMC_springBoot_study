package com.example.demo.src.Post;


import com.example.demo.config.BaseException;

import com.example.demo.src.Post.model.*;
import com.example.demo.utils.JwtService;
import com.example.demo.utils.SHA256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.demo.config.BaseResponseStatus.*;

// Service Create, Update, Delete 의 로직 처리
@Service
public class PostService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PostDao postDao;
    private final PostProvider postProvider;
    private final JwtService jwtService;


    @Autowired
    public PostService(PostDao postDao, PostProvider postProvider, JwtService jwtService) {
        this.postDao = postDao;
        this.postProvider = postProvider;
        this.jwtService = jwtService;

    }

    public PostPostsRes createPost(int userIdx, PostPostsReq postPostsReq) throws BaseException {
        try{
            int postIdx = postDao.insertPost(userIdx, postPostsReq.getContent());
            //insertPosts 함수는 게시물 내용만 저장
            for (int i=0; i<postPostsReq.getPostImgUrl().size(); i++) {
                postDao.insertPostImgs(postIdx, postPostsReq.getPostImgUrl().get(i));
                //반복문 돌며 이미지를 저장
            }

            return new PostPostsRes(postIdx);
        }
        catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
    public void modifyPost(int userIdx, int postIdx, PatchPostsReq patchPostsReq) throws BaseException {
        if(postProvider.checkUserExist(userIdx) == 0) {
            //유저 확인
            throw new BaseException(USERS_EMPTY_USER_ID);
        }
        if(postProvider.checkPostExist(userIdx) == 0) {
            //게시물 확인
            throw new BaseException(POSTS_EMPTY_POST_ID);
        }
        try {
            int result = postDao.updatePost(postIdx, patchPostsReq.getContent());
            //성공 result 1 실패 result 0
            if(result == 0) {
                throw new BaseException(MODIFY_FAIL_POST);
            }
        }
        catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void deletePost(int postIdx) throws BaseException {
        try {
            int result = postDao.deletePost(postIdx);
            //성공 1 실페 0
            if(result == 0) {
                throw new BaseException(DELETE_FAIL_POST);
            }
        }
        catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
