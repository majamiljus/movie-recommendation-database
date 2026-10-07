package rs.ac.bg.etf.sab.student;

import rs.ac.bg.etf.sab.operations.TagsOperations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class mm220576_TagsOperations implements TagsOperations {

    @Override
    public Integer addTag(Integer movieId, String tag) {
        if (movieId == null ||  tag == null || tag.length() > 100) return null;
        Connection conn = DB.getInstance().getConnection();
        String queryMovie = "SELECT 1 FROM Film WHERE IdFilm = ?";
        String queryTag = "SELECT IdTag FROM Tag WHERE Naziv = ?";
        String queryInsertTag = "INSERT INTO Tag(Naziv) VALUES (?)";
        String queryTG = "SELECT 1 FROM TagFilm WHERE IdFilm = ? AND IdTag = ?";
        String queryInsertTG = "INSERT INTO TagFilm(IdFilm, IdTag) VALUES (?, ?)";
        boolean oldAutoCommit = true;
        try (PreparedStatement moviePs = conn.prepareStatement(queryMovie);
             PreparedStatement tagPs = conn.prepareStatement(queryTag);
             PreparedStatement insertTagPs = conn.prepareStatement(queryInsertTag, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement tgPs = conn.prepareStatement(queryTG);
             PreparedStatement insertTgPs = conn.prepareStatement(queryInsertTG)) {

            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            moviePs.setInt(1, movieId);
            ResultSet rs = moviePs.executeQuery();
            if (!rs.next()) {
                rs.close();
                conn.rollback();
                return null;
            }
            Integer tagId = null;
            tagPs.setString(1, tag);
            ResultSet tagRs = tagPs.executeQuery();
            if (tagRs.next()) {
                tagId = tagRs.getInt(1);
            }
            if (tagId == null) {
                insertTagPs.setString(1, tag);
                if (insertTagPs.executeUpdate() != 1) {
                    conn.rollback();
                    return null;
                }
                ResultSet inserRs = insertTagPs.getGeneratedKeys();
                    if (!inserRs.next()) {
                        inserRs.close();
                        conn.rollback();
                        return null;
                    }
                    tagId = inserRs.getInt(1);
            }

            tgPs.setInt(1, movieId);
            tgPs.setInt(2, tagId);
            ResultSet tgRs = tgPs.executeQuery();
            if(tgRs.next()) {
                    tgRs.close();
                    conn.rollback();
                    return null;
            }

            insertTgPs.setInt(1, movieId);
            insertTgPs.setInt(2, tagId);
            if (insertTgPs.executeUpdate() != 1) {
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
    public Integer removeTag(Integer movieId, String tag) {
        if (movieId == null ||  tag == null || tag.length() > 100) return null;
        Connection conn = DB.getInstance().getConnection();
        String query =  "DELETE tf " +
                        "FROM TagFilm tf " +
                        "JOIN Tag t ON t.IdTag = tf.IdTag " +
                        "WHERE tf.IdFilm = ? AND t.Naziv = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            ps.setString(2, tag);
            return ps.executeUpdate() == 1 ? movieId : null;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public int removeAllTagsForMovie(Integer movieId) {
        if (movieId == null) return 0;
        Connection conn = DB.getInstance().getConnection();
        String query = "DELETE FROM TagFilm WHERE IdFilm = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            return 0;
        }
    }

    @Override
    public boolean hasTag(Integer movieId, String tag) {
        if (movieId == null || tag == null || tag.length() > 100) return false;

        Connection conn = DB.getInstance().getConnection();
        String query =  "SELECT 1 " +
                        "FROM TagFilm tf " +
                        "JOIN Tag t ON t.IdTag = tf.IdTag " +
                        "WHERE tf.IdFilm = ? AND t.Naziv = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            ps.setString(2, tag);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public List<String> getTagsForMovie(Integer movieId) {
        List<String> result = new ArrayList<>();
        if (movieId == null) return result;
        Connection conn = DB.getInstance().getConnection();
        String query =  "SELECT t.Naziv " +
                        "FROM TagFilm tf " +
                        "JOIN Tag t ON t.IdTag = tf.IdTag " +
                        "WHERE tf.IdFilm = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String naziv = rs.getString(1);
                result.add(naziv);
            };
            return result;
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Integer> getMovieIdsByTag(String tag) {
        List<Integer> result = new ArrayList<>();
        if (tag == null || tag.length() > 100) return result;
        Connection conn = DB.getInstance().getConnection();
        String query =  "SELECT tf.IdFilm " +
                        "FROM TagFilm tf " +
                        "JOIN Tag t ON t.IdTag = tf.IdTag " +
                        "WHERE t.Naziv = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, tag);
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
    public List<String> getAllTags() {
        List<String> result = new ArrayList<>();
        Connection conn = DB.getInstance().getConnection();
        String query = "SELECT Naziv FROM Tag";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String naziv = rs.getString(1);
                result.add(naziv);
            }
            return result;
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }
}
