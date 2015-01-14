package uk.co.icappsoc.memorygame;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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
     * A fragment containing our memory game.
     */
    public static class PlaceholderFragment extends Fragment {

        private boolean busy;
        private static final int NONE = -1;
        private Handler handler = new Handler();
        private Button[] buttons = new Button[4];
        // Art by Michael B. Myers Jr. at http://drbl.in/bhbA
        private int[] images = new int[]{R.drawable.card_c3po, R.drawable.card_r2d2};
        private int[] buttonToImageIndex = new int[4];
        private boolean[] buttonRevealed = new boolean[4];
        private int lastIndexClicked;

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

            busy = false;
            lastIndexClicked = NONE;
            resetButtons();

            return rootView;
        }

        /** Reset the image of each button to the default back. */
        private void resetButtons(){
            for(int i = 0; i < buttonRevealed.length; i++){
                buttonRevealed[i] = false;
            }

            for(Button b : buttons){
                b.setBackgroundResource(R.drawable.card_back);
            }

            randomizeButtonImages();
        }

        private void randomizeButtonImages(){
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
                if(buttonRevealed[i]){
                    buttons[i].setBackgroundResource(images[buttonToImageIndex[i]]);
                } else {
                    buttons[i].setBackgroundResource(R.drawable.card_back);
                }
            }
        }

        private void onButtonClicked(int indexClicked, Button b){
            if(busy) return;

            // Set the image of the button to its corresponding picture.
            b.setBackgroundResource(images[buttonToImageIndex[indexClicked]]);

            if(NONE == lastIndexClicked){
                // This is the first button clicked.
                lastIndexClicked = indexClicked;
            } else {
                // A button was previously clicked!
                if(lastIndexClicked == indexClicked){
                    // Same button clicked again. Do nothing..
                    return;
                } else {
                    // A unique second button was clicked!
                    if(match(lastIndexClicked, indexClicked)){
                        buttonRevealed[lastIndexClicked] = true;
                        buttonRevealed[indexClicked] = true;
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
        }

        /** @return true if buttons at given indices have an identical picture. */
        private boolean match(int firstButtonIndex, int secondButtonIndex){
            return buttonToImageIndex[firstButtonIndex] == buttonToImageIndex[secondButtonIndex];
        }

        /** Checks if the player has won the game. If so, resets the game.*/
        private void checkVictoryState(){
            for(boolean revealed : buttonRevealed){
                if(!revealed) return; // if any button has not been revealed, the game is on!
            }

            // Congratulate the player!
            Toast.makeText(getActivity(), "Congratulations! You win!", Toast.LENGTH_SHORT).show();

            // Reset the game after a short delay
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    resetButtons();
                }
            }, 1000);
        }
    }
}