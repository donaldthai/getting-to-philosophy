package controllers;

import classes.Url;
import io.ebean.Ebean;
import models.InitialPath;
import models.PhilPath;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;

//import play.db.Database;

/**
 * Getting to Philosophy!
 * Some Wikipedia links are case-sensitive so we'll consider links as is.
 * Rules that our crawler is following:
 * - Clicking on the first non-parenthesized, non-italicized link
 * - Ignoring external links, links to the current page, or red links (links to non-existent pages)
 * - Stopping when reaching "Philosophy", a page with no links or a page that does not exist, or when a loop occurs
 *
 * Notes:
 * Noticed that Xfer's site has a slight different when traversing the first links.
 * Xfer's site actually goes to the next link if current link has been visited.
 * Decided to follow the rules defined in the Getting to Philosophy article. So we
 * actually stop traversing if we come across a link we've seen before to prevent loops.
 */
@Singleton
public class PhilController extends Controller {
    private final String philosophyUrl = "https://en.wikipedia.org/wiki/Philosophy"; //to lowercase
    private final Form<Url> urlForm;


    @javax.inject.Inject
    public PhilController(FormFactory formFactory) {
        this.urlForm = formFactory.form(Url.class);
    }

    /**
     * Getting to Philosophy
     * GET
     * GetToPhil grabs the "url" parameter to tries to walk the links.
     * @return index view with the philosophy paths and # of hops
     */
    public Result GetToPhil() {
        //Let's grab the query string
        //store links seen before
        HashMap<String, Integer> linksMap = new HashMap<String, Integer>();
        String url = request().getQueryString("url");
        url = url.trim();
        String output = "";
        Boolean isInitialSearch = true;
        try {
            Integer hops = 0;

            //check if valid url
            if (!IsWikipediaArticle(url)) {
                throw new Exception("Not a valid Wikipedia article url!");
            }
            else if (IsPhilosophy(url)) {
                //we don't have to try to get the document
                //1 hop
                //save to database
                return ok("Philosphy Article!");
            }

            //see if we need to add the http prefix
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                //add the prefix
                url = "https://" + url;
                System.out.println("Https prefix added!");
            }

            String nextUrl = new String(url);
            InitialPath savedInitPath = null;
            //count link
            while (nextUrl != null) {
                //try to get document
                Document doc = Jsoup.connect(nextUrl).get();

                //check if link we've already seen
                //break
                if (linksMap.containsKey(doc.baseUri())) {
                    //Cycle, doesn't count
                    hops--;
                    output += "(HOPS:"+ hops +", CYCLE DECECTED)";
                    break;
                }
                else {
                    linksMap.put(nextUrl, 1);
                }

                String title = doc.selectFirst("#firstHeading").text();

                //check if we already have an initial path for this in the db
                if (isInitialSearch) {
                    InitialPath ip = findInitialPath(doc.baseUri());
                    //System.out.println(ip.id);
                    if (ip != null) {
                        savedInitPath = ip;
                        //number of hops we can update at the end

                        output += ip.title + "  >  ";
                        //path already in db
                        //grab all the paths and output
                        List<PhilPath> listPhilPaths = findPhilPaths(ip.id);
                        System.out.println(listPhilPaths);
                        if (listPhilPaths != null){
                            for (Integer i = 0; i < listPhilPaths.size(); i++ ) {
                                output += listPhilPaths.get(i).title;
                                if (i != listPhilPaths.size() - 1) {
                                    output += "  >  ";
                                } else {
                                    output += " (HOPS: "+ listPhilPaths.size() +")";
                                }
                            }
                        }

                        break;
                    } else {
                        //ToDo: I suppose we could encode the url....
                        //save it
                        saveInitPath(title, doc.baseUri());
                        savedInitPath = findInitialPath(doc.baseUri());
                    }

                    isInitialSearch = false;
                } else {
                    //save phil path
                    System.out.println(savedInitPath.id);
                    savePhilPath(savedInitPath.id, title, doc.baseUri());
                }

                if (IsPhilosophy(doc.baseUri())) {
                    output += title + " (HOPS: " + hops + ")";
                    break;
                }

                output += title + "  >  ";

                //grab all the links in the main body
                Elements wikiMainBodyLinks = doc.select("#content #bodyContent #mw-content-text a");
                //get the first valid link we should walk
                Element validLink = GetFirstValidLink(wikiMainBodyLinks, nextUrl);

                //check if link is the philosphy article
                if (validLink == null) {
                    System.out.println("No valid links");
                    break;
                }

                nextUrl = validLink.absUrl("href");
                hops++;
                System.out.println("Next URL: " + nextUrl + " " + validLink.cssSelector());
            }
            
            //update hops
            updateInitPathHops(savedInitPath);
        } catch(Exception ex) {
            System.out.println(ex);
            return badRequest("Uh oh!");
        }

        //Debug
        System.out.println(output);

        urlForm.fill(new Url("bob@gmail.com"));
        return ok(index.render("Your result!", urlForm, output, InitialPath.find.all()));//Json.toJson(newsHeadlines));
    }

    /**
     * Checks if url is a philosophy article
     * @param url string url passed in
     * @return true, philosophy article. false, otherwise
     */
    private boolean IsPhilosophy(String url) {
        if (url.contains(philosophyUrl)) {
            return true;
        }

        return false;
    }

    /**
     * Get the first valid link according to our criteria
     * @param links A list of links
     * @return First valid link, otherwise null
     */
    private Element GetFirstValidLink(Elements links, String prevUrl) {
        for (Element link : links) {
            //need to check if link is between a parenthesis
            Element parent = link.parent();
            if (parent != null) {
                //ToDo: Could refine the logic here more...
                String beforeLink = parent.outerHtml().split(link.outerHtml())[0];
                //System.out.println(beforeLink);
                int openParen = beforeLink.split("[(]").length;
                int closeParen = beforeLink.split("[)]").length;
                //System.out.println(beforeLink.split("[(]")[0]);
                //System.out.println(beforeLink.split("[)]")[0]);
                if (openParen > closeParen) {
                    continue;
                }
            }
            if (IsValidLink(link) && link.absUrl("href") != prevUrl) {
                //System.out.println(link.attr("title") + " " + link.absUrl("href") + " " + link.cssSelector());
                return link;
            }
        }
        return null;
    }

    /**
     * Checks if url is a wikipedia article
     * @param url string url passed in
     * @return true, valid wikipedia article we're looking for. false, otherwise
     */
    private boolean IsWikipediaArticle(String url) {
        String u = new String(url);
        if (u.contains("https")) {
            u = u.replaceFirst("https://", "");
        }
        else if (url.contains("http")) {
            u = u.replaceFirst("http://", "");
        }

        //System.out.println(u);

        if (u.startsWith("en.wikipedia.org/wiki")) {
            return true;
        }

        return false;
    }

    /**
     * ToDo: I'm sure there's a better way of handling what type of links there are...
     * Check if link is a valid Wikipedia article link we're looking for
     * @return true, valid link we're looking for. false, otherwise.
     */
    private boolean IsValidLink(Element link) {
        String absUrl = link.absUrl("href").toLowerCase();
        String cssSelector = link.cssSelector().toLowerCase();
        if (absUrl.startsWith("https://en.wikipedia.org/wiki")
                && !absUrl.contains("action=edit")
                && !absUrl.contains("#")
                && !absUrl.contains("file")
                && !absUrl.contains("portal:")
                && !cssSelector.contains("div.hatnote.navigation-not-searchable")
                && !cssSelector.contains("a.extiw")
                && !cssSelector.contains("a.image")
                && !cssSelector.contains("a.mw-redirect")
                && !cssSelector.contains("span.ipa")
                && !cssSelector.contains("plainlinks")
                && !cssSelector.contains("small")
                && !cssSelector.contains("div.thumb")
                && !cssSelector.contains("table")) {
            return true;
        }

        return false;
    }

    /**
     * eBean
     * Save new initial path
     * @param title title of the article
     * @param url url associated with
     */
    private void saveInitPath(String title, String url) {
        InitialPath ip = new InitialPath();
        ip.title = title;
        ip.url = url;
        ip.hops = 0;
        ip.save();
    }

    /**
     * Updates the initial path entity
     * @param ip Initial path entity
     */
    private void updateInitPathHops(InitialPath ip) {
        if (ip != null) {
            ip.update();
        }
    }

    /**
     * eBean
     * Save new phil path
     * @param initPathId initial path id
     * @param title title of the article
     * @param url url associated with
     */
    private void savePhilPath(Integer initPathId, String title, String url) {
        PhilPath pp = new PhilPath();
        pp.initpathid = initPathId;
        pp.title = title;
        pp.url = url;
        pp.save();
    }

    /**
     * Find initial path in the InitialPath Table
     * @param url Unique url
     * @return
     */
    private InitialPath findInitialPath(String url) {
        InitialPath initPath = Ebean.find(InitialPath.class)
                .where()
                .eq("url", url)
                .findOne();
        return initPath;
    }

    /**
     * Find all philosophy paths given the initial path taken
     * @param initPathId initial path id
     * @return list of paths
     */
    private List<PhilPath> findPhilPaths(Integer initPathId) {
        //.out.println(initPathId);
        List<PhilPath> philPath = Ebean.find(PhilPath.class)
                .where()
                .eq("initPathId", initPathId)
                .findList();
        return philPath;
    }
}
