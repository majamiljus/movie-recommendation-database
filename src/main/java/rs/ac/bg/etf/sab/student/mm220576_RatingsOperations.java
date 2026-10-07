package rs.ac.bg.etf.sab.student;

import rs.ac.bg.etf.sab.operations.RatingsOperations;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class mm220576_RatingsOperations implements RatingsOperations {


    @Override
    public boolean addRating(Integer userId, Integer movieId, Integer rating) {
        if (userId == null || movieId == null ||  rating == null || rating < 1 || rating > 10) return false;
        Connection conn = DB.getInstance().getConnection();
        String queryKor = "SELECT 1 FROM Korisnik WHERE IdKor = ?";
        String queryFilm = "SELECT 1 FROM Film WHERE IdFilm = ?";
        String queryOcena = "SELECT 1 FROM Ocena WHERE IdKor = ? AND IdFilm = ?";
        String queryInsert = "INSERT INTO Ocena(IdFilm, IdKor, Ocena) VALUES (?, ?, ?)";
        boolean oldAutoCommit = true;

        try (PreparedStatement korPs = conn.prepareStatement(queryKor);
             PreparedStatement filmPs = conn.prepareStatement(queryFilm);
             PreparedStatement ocenaPs = conn.prepareStatement(queryOcena);
             PreparedStatement insertPs = conn.prepareStatement(queryInsert);
             CallableStatement rewardCs = conn.prepareCall("{call SP_REWARD_USER_(?, ?)}")) {

            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            korPs.setInt(1, userId);
            ResultSet korRs = korPs.executeQuery();
            if (!korRs.next()) {
                korRs.close();
                conn.rollback();
                return false;
            }

            filmPs.setInt(1, movieId);
            ResultSet filmRs = filmPs.executeQuery();
            if (!filmRs.next()) {
                filmRs.close();
                conn.rollback();
                return false;
            }

            ocenaPs.setInt(1, userId);
            ocenaPs.setInt(2, movieId);
            ResultSet ocenaRs = ocenaPs.executeQuery();
            if (ocenaRs.next()) {
                ocenaRs.close();
                conn.rollback();
                return false;
            }

            insertPs.setInt(1, movieId);
            insertPs.setInt(2, userId);
            insertPs.setInt(3, rating);
            if (insertPs.executeUpdate() != 1) {
                conn.rollback();
                return false;
            }

            rewardCs.setInt(1, userId);
            rewardCs.setInt(2, movieId);
            rewardCs.execute();
            conn.commit();
            return true;
        } catch (SQLException e) {
            try {conn.rollback();} catch (SQLException ignored) {}
            return false;
        } finally {
            try {conn.setAutoCommit(oldAutoCommit);} catch (SQLException ignored) {}
        }
    }

    @Override
    public boolean updateRating(Integer userId, Integer movieId, Integer newRating) {
        if (userId == null || movieId == null ||  newRating == null || newRating < 1 || newRating > 10) return false;
        Connection conn = DB.getInstance().getConnection();
        String query = "UPDATE Ocena SET Ocena = ?, Datum = GETDATE() WHERE IdKor = ? AND IdFilm = ?";
        boolean oldAutoCommit = true;
        try (PreparedStatement ps = conn.prepareStatement(query);
             CallableStatement rewardCs = conn.prepareCall("{call SP_REWARD_USER_(?, ?)}")) {

            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            ps.setInt(1, newRating);
            ps.setInt(2, userId);
            ps.setInt(3, movieId);
            if (ps.executeUpdate() != 1) {
                conn.rollback();
                return false;
            }

            rewardCs.setInt(1, userId);
            rewardCs.setInt(2, movieId);
            rewardCs.execute();
            conn.commit();
            return true;
        } catch (SQLException e) {
            try {conn.rollback();} catch (SQLException ignored) {}
            return false;
        } finally {
            try {conn.setAutoCommit(oldAutoCommit);} catch (SQLException ignored) {}
        }
    }

    @Override
    public boolean removeRating(Integer userId, Integer movieId) {
        if (userId == null || movieId == null) return false;
        Connection conn = DB.getInstance().getConnection();
        String query = "DELETE FROM Ocena WHERE IdKor = ? AND IdFilm = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public Integer getRating(Integer userId, Integer movieId) {
        if (userId == null || movieId == null) return null;
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT Ocena FROM Ocena WHERE IdKor = ? AND IdFilm = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()){
                    return rs.getInt(1);
                }
                return null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public List<Integer> getRatedMoviesByUser(Integer userId) {
        List<Integer> result = new ArrayList<>();
        if (userId == null) return result;
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT IdFilm FROM Ocena WHERE IdKor = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                result.add(id);
            }
            return result;
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Integer> getUsersWhoRatedMovie(Integer movieId) {
        List<Integer> result = new ArrayList<>();
        if (movieId == null) return result;
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT IdKor FROM Ocena WHERE IdFilm = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                result.add(id);
            }
            return result;
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }
}
