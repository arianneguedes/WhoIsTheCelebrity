package com.example.whoisthecelebrity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    // ImageView to display the celebrity image
    ImageView celebrityImgView;
    // Image that will be displayed on the ImageView
    Bitmap celebrityImage;
    // Buttons containing celebrity name to be chosen
    Button btnOptionA, btnOptionB, btnOptionC, btnOptionD;
    // ArrayLists to store celebrity names and celebrity image urls
    ArrayList<String> celebrityNames, celebrityImageUrls;
    // Array to store the answers (celebrity names) for each question
    String [] answers;
    // Variables to store the selected celebrity, the button containing
    // the correct answer and the buttons for the incorrect answers
    Integer selectedCelebrity, correctAnswerButton, incorrectAnswerButton;
    // Variable to create questions with random celebrities and random
    // correct and incorrect answers
    Random r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        celebrityImgView = findViewById(R.id.imgViewCelebrity);
        btnOptionA = findViewById(R.id.btnOptionA);
        btnOptionB = findViewById(R.id.btnOptionB);
        btnOptionC = findViewById(R.id.btnOptionC);
        btnOptionD = findViewById(R.id.btnOptionD);
        celebrityNames = new ArrayList<>();
        celebrityImageUrls = new ArrayList<>();
        answers = new String[4];
        r = new Random();

        extractStrings();

        if (savedInstanceState != null) {
            selectedCelebrity = Integer.parseInt(savedInstanceState.getString("selectedCelebrity"));
            correctAnswerButton = Integer.parseInt(savedInstanceState.getString("correctAnswerButton"));
            answers[0] = savedInstanceState.getString("answer1");
            answers[1] = savedInstanceState.getString("answer2");
            answers[2] = savedInstanceState.getString("answer3");
            answers[3] = savedInstanceState.getString("answer4");
            showQuestion();
        } else {
            createQuestion();
        }
    }

    // Saving necessary information, so we don't lose it when we rotate the device
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("selectedCelebrity", selectedCelebrity.toString());
        outState.putString("correctAnswerButton", correctAnswerButton.toString());
        outState.putString("answer1", answers[0]);
        outState.putString("answer2", answers[1]);
        outState.putString("answer3", answers[2]);
        outState.putString("answer4", answers[3]);
    }

    // Creating the menu and populating it
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    // If the user selects the help icon (menu item), start the Help activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.help) {
            Intent intent = new Intent(MainActivity.this, Help.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    // Getting the website content as a String to use it in the application
    public class DownloadWebsite extends AsyncTask<String, Void, String> {
        String result;
        InputStream is;
        URL url;
        HttpURLConnection urlConnection;
        InputStreamReader reader;

        @Override
        protected String doInBackground(String... urls) {
            result = "";
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                while((inputLine = br.readLine()) != null) {
                    result += inputLine;
                }
                br.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
    }

    // Getting the celebrity image
    public class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            URL url;
            HttpURLConnection urlConnection;
            InputStream is;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                is = urlConnection.getInputStream();
                celebrityImage = BitmapFactory.decodeStream(is);
                return celebrityImage;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    // Loading the celebrity image into the screen (ImageView) for each question
    // based on its respective image url
    public void loadImage(String imgUrl, ImageView imgView)
    {
        DownloadImage downloadImgTask = new DownloadImage();
        Bitmap downloadedImage;
        try {
            downloadedImage = downloadImgTask.execute(imgUrl).get();
            imgView.setImageBitmap(downloadedImage);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Populating the ArrayLists containing celebrity names and image urls using the
    // the filtered content from the Fandango website
    public void extractStrings() {
        String celebritiesWebsite = "https://www.fandango.com/famous-actors-and-actresses";
        DownloadWebsite downloadTask = new DownloadWebsite();
        String htmlString;

        try {
            htmlString = downloadTask.execute(celebritiesWebsite).get();
            Pattern p = Pattern.compile("src=\"(.*?)\" alt=\"(.*?)\" width=");
            Matcher m = p.matcher((htmlString));

            while(m.find()) {
                if(m.group(2).length() >= 8 && m.group(2).length() <= 30) {
                    String celebrityName = m.group(2).toString().replace(",", "");
                    celebrityNames.add(celebrityName);
                    celebrityImageUrls.add(m.group(1));
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Creating and displaying a question by randomly selecting one celebrity from the
    // celebrityImageUrls ArrayList and randomly assigning the correct and incorrect
    // answers to the different buttons
    public void createQuestion() {

        selectedCelebrity = r.nextInt(celebrityImageUrls.size());
        try {
            String selectedCelebUrl = celebrityImageUrls.get(selectedCelebrity);
            loadImage(selectedCelebUrl, celebrityImgView);
            correctAnswerButton = r.nextInt(4);

            for(int i = 0; i < 4; i++) {
                if(i == correctAnswerButton) {
                    answers[i] = celebrityNames.get(selectedCelebrity);
                } else {
                    incorrectAnswerButton = r.nextInt(celebrityImageUrls.size());
                    while(incorrectAnswerButton == selectedCelebrity) {
                        incorrectAnswerButton = r.nextInt(celebrityImageUrls.size());
                    }
                    answers[i] = celebrityNames.get(incorrectAnswerButton);
                }

                btnOptionA.setText(answers[0]);
                btnOptionB.setText(answers[1]);
                btnOptionC.setText(answers[2]);
                btnOptionD.setText(answers[3]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Displaying a question that was already on the screen using saved information
    public void showQuestion() {
        String selectedCelebUrl = celebrityImageUrls.get(selectedCelebrity);
        loadImage(selectedCelebUrl, celebrityImgView);

        btnOptionA.setText(answers[0]);
        btnOptionB.setText(answers[1]);
        btnOptionC.setText(answers[2]);
        btnOptionD.setText(answers[3]);
    }

    // onClick event to check if the user selected the correct answer/button or not.
    // A Toast message will be displayed informing that, and a new question will be
    // created at the end
    public void selectedAnswer(View view) {
        if(view.getTag().toString().equals(Integer.toString(correctAnswerButton))) {
            Toast.makeText(this, "Correct answer! Well done!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Incorrect answer! The correct is " + celebrityNames.get(selectedCelebrity) + ".", Toast.LENGTH_SHORT).show();
        }
        createQuestion();
    }
}