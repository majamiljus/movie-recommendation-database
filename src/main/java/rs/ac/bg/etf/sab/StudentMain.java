
package rs.ac.bg.etf.sab;

import rs.ac.bg.etf.sab.operations.*;
import rs.ac.bg.etf.sab.student.*;
import rs.ac.bg.etf.sab.tests.TestHandler;
import rs.ac.bg.etf.sab.tests.TestRunner;

public class StudentMain {
    public static void main(String[] args) throws Exception {
// Uncomment and change fallowing lines
          GeneralOperations generalOperations = new mm220576_GeneralOperations();
          GenresOperations genresOperations = new mm220576_GenresOperations();
          MoviesOperations moviesOperations = new mm220576_MoviesOperations();
          RatingsOperations ratingsOperation = new mm220576_RatingsOperations();
          TagsOperations tagsOperations = new mm220576_TagsOperations();
          UsersOperations usersOperations = new mm220576_UsersOperations();
          WatchlistsOperations watchlistsOperations = new mm220576_WatchlistsOperations();

        TestHandler.createInstance(
                genresOperations,
                moviesOperations,
                ratingsOperation,
                tagsOperations,
                usersOperations,
                watchlistsOperations,
                generalOperations);
        TestRunner.runTests();
    }
}