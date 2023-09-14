//package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.Map;
import java.util.HashMap;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /** Folder that commits live in. */
    static final File COMMIT_FOLDER = Utils.join(Repository.GITLET_DIR, ".commits");
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    /** Parent of this commit */
    private String parent;
    /** The timestamp of this commit. */
    private Date timestamp;

    private HashMap<String, String> fileContents;

    private String hash;

    /* TODO: fill in the rest of this class. */
    public Commit(String message, String parent) {
        this.message = message;
        this.parent = parent;
        this.timestamp = new Date(); //maybe an if statement to set up for if we are at the parent commit (parent=null)
        this.fileContents = new HashMap<>();
        this.hash = Utils.sha1(timestamp.toString() + Math.random());
        this.toFile();
    }
    /* Adds info from staging area into the commit object. **/
    public void populate(Repository repo) {
        for (Map.Entry<String, String> entry : repo.getStagingArea().entrySet()) {
            fileContents.put(entry.getKey(), entry.getValue());
        }
        if (parent != null) {
            for (Map.Entry<String, String> entry : Commit.fromFile(parent).getFileContents().entrySet()) {
                if (!fileContents.containsKey(entry.getKey()) && !repo.getDeletionArea().containsKey(entry.getKey())) {
                    fileContents.put(entry.getKey(), entry.getValue());
                }
            }
        }
        this.toFile();
    }

    public HashMap<String, String> getFileContents() {
        return fileContents;
    }

    public String getHash() {
        return hash;
    }

    public String getParent() {
        return parent;
    }
    /* Reads in and deserializes a commit from a file with name hash in COMMIT_FOLDER.**/

    public void toFile() {
        File commitFile = Utils.join(COMMIT_FOLDER, hash);
        Utils.writeObject(commitFile, this);
    }

    public static Commit fromFile(String hash) {
        File commitFile = Utils.join(COMMIT_FOLDER, hash);
        return Utils.readObject(commitFile, Commit.class);
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}