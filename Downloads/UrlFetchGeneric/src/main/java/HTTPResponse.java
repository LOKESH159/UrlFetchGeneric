public class HTTPResponse {

    private int responseCode;

    private String data;

    public int getResponseCode() {
        return responseCode;
    }

    public HTTPResponse setResponseCode(int responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    public String getData() {
        return data;
    }

    public HTTPResponse setData(String data) {
        this.data = data;
        return this;
    }
}
