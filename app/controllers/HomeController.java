package controllers;

import classes.Url;
import models.InitialPath;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.*;

import views.html.*;

import javax.inject.Inject;
import java.util.List;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {
    private final Form<Url> urlForm;

    @Inject
    public HomeController(FormFactory formFactory) {
        this.urlForm = formFactory.form(Url.class);
    }

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        List<InitialPath> initPaths = InitialPath.find.all();

        return ok(index.render("Getting to Philosophy! - Donald Thai", urlForm, null, initPaths));
    }

}
