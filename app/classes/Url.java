package classes;

public class Url {
    protected String url;

    public Url() {}

    public Url(String url) {
        setUrl(url);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
