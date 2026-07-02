package mapping;

import java.util.Objects;

import http.HttpMethode;

public class UrlMethode {
    String url;
    HttpMethode methode;

    public UrlMethode(String url, HttpMethode methode) {
        this.url = url;
        this.methode = methode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        UrlMethode toCompare = (UrlMethode) obj;

        return this.url.equals(toCompare.getUrl()) && this.methode.equals(toCompare.getMethode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, methode);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HttpMethode getMethode() {
        return methode;
    }

    public void setMethode(HttpMethode methode) {
        this.methode = methode;
    }

}