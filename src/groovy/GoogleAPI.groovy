import grails.converters.JSON
import grails.util.Holders
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by prashant on 6/6/17.
 */
class GoogleAPI {

    private static final String BUCKET_NAME = "fin360"

    // to fetch access token using from refresh token
    public static String fetchAccessTokenUsingRefreshToken() {
        def grailsApplication = Holders.grailsApplication.mainContext.getBean 'grailsApplication'
        String googleAccessTokenURL = "https://www.googleapis.com/oauth2/v4/token"
        HttpURLConnection con = null;
        OutputStreamWriter writer = null;
        String accessToken = null
        String urlParameters =
                "client_id=" + URLEncoder.encode(grailsApplication.config.grails.googleStorage.clientId, "UTF-8") +
                        "&client_secret=" + URLEncoder.encode(grailsApplication.config.grails.googleStorage.clientSecret, "UTF-8") +
                        "&refresh_token=" + URLEncoder.encode(grailsApplication.config.grails.googleStorage.refreshToken, "UTF-8") +
                        "&grant_type=" + URLEncoder.encode("refresh_token", "UTF-8")
        String completeURL = googleAccessTokenURL
        Map<String, String> requestProperty = ["Accept": "application/json", "Content-Type": "application/x-www-form-urlencoded"]
        Map<String, String> response = makeHTTPRequest(completeURL, "POST", urlParameters, requestProperty)
        String apiResponse = response.get("apiResponse")
        String apiReponseCode = response.get("apiReponseCode")
        if (Integer.parseInt(apiReponseCode) == 200) {
            JSONObject userJson = JSON.parse(apiResponse)
            accessToken = userJson.access_token
        } else {
            //send email to admin with response "apiResponse" and code "con.getResponseCode()"
            println "Please Try After sometime"
        }
        println accessToken + " accessToken*******************"
        return accessToken
    }


    public static boolean callGoogleStorageAPI(String accessToken, File file) {
        println file.getName() + " <=========== fileName to upload"
        boolean uploadSuccessful = false
        URLConnection conn = null;
        OutputStream os = null;
        InputStream is = null;
//        String[] fileNameArray = fileNameWithPath.split("/")
        String fileName = file.getName()
        File inputFile = file
        FileInputStream fis = new FileInputStream(inputFile);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            for (int readNum; (readNum = fis.read(buf)) != -1;) {
                bos.write(buf, 0, readNum);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace()
        }
        byte[] inputBytes = bos.toByteArray()
        String[] imageFileExtensionArray = fileName.split("\\.")
        String imageFileExtensionType = imageFileExtensionArray[imageFileExtensionArray.length - 1]
        URL urlObj = new URL("https://www.googleapis.com/upload/storage/v1/b/" + BUCKET_NAME + "/o?uploadType=multipart&name=" + fileName + "&predefinedAcl=publicRead")
        conn = (HttpURLConnection) urlObj.openConnection()
        conn.setDoInput(true)
        conn.setDoOutput(true)
        conn.setRequestMethod("POST")
        if (imageFileExtensionType.equals("JPG") || imageFileExtensionType.equals("jpg") || imageFileExtensionType.equals("JPEG"))
            conn.setRequestProperty("Content-Type", "image/jpeg")
        else if (imageFileExtensionType.equals("png"))
            conn.setRequestProperty("Content-Type", "image/png")
        conn.setRequestProperty("Authorization", "Bearer " + accessToken)
        os = conn.getOutputStream();
        os.write(inputBytes)
        println conn.getResponseCode() + " response code to store an image "

        if (conn.getResponseCode() == 200) {
            uploadSuccessful = true
        } else {
            print("Please Try After sometime")
        }
        return uploadSuccessful
    }

//to store image in a bucket
    public static boolean callGoogleStorageAPI(String accessToken, String fileNameWithPath) {
        println fileNameWithPath + " <=========== file NameWithPath to upload"
        boolean uploadSuccessful = false
        URLConnection conn = null;
        OutputStream os = null;
        InputStream is = null;
        String[] fileNameArray = fileNameWithPath.split("/")
        String fileName = fileNameArray[fileNameArray.length - 1]
        File inputFile = new File(fileNameWithPath);
        FileInputStream fis = new FileInputStream(inputFile);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            for (int readNum; (readNum = fis.read(buf)) != -1;) {
                bos.write(buf, 0, readNum);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace()
        }
        byte[] inputBytes = bos.toByteArray()
        String[] imageFileExtensionArray = fileName.split("\\.")
        String imageFileExtensionType = imageFileExtensionArray[imageFileExtensionArray.length - 1]
        URL urlObj = new URL("https://www.googleapis.com/upload/storage/v1/b/" + BUCKET_NAME + "/o?uploadType=multipart&name=" + fileName + "&predefinedAcl=publicRead")
        conn = (HttpURLConnection) urlObj.openConnection()
        conn.setDoInput(true)
        conn.setDoOutput(true)
        conn.setRequestMethod("POST")
        if (imageFileExtensionType.equals("JPG") || imageFileExtensionType.equals("jpg") || imageFileExtensionType.equals("JPEG"))
            conn.setRequestProperty("Content-Type", "image/jpeg")
        else if (imageFileExtensionType.equals("png"))
            conn.setRequestProperty("Content-Type", "image/png")
        conn.setRequestProperty("Authorization", "Bearer " + accessToken)
        os = conn.getOutputStream();
        os.write(inputBytes)
        println conn.getResponseCode() + " response code to store an image "

        if (conn.getResponseCode() == 200) {
            uploadSuccessful = true
        } else {
            print("Please Try After sometime")
        }
        return uploadSuccessful
    }

//to delete an image
    public static boolean deleteImageFromGoogleStorage(String accessToken, String fileName) {
        boolean deleteSuccessful = false
        String completeURL = "https://www.googleapis.com/storage/v1/b/" + BUCKET_NAME + "/o/" + fileName
        Map<String, String> requestProperty = ["Authorization": "Bearer " + accessToken]
        Map<String, String> response = makeHTTPRequest(completeURL, "DELETE", "", requestProperty)
        String apiReponseCode = response.get("apiReponseCode")
        println apiReponseCode + " response code for deleting image"
        if (Integer.parseInt(apiReponseCode) == 204) {
            deleteSuccessful = true
        } else {
            print("Please Try After sometime")
        }
        return deleteSuccessful
    }

//to fetch image test data from bucket
    public static String fetchImageTextData(String fileName) {
        def grailsApplication = Holders.grailsApplication.mainContext.getBean 'grailsApplication'
        println fileName + " <=============== fileName to fetch text"
        ArrayList<ArrayList> listdata = new ArrayList<>()
        HttpURLConnection con = null;
        String inputFileURL = "gs://" + BUCKET_NAME + "/" + fileName;
        OutputStreamWriter writer = null;
        String inputData = "{\"requests\":[  {\"image\":{  \"source\":{ \"gcs_image_uri\":\"" + inputFileURL + "\"}},\"features\":[  {\"type\":\"TEXT_DETECTION\"   }  ]}  ]}"
        String googleVisionURL = "https://vision.googleapis.com/v1/images:annotate"
        String completeURL = googleVisionURL + "?key=" + grailsApplication.config.grails.googleStorage.apiKey
        Map<String, String> requestProperty = ["Accept": "application/json", "Content-Type": "application/json"]
        Map<String, String> response = makeHTTPRequest(completeURL, "POST", inputData, requestProperty)
        String apiResponse = response.get("apiResponse")
        println "*******fetch Image Text ApiResponse : " + apiResponse
        return apiResponse
    }

    public static Map<String, String> makeHTTPRequest(String inputURL, String method,
                                                      def inputData, Map<String, String> requestProperty) {
        HttpURLConnection con = null
        OutputStreamWriter writer = null
        URL obj = new URL(inputURL)
        con = (HttpURLConnection) obj.openConnection()
        con.setDoInput(true)
        con.setDoOutput(true)
        con.setRequestMethod(method)
        requestProperty.each { k, v ->
            con.setRequestProperty("${k}", "${v}")
        }

        writer = new OutputStreamWriter(con.getOutputStream(), "UTF-8")
        writer.write(inputData)
        writer.flush()
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()))
        String inputLine
        StringBuffer responseStream = new StringBuffer()
        while ((inputLine = bufferedReader.readLine()) != null) {
            responseStream.append(inputLine);
        }
        String apiResponse = responseStream.toString()
        Map<String, String> response = [apiResponse: apiResponse, apiReponseCode: con.getResponseCode()]
        return response
    }

//    grails.googleStorage.clientId = "959996188065-i25m655tiac06jr1qqpkdgnbf994ot44.apps.googleusercontent.com"
//    grails.googleStorage.clientSecret = "ISOhLsyhxUQjGt88R5FrTgaJ"
//    grails.googleStorage.refreshToken = "1/LFh6eRTHjx_eI2wJXUDBr90PpUZ_hrkgEuiYy8E1c7AHQfgLeON3h_V4pS7VkdCb"
//    grails.googleStorage.apiKey = "AIzaSyDgWT0I6D5eEddNJpTCDvAJfDqRuI2_xTA"

//    private static final String REDIRECT_URI="http://localhost:8080/googleVisionTextReader/camScanner/saveImage"
//    private static final String CLIENT_ID="1088861476588-c8p6j60eqdoptqco9gm6vspai3vcgqss.apps.googleusercontent.com"
//    private static final String CLIENT_SECRET="OaEWh88yqME4-Rwx-KkdlvFe"
//    private static final String REFRESH_TOKEN="1/a_aK3uqeq1zLxeRhbNb7KK-S-WDYXD1C66TE7e9sp68"
//    private static final String API_KEY="AIzaSyBrnwKa2Ofh0SQ4NyeUluw3oM7ahmLsi3M"

    //redirected method to get refresh token
    def saveImage(){
        String googleAccessTokenURL="https://accounts.google.com/o/oauth2/token"
        HttpURLConnection con = null;
        OutputStreamWriter writer=null;
        String urlParameters =
                "client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8") +
                        "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, "UTF-8") +
                        "&grant_type=" + URLEncoder.encode("authorization_code", "UTF-8") +
                        "&code=" + URLEncoder.encode(accessCode, "UTF-8") +
                        "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8") ;
        String completeURL=googleAccessTokenURL
        URL obj = new URL(completeURL)
        con = (HttpURLConnection) obj.openConnection()
        con.setDoInput(true)
        con.setDoOutput(true)
        con.setRequestMethod("POST")
        con.setRequestProperty("Accept", "application/json")
        con.setRequestProperty("Content-Type","application/x-www-form-urlencoded")
        writer = new OutputStreamWriter(con.getOutputStream(),"UTF-8")
        writer.write(urlParameters)
        writer.flush()
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer responseStream =new StringBuffer();
        while((inputLine= bufferedReader.readLine())!=null)
        {
            responseStream.append(inputLine);
        }
        String apiResponse = responseStream.toString();
        JSONObject userJson = JSON.parse(apiResponse)
        println userJson
        if(con.getResponseCode()==200){
            println "accesss token"+userJson.access_token
//            callGoogleStorageAPI(userJson.access_token, fileNameWithPath)
        }
    }

}
