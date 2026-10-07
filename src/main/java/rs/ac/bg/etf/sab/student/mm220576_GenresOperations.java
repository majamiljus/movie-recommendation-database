package rs.ac.bg.etf.sab.student;

import rs.ac.bg.etf.sab.operations.GenresOperations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class mm220576_GenresOperations implements GenresOperations {
    @Override
    public Integer addGenre(String name) {
        if (name == null || name.length() > 100) return null;
        Connection conn = DB.getInstance().getConnection();
        String queryZanr = "SELECT 1 FROM Zanr WHERE Naziv = ?";
        String queryInsert = "INSERT INTO Zanr(Naziv) VALUES (?)";
        try (PreparedStatement zanrPs = conn.prepareStatement(queryZanr);
             PreparedStatement insertPs = conn.prepareStatement(queryInsert, Statement.RETURN_GENERATED_KEYS)) {

            zanrPs.setString(1, name);
            ResultSet rs = zanrPs.executeQuery();
            if (rs.next()) return null;
            rs.close();

            insertPs.setString(1, name);
            if (insertPs.executeUpdate() == 0) return null;
            try (ResultSet inserRs = insertPs.getGeneratedKeys()) {
                    if(inserRs.next()){
                        return inserRs.getInt(1);
                    }
                    return null;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Integer updateGenre(Integer genreId, String name) {
        if (genreId == null || name == null || name.length() > 100) return null;
        Connection conn = DB.getInstance().getConnection();
        String query = "UPDATE Zanr SET Naziv = ? WHERE IdZanr = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name);
            ps.setInt(2, genreId);
            return ps.executeUpdate() == 1 ? genreId : null;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Integer removeGenre(Integer genreId) {
        if (genreId == null) return null;
        Connection conn = DB.getInstance().getConnection();
        String queryZf = "DELETE FROM ZanrFilma WHERE IdZanr = ?";
        String queryZanr = "DELETE FROM Zanr WHERE IdZanr = ?";
        boolean oldAutoCommit = true;
        try (PreparedStatement zfPs = conn.prepareStatement(queryZf);
             PreparedStatement zanrPs = conn.prepareStatement(queryZanr)) {
            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            zfPs.setInt(1, genreId);
            zfPs.executeUpdate();
            zanrPs.setInt(1, genreId);
            if (zanrPs.executeUpdate() != 1) {
                conn.rollback();
                return null;
            }
            conn.commit();
            return genreId;
        } catch (SQLException e) {
            try {conn.rollback();} catch (SQLException ignored) {}
            return null;
        } finally {
            try {conn.setAutoCommit(oldAutoCommit);} catch (SQLException ignored) {}
        }
    }

    @Override
    public boolean doesGenreExist(String name) {
        if (name == null || name.length() > 100) return false;
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT 1 FROM Zanr WHERE Naziv = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public Integer getGenreId(String name) {
        if (name == null || name.length() > 100) return null;
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT IdZanr FROM Zanr WHERE Naziv = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name);
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
    public List<Integer> getAllGenreIds() {
        List<Integer> result = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT IdZanr FROM Zanr";
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
}
