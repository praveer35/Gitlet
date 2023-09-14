//package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Arrays;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD_Testing = new File(System.getProperty("user.dir"));
    public static final File CWD = Utils.join(CWD_Testing, "test");
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = Utils.join(CWD_Testing, ".gitlet");
    
    public static final File COMMIT_DIR = Utils.join(GITLET_DIR, ".commits");

    private Commit latestCommit;

    private String currBranch;

    private HashMap<String, String> stagingArea;

    private HashMap<String, String> deletionArea;

    private HashSet<String> commitSet;

    private HashMap<String, String> branchMap;

    public Repository() {
        GITLET_DIR.mkdir();
        Commit.COMMIT_FOLDER.mkdir();
        Blob.BLOB_FOLDER.mkdir();
        this.latestCommit = new Commit("initial commit", null);
        this.branchMap = new HashMap<>();
        this.branchMap.put("main", this.latestCommit.getHash());
        this.currBranch = "main";
        //this.branchMap = new TreeMap<>();
        //this.branchMap.put("main", new Branch(this.latestCommit));
        this.stagingArea = new HashMap<>();
        this.deletionArea = new HashMap<>();
        this.commitSet = new HashSet<>();
        this.commitSet.add(this.latestCommit.getHash());
    }

    /* TODO: fill in the rest of this class. */

    /**
     * Takes in a blob of new information, puts it in the staging area TreeMap. parentcom is the current head commit
     */
    public void add(String filename) {
        if (!Utils.join(CWD, filename).exists()) {
            System.out.println("File does not exist.");
            return;
        }
        if (deletionArea.containsKey(filename)) {
            deletionArea.remove(filename);
        }
        Blob updates = new Blob(filename);
        if (!latestCommit.getFileContents().containsKey(updates.getName())
            || !latestCommit.getFileContents().get(updates.getName()).equals(updates.getHash())) {
            if (stagingArea.containsKey(updates.getName())) {
                stagingArea.remove(updates.getName());
            }
            stagingArea.put(updates.getName(), updates.getHash());
        }
    }

    public void commit(String message) {
        if (message.isBlank()) {
            System.out.println("Please enter a commit message.");
            return;
        }
        if (stagingArea.isEmpty() && deletionArea.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        Commit newCommit = new Commit(message, getLatestCommit().getHash());
        newCommit.populate(this);
        setLatestCommit(newCommit);
        updateBranch(newCommit);
        addToCommitSet(newCommit.getHash());
        clearStagingArea();
        clearDeletionArea();
    }

    /**overwrite the specific file to the state of the latestCommit*/
    public void restore(String filename) { //note that restores do not stage; this
        if (!latestCommit.getFileContents().containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            return;
        } else {
            Blob snapshot = Blob.fromFile(latestCommit.getFileContents().get(filename));
            Utils.writeContents(Utils.join(CWD, filename), snapshot.getContents());
        }
    }
    /**overwrite the current files to this specific commit */
    public void restore(String commitID, String filename) {
        if (!commitSet.contains(commitID)) {
            boolean shorterCommitIDFound = false;
            for (String entry : commitSet) {
                if (entry.contains(commitID)) {
                    shorterCommitIDFound = true;
                    commitID = entry;
                }
            }
            if (!shorterCommitIDFound) {
                System.out.println("No commit with that id exists.");
                return;
            }
        }
        Commit restoringCommit = Commit.fromFile(commitID);
        if (!restoringCommit.getFileContents().containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
        } else {
            Blob snapshot = Blob.fromFile(restoringCommit.getFileContents().get(filename));
            Utils.writeContents(Utils.join(CWD, filename), snapshot.getContents());
        }
    }

    public void log() {
        Commit p = latestCommit;
        while (p != null) {
            System.out.println("===");
            System.out.println("commit " + p.getHash());
            String[] dt = p.getTimestamp().toString().split(" ");
            System.out.println("Date: " + dt[0] + " " + dt[1] + " " + dt[2] + " " + dt[3] + " " + dt[5] + " -0700");
            System.out.println(p.getMessage());
            System.out.println();
            if (p.getParent() == null) {
                break;
            }
            p = Commit.fromFile(p.getParent()); // this is only a string - must find the file and then read it
        }
    }

    public void remove(String filename) {
        boolean removed = false;
        if (stagingArea.containsKey(filename)) {
            stagingArea.remove(filename);
            removed = true;
        }
        if (latestCommit != null && latestCommit.getFileContents().containsKey(filename)) {
            Utils.restrictedDelete(Utils.join(CWD, filename));
            deletionArea.put(filename, latestCommit.getFileContents().get(filename));
            removed = true;
        }
        if (!removed) {
            System.out.println("No reason to remove the file.");
        }
    }

    public void globalLog() {
        List<String> commitList = Utils.plainFilenamesIn(COMMIT_DIR);
        for (int i = 0; i < commitList.size(); i++) {
            Commit p = Commit.fromFile(commitList.get(i));
            System.out.println("===");
            System.out.println("commit " + p.getHash());
            String[] dt = p.getTimestamp().toString().split(" ");
            System.out.println("Date: " + dt[0] + " " + dt[1] + " " + dt[2] + " " + dt[3] + " " + dt[5] + " -0700");
            System.out.println(p.getMessage());
            System.out.println();
        }
    }

    public void find(String keywords) {
        List<String> commitList = Utils.plainFilenamesIn(COMMIT_DIR);
        boolean found = false;
        for (int i = 0; i < commitList.size(); i++) {
            Commit p = Commit.fromFile(commitList.get(i));
            if (p.getMessage().contains(keywords)) {
                System.out.println(p.getHash());
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public void branch(String branchName) {
        if (!branchMap.containsKey(branchName)) {
            branchMap.put(branchName, latestCommit.getHash());
        } else {
            System.out.println("A branch with that name already exists.");
        }
    }

    public void switchBranch(String branchName) {
        if (!branchMap.containsKey(branchName)) {
            System.out.println("No such branch exists.");
        } else if (branchName.equals(currBranch)) {
            System.out.println("No need to switch to the current branch.");
        } else {
            // check for untracked files
            List<String> CWDFiles = Utils.plainFilenamesIn(CWD);
            Commit potential = Commit.fromFile(branchMap.get(branchName));
            for (int i = 0; i < CWDFiles.size(); i++) {
                if (!latestCommit.getFileContents().containsKey(CWDFiles.get(i))) {
                    if (potential.getFileContents().containsKey(CWDFiles.get(i))) {
                        Blob otherBranchBlob = Blob.fromFile(potential.getFileContents().get(CWDFiles.get(i)));
                        Blob thisBranchBlob = new Blob(CWDFiles.get(i));
                        if (!otherBranchBlob.getHash().equals(thisBranchBlob.getHash())) {
                            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                            return;
                        }
                    } else {
                        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                        return;
                    }
                }
            }
            for (int i = 0; i < CWDFiles.size(); i++) {
                if (potential.getFileContents().containsKey(CWDFiles.get(i))) {
                    Blob otherBranchBlob = Blob.fromFile(potential.getFileContents().get(CWDFiles.get(i)));
                    Utils.writeContents(Utils.join(CWD, CWDFiles.get(i)), otherBranchBlob.getContents());
                } else {
                    Utils.restrictedDelete(Utils.join(CWD, CWDFiles.get(i)));
                }
            }
            for (Map.Entry<String, String> entry : potential.getFileContents().entrySet()) {
                Utils.writeContents(Utils.join(CWD, entry.getKey()), Blob.fromFile(entry.getValue()).getContents());
            }
            currBranch = branchName;
            latestCommit = potential;
        }
    }

    public void removeBranch(String branchName) {
        if (branchName.equals(currBranch)) {
            System.out.println("Cannot remove the current branch.");
        } else if (!branchMap.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
        } else {
            branchMap.remove(branchName);
        }
    }

    public void status() {
        List<String> fileList = Utils.plainFilenamesIn(CWD);
        Collections.sort(fileList);
        System.out.println("=== Branches ==="); //gets all the branch names
        System.out.println("*" + currBranch);
        String currBranchCommitID = branchMap.get(currBranch);
        branchMap.remove(currBranch);
        Object[] branchList = branchMap.keySet().toArray();
        branchMap.put(currBranch, currBranchCommitID);
        Arrays.sort(branchList);
        for (Object branchName : branchList) {
            System.out.println((String) branchName);
        }
        System.out.println();
        System.out.println("=== Staged Files ==="); //in staging area and in cwd and matching contents
        for (int i = 0; i < fileList.size(); i++) { //for each file in filelist (cwd)
            if (stagingArea.containsKey(fileList.get(i))) { //if SA has filelist name
                File CWDfile = Utils.join(CWD, fileList.get(i));
                String CWDfilecontents = Utils.readContentsAsString(CWDfile);
                String SA_Blobhash = stagingArea.get(fileList.get(i)); //get the blob hash using the key (which is the same filename in both CWD and SA)
                Blob SAblob = Blob.fromFile(SA_Blobhash); //get the blob object
                String SA_blobContents = SAblob.getContents(); //get the contents of the blob
                if (CWDfilecontents.equals(SA_blobContents)) {//compare the contents
                    System.out.println(fileList.get(i));
                }
            }
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        Object[] deletedList = deletionArea.keySet().toArray();
        for (Object dfileName : deletedList) {
            System.out.println((String) dfileName);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ==="); //in stagingArea but not commited
        System.out.println();
        System.out.println("=== Untracked Files ==="); //not in stagingarea
        System.out.println();
    }

    public void reset(String commitID) { 
        List<String> CWDfileList = Utils.plainFilenamesIn(CWD);
        HashMap<String, String> LC_commitfiles = latestCommit.getFileContents();
        for (int i = 0; i < CWDfileList.size(); i++) {//go through CWD files
            String cwd_file = CWDfileList.get(i);
            if (!LC_commitfiles.containsKey(cwd_file) && !stagingArea.containsKey(cwd_file)) {//not in stageA, not in commits, IS IN CWD
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }
        if (!commitSet.contains(commitID)) {
            boolean shorterCommitIDFound = false;
            for (String entry : commitSet) {
                if (entry.contains(commitID)) {
                    shorterCommitIDFound = true;
                    commitID = entry;
                }
            }
            if (!shorterCommitIDFound) {
                System.out.println("No commit with that id exists.");
                return;
            }
        }
        Commit restoringCommit = Commit.fromFile(commitID); //get the commit
        HashMap<String, String> commitfiles = restoringCommit.getFileContents();
        for (String fileName : commitfiles.keySet()) { //for this commit restore every file on it
            restore(commitID, fileName);
        }
        for (int i = 0; i < CWDfileList.size(); i++) {//if its in the cwd but not in this commit (deletion))
            if (!commitfiles.containsKey(CWDfileList.get(i))) { //then remove it from cwd
                File tobeRemoved = Utils.join(CWD, CWDfileList.get(i)); //find that file in CWD
                Utils.restrictedDelete(tobeRemoved);
            }
        }
        latestCommit = restoringCommit;
        branchMap.replace(currBranch, latestCommit.getHash());
        clearStagingArea();
        clearDeletionArea();
    }

    public Commit findSplitPoint(String branchName) {
        if (!branchMap.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return null;
        }
        if (branchName.equals(currBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            return null;
        }
        List<String> currCWDfileList = Utils.plainFilenamesIn(CWD);
        HashMap<String, String> LC_commitfiles = latestCommit.getFileContents();
        for (int i = 0; i < currCWDfileList.size(); i++) {//go through CWD files
            String cwd_file = currCWDfileList.get(i);
            if (!LC_commitfiles.containsKey(cwd_file) && !stagingArea.containsKey(cwd_file)) {//not in stageA, not in commits, IS IN CWD
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return null;
            }
        }
        Commit otherCommit = Commit.fromFile(branchMap.get(branchName));
        Commit thisCommit = latestCommit;
        Commit splitPoint = null;
        HashSet<String> commitsTraversed = new HashSet<>();
        while (splitPoint == null) {
            if (thisCommit != null) {
                if (!commitsTraversed.contains(thisCommit.getHash())) {
                    commitsTraversed.add(thisCommit.getHash());
                } else {
                    splitPoint = thisCommit;
                    break;
                }
                thisCommit = (thisCommit.getParent() == null) ? null : Commit.fromFile(thisCommit.getParent());
            }
            if (otherCommit != null) {
                if (!commitsTraversed.contains(otherCommit.getHash())) {
                    commitsTraversed.add(otherCommit.getHash());
                } else {
                    splitPoint = otherCommit;
                    break;
                }
                otherCommit = (otherCommit.getParent() == null) ? null : Commit.fromFile(otherCommit.getParent());
            }
            if (thisCommit == null && otherCommit == null) {
                System.out.println("Error traversing back in merge.");
                System.exit(0);
            }
        }
        if (splitPoint.getHash().equals(branchName)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return null;
        }
        Commit otherLatestCommit = Commit.fromFile(branchMap.get(branchName));
        if (splitPoint.getHash().equals(currBranch)) {
            reset(otherLatestCommit.getHash());
            System.out.println("Current branch fast-forwarded.");
            return null;
        }
        return splitPoint;
    }

    public void writeMerge(Blob thisVersion, Blob otherVersion, String filename) {
        String toReplace = "<<<<<<< HEAD\n";
        if (thisVersion != null) {
            toReplace += thisVersion.getContents();
        }
        toReplace += "=======\n";
        if (otherVersion != null) {
            toReplace += otherVersion.getContents();
        }
        toReplace += ">>>>>>>\n";
        Utils.writeContents(Utils.join(CWD, filename, toReplace));
        add(filename);
        System.out.println("Encountered a merge conflict.");
    }

    public void merge(String branchName) {
        if (findSplitPoint(branchName) == null) {
            return;
        }
        Commit splitPoint = findSplitPoint(branchName);
        Commit otherLatestCommit = Commit.fromFile(branchMap.get(branchName));
        for (Map.Entry<String, String> entry : otherLatestCommit.getFileContents().entrySet()) {
            boolean isInSplit = splitPoint.getFileContents().containsKey(entry.getKey());
            boolean isInThis = latestCommit.getFileContents().containsKey(entry.getKey());
            Blob otherVersion = Blob.fromFile(entry.getValue());
            Blob thisVersion = null;
            boolean mergeConflict = false;
            if (isInThis) {
                // in both given and main
                thisVersion = Blob.fromFile(latestCommit.getFileContents().get(entry.getKey()));
                if (isInSplit) {
                    Blob splitVersion = Blob.fromFile(splitPoint.getFileContents().get(entry.getKey()));
                    if (!otherVersion.getHash().equals(splitVersion.getHash())) {
                        if (thisVersion.getHash().equals(splitVersion.getHash())) {
                            // if unmodified in this, overwrite this with other and stage (#1)
                            Utils.writeContents(Utils.join(CWD, entry.getKey()), otherVersion.getContents());
                            add(entry.getKey());
                        } else {
                            // else, modified in both -- merge conflict (#8)
                            writeMerge(thisVersion, otherVersion, entry.getKey());
                        }
                    }
                } else {
                    if (!otherVersion.getHash().equals(thisVersion.getHash())) {
                        // if no split point and versions are not equal -- merge conflict (#8)
                        writeMerge(thisVersion, otherVersion, entry.getKey());
                    }
                }
            } else if (isInSplit) {
                // if not in this but in split:
                // if unmodified in given, do nothing (#7). else if modified, merge conflict (#8)
                Blob splitVersion = Blob.fromFile(splitPoint.getFileContents().get(entry.getKey()));
                if (!otherVersion.getHash().equals(splitVersion.getHash())) {
                    writeMerge(thisVersion, otherVersion, entry.getKey());
                }
            } else {
                // if just in given, check out and stage (#5)
                Utils.writeContents(Utils.join(CWD, entry.getKey()), otherVersion.getContents());
                add(entry.getKey());
            }
        }
        for (Map.Entry<String, String> entry : splitPoint.getFileContents().entrySet()) {
            boolean isInOther = otherLatestCommit.getFileContents().containsKey(entry.getKey());
            boolean isInThis = latestCommit.getFileContents().containsKey(entry.getKey());
            if (!isInOther && isInThis) {
                // if unmodified in this, remove and untrack (#6). else, merge conflict (#8)
                Blob splitVersion = Blob.fromFile(entry.getValue());
                Blob thisVersion = Blob.fromFile(latestCommit.getFileContents().get(entry.getKey()));
                if (thisVersion.getHash().equals(splitVersion.getHash())) {
                    stagingArea.remove(entry.getKey());
                    Utils.restrictedDelete(entry.getKey());
                } else {
                    writeMerge(thisVersion, null, entry.getKey());
                }
            }
        }
        commit("Merged " + branchName + " into " + currBranch + ".");
    }

    public Commit getLatestCommit() {
        return latestCommit;
    }

    public void setLatestCommit(Commit c) {
        latestCommit = c;
    }

    public void updateBranch(Commit c) {
        branchMap.replace(currBranch, c.getHash());
    }

    public HashMap<String, String> getStagingArea() {
        return stagingArea;
    }

    public void clearStagingArea() {
        stagingArea = new HashMap<>();
    }

    public HashMap<String, String> getDeletionArea() {
        return deletionArea;
    }

    public void clearDeletionArea() {
        deletionArea = new HashMap<>();
    }

    public HashSet<String> getCommitSet() {
        return commitSet;
    }

    public void addToCommitSet(String hash) {
        commitSet.add(hash);
    }

    public void toFile() {
        File repositoryFile = Utils.join(GITLET_DIR, "repository");
        Utils.writeObject(repositoryFile, this);
    }

    public static Repository fromFile() {
        File repositoryFile = Utils.join(GITLET_DIR, "repository");
        return Utils.readObject(repositoryFile, Repository.class);
    }
}