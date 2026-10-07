package rs.ac.bg.etf.sab.student;

import rs.ac.bg.etf.sab.operations.GeneralOperations;

import java.sql.*;

public class mm220576_GeneralOperations implements GeneralOperations {
    @Override
    public void eraseAll() {
        Connection conn = DB.getInstance().getConnection();
        try(Statement s = conn.createStatement();) {
            s.executeUpdate("delete from TagFilm");
            s.executeUpdate("delete from ZanrFilma");
            s.executeUpdate("delete from ListaZelja");
            s.executeUpdate("delete from Ocena");
            s.executeUpdate("delete from Film");
            s.executeUpdate("delete from Tag");
            s.executeUpdate("delete from Korisnik");
            s.executeUpdate("delete from Zanr");
            s.executeUpdate("DBCC CHECKIDENT ('Korisnik', RESEED, 0)");
            s.executeUpdate("DBCC CHECKIDENT ('Film', RESEED, 0)");
            s.executeUpdate("DBCC CHECKIDENT ('Zanr', RESEED, 0)");
            s.executeUpdate("DBCC CHECKIDENT ('Tag', RESEED, 0)");
            s.executeUpdate("DBCC CHECKIDENT ('Ocena', RESEED, 0)");
        } catch (SQLException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
