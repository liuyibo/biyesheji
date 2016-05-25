package liuyibo;

import java.net.URLEncoder;

/**
 * Created by liuyibo on 16-3-2.
 */
public class URLBuilder {
    private StringBuilder sb;

    public URLBuilder param(String key, String... value) {
        if (key == null || value == null || key.isEmpty() || value.length == 0) {
            return this;
        }
        if (sb != null) {
            sb.append("&");
        } else {
            sb = new StringBuilder();
        }
        sb.append(URLEncoder.encode(key));
        sb.append("=");
        sb.append(URLEncoder.encode(value[0]));
        for (int i = 1; i < value.length; i++) {
            sb.append("|");
            sb.append(URLEncoder.encode(value[i]));
        }
        return this;
    }

    /**
     * build url with params
     * @param url
     * @return
     */
    public String build(String url) {
        if (sb == null) {
            return url;
        } else {
            return url + "?" + sb.toString();
        }
    }

    /**
     * build a param string
     * @return
     */
    public String buildParams() {
        return sb.toString();
    }

}
