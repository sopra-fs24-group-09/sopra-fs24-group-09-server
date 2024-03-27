package ch.uzh.ifi.hase.soprafs24.entity;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "player") 
public class Player extends User {

    private String audioData; 
    private String word; 

    public Player() {
        super();
    }

    public String getAudioData() {
        return audioData;
    }

    public void setAudioData(String audioData) {
        this.audioData = audioData;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
