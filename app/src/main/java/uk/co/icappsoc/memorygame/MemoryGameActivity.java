package uk.co.icappsoc.memorygame;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Random;


public class MemoryGameActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private static final int BUSY = -2;
        private static final int NONE = -1;
        private Handler handler = new Handler();
        private Button[] buttons = new Button[4];
        private int[] images = new int[]{R.drawable.card_c3po, R.drawable.card_r2d2};
        private int[] buttonToImageIndex = new int[4];
        private int buttonClicked;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_memory_game, container, false);

            // Get a reference to each button, store in our buttons array
            buttons[0] = (Button) rootView.findViewById(R.id.button);
            buttons[1] = (Button) rootView.findViewById(R.id.button2);
            buttons[2] = (Button) rootView.findViewById(R.id.button3);
            buttons[3] = (Button) rootView.findViewById(R.id.button4);

            // Each button behaves the same; on click, call showPicture with a reference to the clicked button
            for(int i = 0; i < buttons.length; i++){
                final int index = i;
                buttons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onButtonClicked(index, buttons[index]);
                    }
                });
            }

            // Randomize button images
            Random random = new Random();
            int[] count = new int[images.length];
            for(int i = 0; i < buttons.length; i++){
                // Ensure max 2 of any given image are chosen
                // Dirty solution, may not scale nicely depending on number of buttons / images
                int candidateImageIndex = random.nextInt(images.length);
                while(count[candidateImageIndex] >= 2){
                    candidateImageIndex = random.nextInt(images.length);
                }

                count[candidateImageIndex]++;
                buttonToImageIndex[i] = candidateImageIndex;
            }

            resetButtons();

            return rootView;
        }

        private void onButtonClicked(int index, Button b){

            if(BUSY == buttonClicked) return;
            if(NONE == buttonClicked){
                // Set the image of the button to be a chosen picture
                b.setBackgroundResource(images[buttonToImageIndex[index]]);

                buttonClicked = index;
            } else {
                // another button was previously clicked!
                if(buttonClicked == index){
                    return;
                } else {
                    buttonClicked = BUSY;

                    b.setBackgroundResource(images[buttonToImageIndex[index]]);

                    // Call the hidePictures method after a delay
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            resetButtons();
                        }
                    }, 700);
                }
            }
        }

        private void resetButtons(){
            // Reset the image of each button to the default back
            for(Button b : buttons){
                b.setBackgroundResource(R.drawable.card_back);
            }

            buttonClicked = NONE;
        }
    }
}