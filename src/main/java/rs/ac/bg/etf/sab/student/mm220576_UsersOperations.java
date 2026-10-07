package rs.ac.bg.etf.sab.student;

import rs.ac.bg.etf.sab.operations.UsersOperations;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class mm220576_UsersOperations implements UsersOperations {

    @Override
    public Integer addUser(String username) {
        if (username == null || username.length() > 100) return null;
        Connection conn = DB.getInstance().getConnection();
        String queryKor = "SELECT 1 FROM Korisnik WHERE KorIme = ?";
        String queryInsert = "INSERT INTO Korisnik(KorIme) VALUES (?)";
        try (PreparedStatement checkPs = conn.prepareStatement(queryKor);
             PreparedStatement insertPs = conn.prepareStatement(queryInsert, Statement.RETURN_GENERATED_KEYS)) {
            checkPs.setString(1, username);
             ResultSet rs = checkPs.executeQuery();
             if (rs.next()) return null;
            rs.close();
            insertPs.setString(1, username);
            if (insertPs.executeUpdate() != 1) return null;

            ResultSet inserRs = insertPs.getGeneratedKeys();
            if(inserRs.next()){
                return inserRs.getInt(1);
            }
            return null;

        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Integer updateUser(Integer userId, String username) {
        if (userId == null || username == null || username.length() > 100) return null;
        Connection conn = DB.getInstance().getConnection();
        String query = "UPDATE Korisnik SET KorIme = ? WHERE IdKor = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1 ? userId : null;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Integer removeUser(Integer userId) {
        if (userId == null) return null;
        Connection conn = DB.getInstance().getConnection();
        String queryLZ = "DELETE FROM ListaZelja WHERE IdKor = ?";
        String queryOcena = "DELETE FROM Ocena WHERE IdKor = ?";
        String queryKor = "DELETE FROM Korisnik WHERE IdKor = ?";
        boolean oldAutoCommit = true;

        try (PreparedStatement lzPs = conn.prepareStatement(queryLZ);
             PreparedStatement ocenaPs = conn.prepareStatement(queryOcena);
             PreparedStatement korPs = conn.prepareStatement(queryKor)) {
                oldAutoCommit = conn.getAutoCommit();
                conn.setAutoCommit(false);

                lzPs.setInt(1, userId);
                lzPs.executeUpdate();

                ocenaPs.setInt(1, userId);
                ocenaPs.executeUpdate();

                korPs.setInt(1, userId);
                if (korPs.executeUpdate() != 1) {
                    conn.rollback();
                    return null;
                }

                conn.commit();
                return userId;
        } catch (SQLException e) {
            try {conn.rollback();} catch (SQLException ignored) {}
            return null;
        } finally {
            try {conn.setAutoCommit(oldAutoCommit);} catch (SQLException ignored) {}
        }
    }

    @Override
    public boolean doesUserExist(String username) {
        if (username == null || username.length() > 100) return false;
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT 1 FROM Korisnik WHERE KorIme = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public Integer getUserId(String username) {
        if (username == null || username.length() > 100) return null;
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT IdKor FROM Korisnik WHERE KorIme = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()){
                    return rs.getInt(1);
                }
                return  null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public List<Integer> getAllUserIds() {
        List<Integer> result = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT IdKor FROM Korisnik";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                result.add(id);
            }
            rs.close();
            return result;
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Integer> getRecommendedMoviesFromFavoriteGenres(Integer userId) {
        List<Integer> result = new ArrayList<>();
        if (userId == null) return result;
        Connection conn = DB.getInstance().getConnection();
        try (CallableStatement cs = conn.prepareCall("{call sp_recommended_idfilm(?)}")) {
            cs.setInt(1, userId);
            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                result.add(id);
            }
            rs.close();
            return result;
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Integer getRewards(Integer userId) {
        if (userId == null) return 0;
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT Nagrade FROM Korisnik WHERE IdKor = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
            rs.close();
            return 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    @Override
    public List<String> getThematicSpecializations(Integer userId) {
        List<String> result = new ArrayList<>();
        if (userId == null) return result;
        Connection conn = DB.getInstance().getConnection();
        String query =  "SELECT t.Naziv " +
                        "FROM Ocena o " +
                        "JOIN TagFilm tf ON tf.IdFilm = o.IdFilm " +
                        "JOIN Tag t ON t.IdTag = tf.IdTag " +
                        "WHERE o.IdKor = ? AND o.Ocena >= 8 " +
                        "GROUP BY t.IdTag, t.Naziv " +
                        "HAVING COUNT(DISTINCT o.IdFilm) >= 2";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String naziv = rs.getString(1);
                result.add(naziv);
            }
            rs.close();
            return result;
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public String getUserDescription(Integer userId) {
        if (userId == null) return "undefined";
        Connection conn = DB.getInstance().getConnection();
        String query =  "SELECT COUNT(DISTINCT o.IdFilm) AS BrojFilmova, " +
                        "COUNT(DISTINCT tf.IdTag) AS BrojTagova " +
                        "FROM Ocena o " +
                        "LEFT JOIN TagFilm tf ON tf.IdFilm = o.IdFilm " +
                        "WHERE o.IdKor = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return "undefined";
            int brojFilmova = rs.getInt("BrojFilmova");
            int brojTagova = rs.getInt("BrojTagova");
            rs.close();
            if (brojFilmova < 10) return "undefined";
            if (brojTagova >= 10) return "curious";
            return "focused";
        } catch (SQLException e) {
            return "undefined";
        }
    }
}
