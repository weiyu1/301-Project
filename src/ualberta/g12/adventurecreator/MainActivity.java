
package ualberta.g12.adventurecreator;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.List;

public class MainActivity extends Activity implements LView<StoryList>, OnItemClickListener {

    private List<Story> stories;
    private StoryList storyList;
    private static final boolean DEBUG_LOG = true;
    private static final String TAG = "MainActivity";
    private ListView listView;
    private StoryListArrayAdapter adapter;

    private static final String SHARED_PREF_IS_AUTHOR_FLAG = "isAuthor";
    private static boolean isAuthor = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get our storyList instance from the application
        storyList = AdventureCreatorApplication.getStoryList();
        // Load our local stories from the StoryList Model
        stories = storyList.getAllStories();

        if (DEBUG_LOG)
            Log.d(TAG, String.format("Number of stories is: %d", stories.size()));

        // Set up ListView Stuff
        adapter = new StoryListArrayAdapter(this, R.layout.listview_story_list, stories);
        listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        // TODO: Set up listeners on items

    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        isAuthor = settings.getBoolean(SHARED_PREF_IS_AUTHOR_FLAG, false);

    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(SHARED_PREF_IS_AUTHOR_FLAG, isAuthor);

        // Commit the edits
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Make sure check box is checked if it needs to be
        menu.findItem(R.id.menu_check_box_author).setChecked(isAuthor);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_story:
                // TODO: Launch the EditStoryActivity with the id of NEW_STORY
                startActivity(new Intent(this, CreateStoryActivity.class));
                return true;

            case R.id.menu_check_box_author:
                if (DEBUG_LOG)
                    Log.d(TAG,
                            String.format("Checkbox checked, current value: %s", item.isChecked()));

                boolean alreadyChecked = item.isChecked();
                // Set it to the opposite of what it previously was
                item.setChecked(!alreadyChecked);

                // Set the flag now
                isAuthor = item.isChecked();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void update(StoryList model) {
        // Reload our stories from StoryList Model
        stories = storyList.getAllStories();
        // Notify Our ListView that our array has changed
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
        // Open Edit/View story Activity with
        Intent i;
        if (isAuthor) {
            i = new Intent(this, StoryEditActivity.class);
            i.putExtra(StoryEditActivity.INTENT_STORY_ID, stories.get(pos).getId());
        } else {
            i = new Intent(this, StoryViewActivity.class);
            i.putExtra(StoryViewActivity.INTENT_STORY_ID, stories.get(pos).getId());
        }
        startActivity(i);
    }
}
