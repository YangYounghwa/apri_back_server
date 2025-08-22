package apri.back_demo.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import apri.back_demo.model.KakaoUser;

@Service
public class UserRegisteration {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void registerUser(KakaoUser user) {

        String sql = "INSERT INTO users (kakao_user_id, user_name, gender, birth_year,thumbnail_url,nickname)" +
                "VALUES (?,?,?,?,?,?)";
        jdbcTemplate.update(sql, user.getKakao_user_id(), null, user.getGender(), user.getBirth_year(),
                user.getThumbnail_url(), user.getNickname());
        // add necessary
    }

    public boolean deleteUserByKakaoId(Long kakaoId) {              
        String sql = "DELETE FROM users where kakao_user_id = ?";
        return jdbcTemplate.update(sql, kakaoId) > 0;
    }

    public boolean existsByKakaoId(Long kakaoId) {
        String sql = "SELECT COUNT(*) FROM users WHERE kakao_user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, kakaoId);
        return count != null && count > 0;
    }

    public boolean updateUser(KakaoUser user) {
        String sql = "UPDATE users SET user_name = ?, gender = ?, birth_year = ?, thumbnail_url = ?, nickname = ?"
                + "WHERE kakao_user_id = ?";
        int rows = jdbcTemplate.update(sql,
                user.getName(),
                user.getGender(),
                user.getBirth_year(),
                user.getThumbnail_url(),
                user.getNickname(),
                user.getKakao_user_id());
        return rows > 0;
    }

    public KakaoUser findByKakaoUserId(Long kakaoUserId) {
        String sql = "SELECT * FROM users WHERE kakao_user_id = ?";

        try {
            return jdbcTemplate.queryForObject(
                    sql,
                    new KakaoUserRowMapper(),
                    kakaoUserId // varargs approach
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // findByKakaoUserId have apri_id 
    public Long getApri_idByKakaoUserId(Long kakaoUserId){
        String sql = "SELECT apri_id FROM users WHERE kakao_user_id = ?";
        try {

            return jdbcTemplate.queryForObject(sql,Long.class,kakaoUserId, null);

        } catch (EmptyResultDataAccessException e){
            return null;
        }

    }

    private static class KakaoUserRowMapper implements RowMapper<KakaoUser> {
        @Override
        public KakaoUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            KakaoUser user = new KakaoUser();
            user.setApri_id(rs.getLong("apri_id"));
            user.setKakao_user_id(rs.getLong("kakao_user_id"));
            user.setName(rs.getString("user_name"));
            user.setGender(rs.getString("gender"));
            user.setBirth_year(rs.getInt("birth_year"));
            Timestamp ts = rs.getTimestamp("registration_timestamp");
            if (ts != null) {
                user.setRegistration_timestamp(ts.toLocalDateTime());
            }
            user.setThumbnail_url(rs.getString("thumbnail_url"));
            user.setNickname(rs.getString("nickname"));
            return user;
        }
    }
}
