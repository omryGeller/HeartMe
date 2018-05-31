package geller.omry.heartme.Model;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BloodTestManger {

    private Map<String,String> bloodTestCategoryMap;
    private static BloodTestManger managerInstace;
    private final String BLOOD_TEST_URL="https://s3.amazonaws.com/s3.helloheart.home.assignment/bloodTestConfig.json";

    public BloodTestManger() {

        bloodTestCategoryMap=new HashMap<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                fillBloodTestCategoryMap();
            }
        }).start();

    }

    /**
     * This method is responsible for creating a single instance of BloodTestManger.
     * It is synchronized to prevent the creation of multiple instances in case it is approached by more then one thread.
     * @return the return value is the single instance created.
     */
    public static synchronized BloodTestManger getManagerInstance(){

        if(managerInstace == null){
            managerInstace=new BloodTestManger();
        }
        return managerInstace;
    }


    /**
     * This method evaluates the userCategoryInput parameter to determine if there is a category for it in the bloodTestCategoryMap(Dataset).
     * @param userCategoryInput the user's input to be evaluated for finding a matching(or more accurate) category.
     * @return if a matching category was found in the dataset th method will return that category,else it will return 'Unknown'.
     */
    public String getRelevantBloodTestCategory(String userCategoryInput){

        for(String category : bloodTestCategoryMap.keySet()){
            if(category.equalsIgnoreCase(userCategoryInput)){
                return category;
            }
        }

        userCategoryInput=userCategoryInput.toLowerCase().replaceAll(" ","-").trim();

        List<String> words=new ArrayList<>(Arrays.asList(userCategoryInput.split("[^A-Za-z0-9]")));

        List<String> relevantWords=new ArrayList<>(words);

        for(String wordToRemove : words) {
            boolean isRelevantWord=false;
            for(String category : bloodTestCategoryMap.keySet())
            {
                if(Arrays.asList(category.toLowerCase().split("[^A-Za-z0-9]")).contains(wordToRemove))
                    isRelevantWord=true;
            }
            if(!isRelevantWord)
                relevantWords.remove(wordToRemove);
        }

        if(relevantWords.size() == 0){
            return "Unknown";
        }

        for (String category : bloodTestCategoryMap.keySet()) {
            boolean isCorrectCategory=true;

            for(String word : relevantWords){

                if(!Arrays.asList(category.toLowerCase().split("[^A-Za-z0-9]")).contains(word)){
                    isCorrectCategory=false;
                    break;
                }
            }
            if(isCorrectCategory){
                return category;
            }
        }
        return "Unknown";
    }

    /**
     * This method gets the dataset from the aws bucket and fills the bloodTestCategoryMap
     * with the name and result properties from the objects it gets
     */
    private void fillBloodTestCategoryMap(){
        URL url = null;
        try {
            url = new URL(BLOOD_TEST_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (url != null) {
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream is = httpURLConnection.getInputStream();
                String json = IOUtils.toString(is, "UTF-8");
                JSONObject jObj = new JSONObject(json);
                JSONArray jsonArray=new JSONArray(jObj.getString("bloodTestConfig"));
                if(jsonArray != null){
                    if(jsonArray.length() > 0){
                        for(int i=0;i<jsonArray.length();i++){
                            String name=jsonArray.getJSONObject(i).getString("name");
                            String result=jsonArray.getJSONObject(i).getString("threshold");
                            if(name != null && !name.isEmpty() && result != null && !result.isEmpty())
                                bloodTestCategoryMap.put(name,result);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getBloodTestResult(String testName){
        return bloodTestCategoryMap.get(testName);
    }
}
