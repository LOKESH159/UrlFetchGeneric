import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class HttpURLService {

    private static final String CHARSET = "UTF-8";

    private static final int CONNECTION_TIMEOUT = 60000;

    private static final int READ_TIMEOUT = 60000;

    private HttpURLConnection httpURLConnection;

    private OutputStream output;

    private String url;

    private HTTPMethod requestMethod;

    private Map<String, String> headers;

    private String payload;

    static {
        allowMethods("PATCH");
    }

    public HttpURLService() {

    }

    public static HttpURLService getInstance() {
        return new HttpURLService();
    }

    public HttpURLService setUrl(String url) throws Exception {

        if (!isNotNullOrEmpty(url))
            throw new Exception("Response url :: " + url);

        this.url = url;

        return this;
    }

    public HttpURLService setRequestMethod(HTTPMethod requestMethod) throws Exception {
        if (requestMethod == null)
            throw new Exception("Request method :: " + requestMethod);

        this.requestMethod = requestMethod;

        return this;
    }

    public HttpURLService setHeaders(Map<String, String> headers) {

        this.headers = headers;

        return this;
    }

    public HttpURLService setPayload(String payload) throws Exception {
        if (isNotNullOrEmpty(payload))
            this.payload = payload;

        return this;

    }

    public HTTPResponse execute() throws Exception {

        try {

            makeConnection();
            int statusCode = httpURLConnection.getResponseCode();

            if (statusCode >= 300)
                throw new RuntimeException("Response status ::" + statusCode);
            return buildResponse(statusCode, readInputStream(httpURLConnection.getInputStream()));
        } finally {
            if (httpURLConnection != null)
                httpURLConnection.disconnect();
        }
    }

    private void makeConnection() throws IOException {
        this.httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
        httpURLConnection.setConnectTimeout(CONNECTION_TIMEOUT);
        httpURLConnection.setUseCaches(false);

        setRequestMethod();
    }

    private void setRequestMethod() throws IOException {
        httpURLConnection.setRequestMethod(requestMethod.toString());
        setHeaders();
    }

    private void setHeaders() throws IOException {

        if (headers != null)
            headers.forEach((headerName, headerValue) -> httpURLConnection.setRequestProperty(headerName, headerValue));
        writeOutput();
    }

    private void writeOutput() throws IOException {

        if (payload != null) {
            try {
                httpURLConnection.setDoOutput(true);
                output = httpURLConnection.getOutputStream();
                output.write(payload.getBytes(CHARSET));
            } finally {
                if (output != null)
                    output.close();
            }
        }
    }

    private HTTPResponse buildResponse(int status_code, String response_data) throws Exception {
        return new HTTPResponse().setResponseCode(status_code).setData(response_data);


    }


    /**
     * @param methods
     */
    private static void allowMethods(String... methods) {
        try {
            Field methodsField = HttpURLConnection.class.getDeclaredField("methods");

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(methodsField, methodsField.getModifiers() & ~Modifier.FINAL);

            methodsField.setAccessible(true);

            String[] oldMethods = (String[]) methodsField.get(null);
            Set<String> methodsSet = new LinkedHashSet<>(Arrays.asList(oldMethods));
            methodsSet.addAll(Arrays.asList(methods));
            String[] newMethods = methodsSet.toArray(new String[0]);

            methodsField.set(null/*static field*/, newMethods);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }


    private static boolean isNotNullOrEmpty(String data){
        if (data != null && !data.isEmpty())
            return true;
        return false;
    }


    public static String readInputStream(InputStream inputStream) throws IOException {
        if (inputStream == null)
            throw new RuntimeException("Invalid data, Input stream can't be null");
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1)
                result.write(buffer, 0, length);

            return String.valueOf(result);
        } finally {
            if (inputStream != null)
                inputStream.close();
        }
    }



}
