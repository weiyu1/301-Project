
package ualberta.g12.adventurecreator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.util.Log;

import java.io.File;
import java.util.LinkedList;
import java.util.List;


// Right now we are making Fragments here and also choice
// What i was thinking was maybe just make the general fragment here
// and have another screen to add choices then we could have list a listview type thing so can have a lot of choices
// and long clicking each one would let you edit/delete them 
// TODO discuss with the team
//
public class EditFragmentActivity extends Activity implements FView<Fragment> {

    private int storyId, position, type;
    private TextView fragmentTitleTextView;
    private ListView fragmentPartListView;
    private FragmentPartAdapter adapter;
    public static final int EDIT = 0;
    public static final int ADD = 1;
    private EditText titleText;
    private EditText idPageNumText;
    Uri imageFileUri;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    ImageButton imag;
    private static final String TAG = "EditFragmentActivity";
    private OfflineIOHelper offlineHelper;
    private String mode;
    private StoryList storyList;
    private Story story;
    private int storyPos, fragPos;
    private Fragment fragment;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_editor);

        //obtain the intent
        Intent editActIntent = getIntent();
        mode = (String) editActIntent.getSerializableExtra("Mode");
        storyList = (StoryList)editActIntent.getSerializableExtra("StoryList");
        story  = (Story)editActIntent.getSerializableExtra("Story");
        storyPos  = (Integer)editActIntent.getSerializableExtra("StoryPos");
        fragment = (Fragment)editActIntent.getSerializableExtra("Fragment");
        fragPos = (Integer)editActIntent.getSerializableExtra("FragmentPos");
        
        
        //get widget references
        fragmentPartListView = (ListView) findViewById(R.id.FragmentPartList);
        titleText = (EditText) findViewById(R.id.fragmentTitle);
        idPageNumText = (EditText) findViewById(R.id.idPageNum);
            
        // TODO: Set the fragmentController to our Fragment

        /* for testing, will delete later -Lindsay */
        //Drawable ill = Drawable.createFromPath("/mnt/sdcard/tmp/2013-11-04 22.04.41.jpg");
        FragmentController.addTextSegment(fragment, "part1");
        FragmentController.addTextSegment(fragment,"part2beforeill");
        FragmentController.addTextSegment(fragment,"part number 3 which is rather long because we woud like to test text wrapping");

        //FragmentController.addIllustration(fragment,ill,2);
        
        if (mode.equals("Edit") == true && fragment.getDisplayOrder().size()==0){
            FragmentController.addEmptyPart(fragment);
        }
        // TODO: Load our fragment into view
        

        //Loads title
        String title = fragment.getTitle();
        if (titleText != null){
            if (title != null)
                titleText.setText(title);
            else
                titleText.setText("Title Here");  //should this go here? -Lindsay
        }

        //Loads fragment parts (text, images, videos, sounds, etc)
        adapter = new FragmentPartAdapter(this, R.layout.activity_fragment_editor, fragment);
        fragmentPartListView.setAdapter(adapter);

        registerForContextMenu(fragmentPartListView);
    }

    @Override
    public void onBackPressed() {
        saveTitlePageId();
        saveFragment();
//        Intent i = new Intent(this, StoryEditActivity.class);
//        i.putExtra("Story",story);
//        i.putExtra(StoryEditActivity.INTENT_STORY_ID, story.getId());
        super.onBackPressed();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fragment_editor, menu);
        return true;
    }

    @Override
    public void update(Fragment model) {
        // TODO reload all fields based on new info from model

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fragment_part_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        position = (int)info.id;
        
        CharSequence itemTitle = item.getTitle();
        if (itemTitle.equals("Insert Text")){
            FragmentController.addTextSegment(fragment, "New text", position);

        } else if (itemTitle.equals("Insert Illustration")){
            AddImage();

        } else if (itemTitle.equals("Edit")){
            if (fragment.getDisplayOrder().get(position).equals("t") || fragment.getDisplayOrder().get(position).equals("e")){
                RelativeLayout curLayout = new RelativeLayout(this);
                
                LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final PopupWindow editTextWindow = new PopupWindow(inflater.inflate(R.layout.edit_text_seg_popup, null, true), 400, 400, true);
        
                final EditText editTextSegView = (EditText) editTextWindow.getContentView().findViewById(R.id.editTextSeg);
                editTextSegView.setText(fragment.getTextSegments().get(position));
                
                Button editTextSave = (Button) editTextWindow.getContentView().findViewById(R.id.editTextSave);
                Button editTextCancel = (Button) editTextWindow.getContentView().findViewById(R.id.editTextCancel);
                
                editTextSave.setOnClickListener(new EditTextSegOnClickListener(position) {
                    @Override
                    public void onClick(View v) {
                        String newText = editTextSegView.getText().toString();
                        FragmentController.deleteFragmentPart(fragment,this.position);
                        FragmentController.addTextSegment(fragment, newText, this.position);
                        editTextWindow.dismiss();
                    }
                });
                
                editTextCancel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editTextWindow.dismiss();
                    }
                });
                
                
                editTextWindow.showAtLocation(curLayout, Gravity.CENTER, 0, 0); 
                editTextWindow.update(0,0,fragmentPartListView.getWidth(),400);
                
            } else if (fragment.getDisplayOrder().get(position).equals("i")){
                Drawable illustration = getDrawableGalleryOrCamera();
                FragmentController.addIllustration(fragment, illustration, position);
            }
            
        } else if(itemTitle.equals("Add Choice")){ 
            //add choice Logic
            //            EditText choice1ET = (EditText) findViewById(R.id.choiceId1);
            //            String choice1 = choice1ET.getText().toString();
            //            aNewChoice.setChoiceText(choice1);
        	
        	//TODO AS of right now it only works for edit mode will have to add extra logic for ADD mode as fragment is not created yet
        	/*
        	// will have to change way of calling 
        	// TODO change to newer way of calling 
        	if (type == EDIT){
        		Fragment selectedFrag = fragmentList.get(pos);
        		Intent intent = new Intent(this,EditChoiceActivity.class);
        		intent.putExtra("OurFragmentId", fragmentId);
        		intent.putExtra("OurStoryId", storyId);
        		startActivity(intent);
        		Log.d("HI","HELLO");
        	}
        	*/

        }else if (itemTitle.equals("Delete")){
            FragmentController.deleteFragmentPart(fragment, position);
        }

        System.out.println("n2");
        //Make sure the fragment isn't completely empty
        if(fragment.getDisplayOrder().size()==0)
            FragmentController.addEmptyPart(fragment);

        //reset listview to display any changes
        fragmentPartListView.invalidateViews();
        return true;
    }
    
    /*
     * Needs to be finished!!!  (Please) -Lindsay
     */
    private Drawable getDrawableGalleryOrCamera(){
     // From: http://stackoverflow.com/q/16391124/1684866
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
        galleryIntent.setType("image/*");
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent);
        chooser.putExtra(Intent.EXTRA_TITLE, "Select Illustration From");

        Intent[] intentArray = {
                cameraIntent
        };
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
        startActivityForResult(chooser, 0);
        return null;
    }
    
    /*
     * Must call before leaving activity!!!
     */
    private void saveTitlePageId() {
        //AS OF RIGHT NOW ONLY LOADS ONE CHOICE TITLE AND TITLE PAGE NUMBER AND ALSO FRAGMENT DESCRIPTION

        EditText titleET = (EditText) findViewById(R.id.fragmentTitle);
        EditText idPageNumET = (EditText) findViewById(R.id.idPageNum);
        
        String title = titleET.getText().toString();
        String idPageNum = idPageNumET.getText().toString();
        int idPage = -9;
        try{
            idPage = Integer.parseInt(idPageNum);
        }catch(NumberFormatException e){
            Log.d("Msg","There was a number format exception!");
        }

        fragment.setTitle(title);
        fragment.setId(idPage);
    }   
    
    public void AddImage() {
        
        // From: http://stackoverflow.com/q/16391124/1684866
     //*****Gallery Intent to save image      
     //NOT FINISHED! only opens up gallery***
      Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT,null);
      galleryIntent.setType("image/*");
      galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
     //***end Gallery intent**** 
     
   
      //*****Camera intent to save image *****
      Intent CameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         
         String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp";
         File folderF = new File(folder);
         if (!folderF.exists()) {
             folderF.mkdir();
     }     
     String imageFilePath = folder + "/" + String.valueOf(System.currentTimeMillis()) + "jpg";
     File imageFile = new File(imageFilePath);
     imageFileUri = Uri.fromFile(imageFile);
     CameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
     //*********/End Camera intent******
     
     //Intent for chooser for image resource 
      Intent chooser = new Intent(Intent.ACTION_CHOOSER);   
      chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent);      
      chooser.putExtra(Intent.EXTRA_TITLE, "Select Illustration From");
      
      Intent[] intentArray =  {CameraIntent}; 
      chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
      startActivityForResult(chooser,CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);  
 }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                System.out.println("RES OK");
                Drawable illustration = Drawable.createFromPath(imageFileUri.getPath());
                System.out.println("DRAWABLECREATE");
                FragmentController.addIllustration(fragment, illustration, position);
                System.out.println("ADDILL");
                
                ImageButton imag = (ImageButton) findViewById(R.id.imagbut);
                //button.setScaleType(ScaleType.CENTER_INSIDE);
                imag.setImageDrawable(Drawable.createFromPath(imageFileUri.getPath()));
                System.out.println("SET imagBUT");
            }
        }
    }
    
    private void saveFragment(){
        storyList.getAllStories().get(storyPos).getFragments().set(fragPos, fragment);
        offlineHelper = new OfflineIOHelper(EditFragmentActivity.this);
        offlineHelper.saveOfflineStories(storyList);
    }
}
