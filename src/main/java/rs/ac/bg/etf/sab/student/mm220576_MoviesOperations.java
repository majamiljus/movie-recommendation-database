package rs.ac.bg.etf.sab.student;

import rs.ac.bg.etf.sab.operations.MoviesOperations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class mm220576_MoviesOperations implements MoviesOperations {

    @Override
    public Integer addMovie(String title, Integer genreId, String director) {
        if (title == null || title.length() > 100 || genreId == null || director == null || director.length() > 100) return null;
        Connection conn = DB.getInstance().getConnection();
        String queryZanr = "SELECT 1 FROM Zanr WHERE IdZanr = ?";
        String queryInsertF = "INSERT INTO Film(Naslov, Reziser) VALUES (?, ?)";
        String queryInsertZF = "INSERT INTO ZanrFilma(IdFilm, IdZanr) VALUES (?, ?)";
        boolean oldAutoCommit = true;
        try (PreparedStatement zanrPs = conn.prepareStatement(queryZanr);
             PreparedStatement filmPs = conn.prepareStatement(queryInsertF, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement zfPs = conn.prepareStatement(queryInsertZF)) {

            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            zanrPs.setInt(1, genreId);
            ResultSet zanrRs = zanrPs.executeQuery();
            if (!zanrRs.next()) {
                zanrRs.close();
                conn.rollback();
                return null;
            }

            filmPs.setString(1, title);
            filmPs.setString(2, director);
            if (filmPs.executeUpdate() != 1) {
                conn.rollback();
                return null;
            }

            ResultSet filmRs = filmPs.getGeneratedKeys();
            if (!filmRs.next()) {
                filmRs.close();
                conn.rollback();
                return null;
            }
            int movieId = filmRs.getInt(1);
            zfPs.setInt(1, movieId);
            zfPs.setInt(2, genreId);
            if (zfPs.executeUpdate() != 1) {
                conn.rollback();
                return null;
            }
            conn.commit();
            return movieId;
        } catch (SQLException e) {
            try {conn.rollback();} catch (SQLException ignored) {}
            return null;
        } finally {
            try {conn.setAutoCommit(oldAutoCommit);} catch (SQLException ignored) {}
        }
    }

    @Override
    public Integer updateMovieTitle(Integer movieId, String title) {
        if (movieId == null || title == null || title.length() > 100) return null;
        Connection conn = DB.getInstance().getConnection();
        String query = "UPDATE Film SET Naslov = ? WHERE IdFilm = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, title);
            ps.setInt(2, movieId);
            return ps.executeUpdate() == 1 ? movieId : null;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Integer addGenreToMovie(Integer movieId, Integer genreId) {
        if (movieId == null || genreId == null) return null;
        Connection conn = DB.getInstance().getConnection();
        String queryF = "SELECT 1 FROM Film WHERE IdFilm = ?";
        String queryZ = "SELECT 1 FROM Zanr WHERE IdZanr = ?";
        String queryZF = "SELECT 1 FROM ZanrFilma WHERE IdFilm = ? AND IdZanr = ?";
        String queryInsertZF = "INSERT INTO ZanrFilma(IdFilm, IdZanr) VALUES (?, ?)";
        try (PreparedStatement filmPs = conn.prepareStatement(queryF);
             PreparedStatement zanrPs = conn.prepareStatement(queryZ);
             PreparedStatement zfPs = conn.prepareStatement(queryZF);
             PreparedStatement insertPs = conn.prepareStatement(queryInsertZF)) {

            filmPs.setInt(1, movieId);
            ResultSet filmRs = filmPs.executeQuery();
            if (!filmRs.next()) return null;
            filmRs.close();

            zanrPs.setInt(1, genreId);
            ResultSet zanrRs =  zanrPs.executeQuery();
            if (!zanrRs.next()) return null;
            zanrRs.close();

            zfPs.setInt(1, movieId);
            zfPs.setInt(2, genreId);
            ResultSet zfRs = zfPs.executeQuery();
            if (zfRs.next()) return null;
            zfRs.close();

            insertPs.setInt(1, movieId);
            insertPs.setInt(2, genreId);
            return insertPs.executeUpdate() == 1 ? movieId : null;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Integer removeGenreFromMovie(Integer movieId, Integer genreId) {
        if (movieId == null || genreId == null) return null;
        Connection conn = DB.getInstance().getConnection();
        String query = "DELETE FROM ZanrFilma WHERE IdFilm = ? AND IdZanr = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            ps.setInt(2, genreId);
            return ps.executeUpdate() == 1 ? movieId : null;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Integer updateMovieDirector(Integer movieId, String director) {
        if (movieId == null ||  director == null || director.length() > 100) return null;
        Connection conn = DB.getInstance().getConnection();
        String query = "UPDATE Film SET Reziser = ? WHERE IdFilm = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, director);
            ps.setInt(2, movieId);
            return ps.executeUpdate() == 1 ? movieId : null;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Integer removeMovie(Integer movieId) {
        if (movieId == null) return null;
        Connection conn = DB.getInstance().getConnection();
        String queryTF = "DELETE FROM TagFilm WHERE IdFilm = ?";
        String queryLZ = "DELETE FROM ListaZelja WHERE IdFilm = ?";
        String queryOcena = "DELETE FROM Ocena WHERE IdFilm = ?";
        String queryZf = "DELETE FROM ZanrFilma WHERE IdFilm = ?";
        String queryFilm = "DELETE FROM Film WHERE IdFilm = ?";
        boolean oldAutoCommit = true;
        try (PreparedStatement tfPs = conn.prepareStatement(queryTF);
             PreparedStatement lzPs = conn.prepareStatement(queryLZ);
             PreparedStatement ocenaPs = conn.prepareStatement(queryOcena);
             PreparedStatement zanrPs = conn.prepareStatement(queryZf);
             PreparedStatement filmPs = conn.prepareStatement(queryFilm)) {

            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            tfPs.setInt(1, movieId);
            tfPs.executeUpdate();
            lzPs.setInt(1, movieId);
            lzPs.executeUpdate();
            ocenaPs.setInt(1, movieId);
            ocenaPs.executeUpdate();
            zanrPs.setInt(1, movieId);
            zanrPs.executeUpdate();
            filmPs.setInt(1, movieId);
            if (filmPs.executeUpdate() != 1) {
                conn.rollback();
                return null;
            }
            conn.commit();
            return movieId;
        } catch (SQLException e) {
            try {conn.rollback();} catch (SQLException ignored) {}
            return null;
        } finally {
            try {conn.setAutoCommit(oldAutoCommit);} catch (SQLException ignored) {}
        }
    }

    @Override
    public List<Integer> getMovieIds(String title, String director) {
        List<Integer> result = new ArrayList<>();
        if ( title == null || title.length() > 100 ||  director == null || director.length() > 100) return result;
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT IdFilm FROM Film WHERE Naslov = ? AND Reziser = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, title);
            ps.setString(2, director);
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
    public List<Integer> getAllMovieIds() {
        List<Integer> result = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT IdFilm FROM Film";
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
    public List<Integer> getMovieIdsByGenre(Integer genreId) {
        List<Integer> result = new ArrayList<>();
        if (genreId == null) return result;
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT IdFilm FROM ZanrFilma WHERE IdZanr = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, genreId);
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
    public List<Integer> getGenreIdsForMovie(Integer movieId) {
        List<Integer> result = new ArrayList<>();
        if (movieId == null) return result;
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT IdZanr FROM ZanrFilma WHERE IdFilm = ?";
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

    @Override
    public List<Integer> getMovieIdsByDirector(String director) {
        List<Integer> result = new ArrayList<>();
        if (director == null || director.length() > 100) return result;
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT IdFilm FROM Film WHERE Reziser = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, director);
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
    public String getMovieTrend(Integer movieId) {
        if (movieId == null) return null;
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT Trend FROM Film WHERE IdFilm = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()){
                    return rs.getString(1);
                }
                return null;
            }
        } catch (SQLException e) {
            return null;
        }
    }
}
