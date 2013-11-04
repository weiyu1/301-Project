
package ualberta.g12.adventurecreator;

public class StoryController implements SController {

    
    /**
     * @param newTitle the new title of story s
     * @param s the story to change the title of*/
    public void setTitle(String newTitle, Story s){
        s.setStoryTitle(newTitle);
    }
    
    /**
     * @param newAuthor the new author of story s
     * @param s the story to change the author of*/
    public void setAuthor(String newAuthor, Story s){
        s.setAuthor(newAuthor);
    }
    
    @Override
    public void addFragment(Story s, Fragment f) {
        s.addFragment(f);
    }

    @Override
    public boolean deleteFragment(Story s, Fragment f) {
        return s.removeFragment(f);
    }

}
