package com.example.demo.src.Post;


import com.example.demo.config.BaseException;
import com.example.demo.src.Post.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

import static com.example.demo.config.BaseResponseStatus.DATABASE_ERROR;

@Repository
public class PostDao {

    private JdbcTemplate jdbcTemplate;
    private List<GetPostImgRes> getPostImgRes;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<GetPostRes> selectPosts(int userIdx){
        String selectPostsQuery =
                "SELECT p.postIdx as postIdx,\n" +
                        "       u.userIdx as userIdx,\n" +
                        "       u.nickName as nickName,\n" +
                        "       u.profileImgUrl as profileImgUrl,\n" +
                        "       p.content as content,\n" +
                        "       IF(postLikeCount is null, 0, postLikeCount) as postLikeCount,\n" +
                        "       IF(commnentCount is null, 0, commentCount) as commmentCount,\n" +
                        "       CASE WHEN timestampdiff(second, p.updatedAt, current_timestamp) < 60\n" +
                        "        THEN concat(timestampdiff(second, p.updatedAt, current_timestamp), '초 전')\n" +
                        "        WHEN timestampdiff(minute, p.updatedAt, current_timestamp) < 60\n" +
                        "        THEN concat(timestampdiff(minute, p.updatedAt, current_timestamp), '분 전')\n" +
                        "        WHEN timestampdiff(hour, p.updatedAt, current_timestamp) < 24\n" +
                        "        THEN concat(timestampdiff(hour, p.updatedAt, current_timestamp), '시간 전')\n" +
                        "        WHEN timestampdiff(day, p.updatedAt, current_timestamp) < 365\n" +
                        "        THEN concat(timestampdiff(day, p.updatedAt, current_timestamp), '일 전')\n" +
                        "        ELSE timestampdiff(year, p.updatedAt, current_timestamp)\n" +
                        "        END AS updatedAt,\n" +
                        "        IF(pl.status = 'ACTIVE', 'Y', 'N') as likeOrNot\n" +
                        "        FROM Post as p\n" +
                        "        JOIN User as u on u.userIdx = p.useridx\n" +
                        "        LEFT JOIN (SELECT postIdx, userIdx, count(postLikeidx) as postLikeCount\n" +
                        "                   FROM PostLike\n" +
                        "                   WHERE status ='ACTIVE'\n" +
                        "                   GROUP BY postIdx) as pl on p.postIdx = pl.postIdx\n" +
                        "        LEFT JOIN (SELECT postIdx, COUNT(commentIdx) as commentCount\n" +
                        "                   FROM Comment\n" +
                        "                   WHERE status='ACTIVE') as c on c.postIdx = p.postIdx\n" +
                        "        WHERE p.status = 'ACTIVE' and p.postIdx = ?";

        int selectPostsParam=userIdx;

        return this.jdbcTemplate.query(selectPostsQuery,
                (rs,rowNum) -> new GetPostRes(
                        rs.getInt("postIdx"),
                        rs.getInt("userIdx"),
                        rs.getString("nickname"),
                        rs.getString("profileImgUrl"),
                        rs.getString("content"),
                        rs.getInt("postLikeCount"),
                        rs.getInt("commentCount"),
                        rs.getString("updatedAt"),
                        rs.getString("likeOrNOt"),
                        getPostImgRes=this.jdbcTemplate.query("\"SELECT pi.postImgUrlIdx, pi.imgUrl +\n" +
                                        "              \"               FROM PostImgUrl as pi\" +\n" +
                                        "              \"               JOIN Post as p on p.postIdx = pi.postIdx +\n" +
                                        "              \"               WHERE pi.status = 'ACTIVE' and p.postIdx = ?;\"",
                                (rk,rownum) -> new GetPostImgRes(
                                        rk.getInt("postImgUrlIdx"),
                                        rk.getString("imgUrl")
                                ), rs.getInt("postIdx")
                        )

                ),selectPostsParam);
    }

    //useridx'가 존재하는지 체크하는 함수
    public int checkUserExist(int userIdx){
        String checkUserExistQuery = "select exists(select userIdx from User where userIdx = ?)";
        int checkUserExistParams = userIdx;
        return this.jdbcTemplate.queryForObject(checkUserExistQuery,
                int.class,
                checkUserExistParams);

    }

    //postIdx'가 존재하는지 체크하는 함수
    public int checkPostExist(int postIdx){
        String checkPostExistQuery = "select exists(select postIdx from Post where postIdx = ?)";
        int checkPostExistParams = postIdx;
        return this.jdbcTemplate.queryForObject(checkPostExistQuery,
                int.class,
                checkPostExistParams);

    }
    //insert 문은 return을 하는게 아니라 update를 해주야 한다.

    public int insertPost(int userIdx, String content){
        String insertPostQuery = "INSERT INTO Post(userIdx, content) VALUES (?, ?)";
        Object []insertPostParams = new Object[] {userIdx, content};
        this.jdbcTemplate.update(insertPostQuery,
                insertPostParams);
        String lastInsertIdxQuery="SELECT last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInsertIdxQuery, int.class);
        //마지막에 들어간 idx값
    }

    public int insertPostImgs(int postIdx, PostImgUrlReq postImgUrlReq){
        String insertPostImgsQuery = "INSERT INTO PostImgUrl(postIdx, imgUrl) VALUES (?, ?)";
        Object []insertPostImgsParams = new Object[] {postIdx, postImgUrlReq.getImgUrl()};
        this.jdbcTemplate.update(insertPostImgsQuery,
                insertPostImgsParams);
        String lastInsertIdxQuery="SELECT last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInsertIdxQuery, int.class);

    }

    public int updatePost(int postIdx, String content){
        String updatePostQuery = "UPDATE Post SET content=? WHERE postIdx=?";
        Object [] updatePostParams = new Object[] {content, postIdx};
        return this.jdbcTemplate.update(updatePostQuery, updatePostParams);

    }

    public int deletePost(int postIdx){
        String deletePostQuery = "UPDATE Post SET status='INACTIVE' WHERE postIdx=?";
        Object [] deletePostParams = new Object[] {postIdx};
        return this.jdbcTemplate.update(deletePostQuery, deletePostParams);

    }
    //삭제가 아닌 active 에서 inactive로 수정
}