package uk.co.icappsoc.memorygame;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


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

        private Handler handler = new Handler();
        private Button[] buttons = new Button[4];

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
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPicture((Button) v);
                }
            };

            for(Button b : buttons){
                b.setOnClickListener(onClickListener);
            }

            return rootView;
        }

        private void showPicture(Button b){
            // Set the image of the button to be a chosen picture
            b.setBackgroundResource(R.drawable.card_c3po);

            // Call the hidePictures method after a delay
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hidePictures();
                }
            }, 4000); // Run after 4000ms, or 4s
        }

        private void hidePictures(){
            // Reset the image of each button to the default back
            for(Button b : buttons){
                b.setBackgroundResource(R.drawable.card_back);
            }
        }
    }
}
