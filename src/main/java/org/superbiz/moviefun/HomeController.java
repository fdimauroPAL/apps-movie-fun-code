package org.superbiz.moviefun;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.superbiz.moviefun.albums.Album;
import org.superbiz.moviefun.albums.AlbumFixtures;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.Movie;
import org.superbiz.moviefun.movies.MovieFixtures;
import org.superbiz.moviefun.movies.MoviesBean;

import java.util.Map;

@Controller
public class HomeController {

    private final MoviesBean moviesBean;
    private final AlbumsBean albumsBean;
    private final MovieFixtures movieFixtures;
    private final AlbumFixtures albumFixtures;
    private final PlatformTransactionManager albumstransactionmanager;
    private final PlatformTransactionManager moviestransactionmanager;

    public HomeController(MoviesBean moviesBean, AlbumsBean albumsBean, MovieFixtures movieFixtures, AlbumFixtures albumFixtures, @Qualifier("albumsTransactionManager") PlatformTransactionManager albumsTransactionManager, @Qualifier("moviesTransactionManager") PlatformTransactionManager moviesTransactionManager) {
        this.moviesBean = moviesBean;
        this.albumsBean = albumsBean;
        this.movieFixtures = movieFixtures;
        this.albumFixtures = albumFixtures;
        this.moviestransactionmanager=moviesTransactionManager;
        this.albumstransactionmanager=albumsTransactionManager;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/setup")
    public String setup(Map<String, Object> model) {

        TransactionTemplate moviestemplate = new TransactionTemplate(moviestransactionmanager);
        moviestemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    for (Movie movie : movieFixtures.load()) {
                        moviesBean.addMovie(movie);
                    }
                    model.put("movies", moviesBean.getMovies());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        });


        TransactionTemplate albumstemplate = new TransactionTemplate(albumstransactionmanager);
        albumstemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    for (Album album : albumFixtures.load()) {
                        albumsBean.addAlbum(album);
                    }
                    model.put("albums", albumsBean.getAlbums());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        });




        return "setup";
    }
}
