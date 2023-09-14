//package gitlet;

import java.io.File;
import java.util.Scanner;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args == null || args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        Repository repo = null;
        if (Repository.GITLET_DIR.exists()) {
            repo = Repository.fromFile();
        }
        switch (args[0]) {
            case "init":
                // TODO: handle the `init` command
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    repo = new Repository();
                } else {
                    System.out.println("A Gitlet version-control system already exists in the current directory.");
                }
                //call the init command
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                if (repo == null) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                repo.add(args[1]);
                //call the add command
                break;
            // TODO: FILL THE REST IN
            case "commit":
                if (repo == null) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                repo.commit(args[1]);
                break;
                //"This command is not understood."
            case "restore":
                if (repo == null) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (args.length == 3) {
                    if (!args[1].equals("--")) {
                        System.out.println("Incorrect operands.");
                    }
                    repo.restore(args[2]);
                } else if (args.length == 4) {
                    if (!args[2].equals("--")) {
                        System.out.println("Incorrect operands.");
                    }
                    repo.restore(args[1], args[3]);
                } else {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                break;
            case "rm":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (repo == null) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                repo.remove(args[1]);
                break;
            case "log":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (repo == null) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                repo.log();
                break;
            case "global-log":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (repo == null) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                repo.globalLog();
                break;
            case "find":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (repo == null) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                repo.find(args[1]);
                break;
            case "status":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (repo == null) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                repo.status();
                break;
            case "branch":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (repo == null) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                repo.branch(args[1]);
                break;
            case "switch":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (repo == null) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                repo.switchBranch(args[1]);
                break;
            case "rm-branch":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (repo == null) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                repo.removeBranch(args[1]);
                break;
            case "reset":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (repo == null) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                repo.reset(args[1]);
                break;
            case "merge":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (repo == null) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                repo.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
        repo.toFile();
        //System.out.print("rerun program? 1(yes) or 0(no) ");
        //yes = in.nextInt();
        //in.nextLine();
        // repo.toFile();
    }
}