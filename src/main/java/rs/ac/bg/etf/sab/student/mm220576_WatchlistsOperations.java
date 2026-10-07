package rs.ac.bg.etf.sab.student;

import rs.ac.bg.etf.sab.operations.WatchlistsOperations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class mm220576_WatchlistsOperations implements WatchlistsOperations {
    @Override
    public boolean addMovieToWatchlist(Integer userId, Integer movieId) {
        if (userId == null || movieId == null) return false;

        Connection conn = DB.getInstance().getConnection();
        String queryUser = "SELECT 1 FROM Korisnik WHERE IdKor = ?";
        String queryMovie = "SELECT 1 FROM Film WHERE IdFilm = ?";
        String queryLZ = "SELECT 1 FROM ListaZelja WHERE IdKor = ? AND IdFilm = ?";
        String queryInsert = "INSERT INTO ListaZelja(IdFilm, IdKor) VALUES (?, ?)";

        try (PreparedStatement userPs = conn.prepareStatement(queryUser);
             PreparedStatement moviePs = conn.prepareStatement(queryMovie);
             PreparedStatement lzPs = conn.prepareStatement(queryLZ);
             PreparedStatement insertPs = conn.prepareStatement(queryInsert)) {

            userPs.setInt(1, userId);
            ResultSet userRs = userPs.executeQuery();
            if (!userRs.next()) return false;
            userRs.close();

            moviePs.setInt(1, movieId);
            ResultSet movieRs = moviePs.executeQuery();
            if (!movieRs.next()) return false;
            movieRs.close();

            lzPs.setInt(1, userId);
            lzPs.setInt(2, movieId);
            ResultSet lzRs = lzPs.executeQuery();
            if (lzRs.next()) return false;
            lzRs.close();

            insertPs.setInt(1, movieId);
            insertPs.setInt(2, userId);
            return insertPs.executeUpdate() == 1;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean removeMovieFromWatchlist(Integer userId, Integer movieId) {
        if (userId == null || movieId == null) return false;
        Connection conn = DB.getInstance().getConnection();
        String query = "DELETE FROM ListaZelja WHERE IdKor = ? AND IdFilm = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean isMovieInWatchlist(Integer userId, Integer movieId) {
        if (userId == null || movieId == null) return false;
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT 1 FROM ListaZelja WHERE IdKor = ? AND IdFilm = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public List<Integer> getMoviesInWatchlist(Integer userId) {
        List<Integer> result = new ArrayList<>();
        if (userId == null) return result;
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT IdFilm FROM ListaZelja WHERE IdKor = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
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
    public List<Integer> getUsersWithMovieInWatchlist(Integer movieId) {
        List<Integer> result = new ArrayList<>();
        if (movieId == null) return result;
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT IdKor FROM ListaZelja WHERE IdFilm = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
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
}
