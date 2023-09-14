//package gitlet;

import java.io.File;
import java.io.Serializable;

//import static gitlet.Utils.*;

public class Blob implements Serializable {
    private String name;
    /*Make a folder for blobs to be accessed in the main GITLET_DIR **/
    public static final File BLOB_FOLDER = Utils.join(Repository.GITLET_DIR, ".blobs");
    private String contents;
    private String hash;
    public Blob(String name) {
        File f = Utils.join(Repository.CWD, name);
        byte[] b = Utils.readContents(f);
        this.name = name;
        this.hash = Utils.sha1(b);
        this.contents = Utils.readContentsAsString(f);
        this.toFile();
    }
    public String getHash() {
        return this.hash;
    }
    public String getName() {
        return this.name;
    }
    public String getContents() {
        return this.contents;
    }
    public void toFile() {
        File blobFile = Utils.join(BLOB_FOLDER, hash);
        Utils.writeObject(blobFile, this);
    }
    public static Blob fromFile(String hash) {
        File blobFile = Utils.join(BLOB_FOLDER, hash);
        return Utils.readObject(blobFile, Blob.class);
    }
}