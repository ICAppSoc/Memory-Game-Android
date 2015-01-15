package uk.co.icappsoc.memorygame;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import java.util.Random;


public class MemoryGameActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MemoryGameFragment())
                    .commit();
        }
    }

    /**
     * A fragment containing our memory game.
     */
    public static class MemoryGameFragment extends Fragment {
        private static final int NUM_BUTTONS = 8;
        private static final String LOG_TAG = MemoryGameFragment.class.getSimpleName();

        private boolean busy;
        private Handler handler = new Handler();

        private Button[] buttons = new Button[NUM_BUTTONS];
        // Art by Michael B. Myers Jr. at http://drbl.in/bhbA
        private int[] images = new int[]{
                R.drawable.card_c3po,
                R.drawable.card_r2d2,
                R.drawable.card_han,
                R.drawable.card_chewie};
        private int[] buttonToImageIndex = new int[NUM_BUTTONS];

        private Chronometer timer;
        private boolean firstPairRevealed;
        private boolean[] buttonPermanentlyRevealed = new boolean[NUM_BUTTONS];
        private int lastIndexClicked;
        private static final int NONE = -1;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_memory_game, container, false);

            // Get a reference to each button, store in our buttons array
            for(int i = 0; i < NUM_BUTTONS; i++){
                buttons[i] = (Button) rootView.findViewById(getButtonId("button" + i));
            }

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

            timer = (Chronometer) rootView.findViewById(R.id.timer);

            busy = false;
            lastIndexClicked = NONE;
            resetButtons();

            return rootView;
        }

        /** A handy (but not recommended) way to retrieve a resource id by name in code. */
        private int getButtonId(final String buttonIdName){
            final int buttonId = getResources().getIdentifier(buttonIdName, "id", getActivity().getPackageName());
            if(0 == buttonId){
                // uh oh. better not even try..
                Log.e(LOG_TAG, "Could not find button with id " + buttonIdName);
                throw new RuntimeException("Cannot find necessary button..");
            }
            return buttonId;
        }

        /** Reset the image of each button to the default back. */
        private void resetButtons(){
            for(int i = 0; i < buttonPermanentlyRevealed.length; i++){
                buttonPermanentlyRevealed[i] = false;
            }
            firstPairRevealed = false;

            for(Button b : buttons){
                b.setBackgroundResource(R.drawable.card_back);
            }

            randomizeButtonImages();
        }

        private void randomizeButtonImages(){
            if(images.length < NUM_BUTTONS / 2){
                Log.e(LOG_TAG, images.length + " images are not enough for " + NUM_BUTTONS + " buttons.");
                throw new RuntimeException("Not enough images!");
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
        }

        private void hideUnrevealedButtons(){
            for(int i = 0; i < buttons.length; i++){
                if(buttonPermanentlyRevealed[i]){
                    buttons[i].setBackgroundResource(images[buttonToImageIndex[i]]);
                } else {
                    buttons[i].setBackgroundResource(R.drawable.card_back);
                }
            }
        }

        private void onButtonClicked(int indexClicked, Button b){
            if(busy) return;
            if(buttonPermanentlyRevealed[indexClicked]) return; // Nothing to do here..

            // Set the image of the button to its corresponding picture.
            b.setBackgroundResource(images[buttonToImageIndex[indexClicked]]);

            if(NONE == lastIndexClicked){
                // This is the first button clicked.
                lastIndexClicked = indexClicked;

                if(!firstPairRevealed){
                    hideTimer();
                }
            } else if(lastIndexClicked != indexClicked){
                // A unique second button was clicked!
                if(!firstPairRevealed){
                    startTimer();
                    firstPairRevealed = true;
                }

                if(match(lastIndexClicked, indexClicked)){
                    buttonPermanentlyRevealed[lastIndexClicked] = true;
                    buttonPermanentlyRevealed[indexClicked] = true;
                    lastIndexClicked = NONE;

                    checkVictoryState();
                } else {
                    lastIndexClicked = NONE;

                    // Do not let the user perform clicks while we are waiting
                    busy = true;

                    // Call the hideUnrevealedButtons method after a delay
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hideUnrevealedButtons();
                            busy = false;
                        }
                    }, 700);
                }
            }
        }

        /** @return true if buttons at given indices have an identical picture. */
        private boolean match(int firstButtonIndex, int secondButtonIndex){
            return buttonToImageIndex[firstButtonIndex] == buttonToImageIndex[secondButtonIndex];
        }

        /** Checks if the player has won the game. If so, resets the game.*/
        private void checkVictoryState(){
            for(boolean revealed : buttonPermanentlyRevealed){
                if(!revealed) return; // if any button has not been revealed, the game is on!
            }

            stopTimer();

            // Congratulate the player!
            Toast.makeText(getActivity(), "Congratulations! You win!", Toast.LENGTH_SHORT).show();

            // Reset the game after a short delay
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    resetButtons();
                }
            }, 1400);
        }

        private void startTimer(){
            timer.setBase(SystemClock.elapsedRealtime());
            timer.start();
            timer.setVisibility(View.VISIBLE);
        }

        private void stopTimer(){
            timer.stop();
        }

        private void hideTimer(){
            timer.setVisibility(View.GONE);
        }
    }
}