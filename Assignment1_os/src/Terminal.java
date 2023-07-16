import java.io.IOException;
import java.io.File;
import java.nio.file.*;
import java.nio.file.Files;
import java.io.*;
import java.io.FileReader;
import java.util.Scanner;
import java.util.*;
import java.lang.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

 class parser
{

    public class Command
    {
        private String command;
        private int parameterCount ;
        public Command(String cmd, int c)
        {
            command = cmd;
            parameterCount = c;
        }
        public Command() {}
        public int getparameterCount ()
        {
            return parameterCount ;
        }
        public String getCommand(){
            return command;
        }
    }

    private String cmd;
    private String[] args;
    private boolean isValidParse;
    ArrayList<String> arrgs = new ArrayList<String>();
    private ArrayList<Command> commands;
    String commandName;

    public boolean isFilePath(String path){
        return Pattern.matches("[a-zA-Z]:(/[a-zA-Z][^\\s/]*)*/?", path);
    }

    public parser()
    {
        commands = new ArrayList<>();
    }

    public int getArgsCo()
    {
        return args== null ? 0 : args.length;
    }

    public void initializeCommands(String commandListPath) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(commandListPath));
        Pattern commentPattern = Pattern.compile("^(//).*"); // Regex that matches comments
        Pattern commandPattern = Pattern.compile("[a-zA-Z?]+\\s\\d"); // Regex that matches commands
        String line = br.readLine();
        while (line != null){ // will exit loop when eof is reached
            if (commentPattern.matcher(line).matches()){ } //ignore if comment
            else if (commandPattern.matcher(line).matches()){ // line is a valid command
                String[] lineElements = line.split("\\s");
                commands.add(new Command(lineElements[0], Integer.parseInt(lineElements[1])));
            }
            line = br.readLine();
        }
        br.close(); // close file
    }
    public boolean parse(String input)
    {
        cmd = null;
        args = null;
        isValidParse = false;
        Matcher matcher = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(input);

        ArrayList<String> argsList = new ArrayList<>();
        if (!matcher.find()){
            return false;
        }
        cmd = matcher.group();
        while (matcher.find()){
            if (matcher.group(1) != null){
                argsList.add(matcher.group(1));
            }
            else{
                argsList.add(matcher.group(2));
            }
        }
        args = new String[argsList.size()];
        argsList.toArray(args);

        Command c = getCommand(cmd, args.length);
        if (c != null){
            isValidParse = true;
            return true;
        }
        cmd = null;
        args = null;
        return false;

    }
    public Command getCommand(String c , int y)
    {
        for(int i =0; i< commands.size(); i++)
            if (commands.get(i).getCommand().equals(c) && y == commands.get(i).getparameterCount())
            {
                return commands.get(i);

            }
        return null;
    }

    public Command getCommand(String c)
    {
        for(int i =0; i< commands.size(); i++)
            if (commands.get(i).getCommand().equals(c))
            {
                return commands.get(i);
            }
        return null;
    }
    public ArrayList<Command>  getCommands()
    {
        return commands;
    }

    public String getCmd()
    {
        return isValidParse ? cmd : null;
    }

    public String[] getArgs()
    {
        return isValidParse ? args: null ;
    }
    public boolean isValidParse() {
        return isValidParse;
    }
}
//////////////////////////////////////////////////////
public class Terminal {
    private class PrintManager
    {
        private String buffer = "";
        private int NEW_LINE_LIMIT = 8;
        private int newLineCount = 0;
        private PrintStream outputStream = System.out;
        public void setPrintStream(PrintStream p){
            if(outputStream != System.out){
                outputStream.close();
            }
            outputStream = p;
        }
        public void print(String s){
            if(outputStream == System.out) {
                buffer += s;
                if (newLineCount < NEW_LINE_LIMIT)
                    print();
            }
            else{
                outputStream.print(s);
            }
        }
        public  void print(){
            int i;
            for(i = 0;i < buffer.length() && newLineCount < NEW_LINE_LIMIT; i++){
                if(buffer.charAt(i) == '\n')
                    newLineCount++;
                outputStream.print(buffer.charAt(i));
            }
            buffer = buffer.substring(i);
            if (newLineCount == NEW_LINE_LIMIT)
                outputStream.print("...");
        }
        public  void println(String s){
            print(s + System.getProperty("line.separator"));
        }
        public void end(){
            newLineCount = 0;
            buffer = "";
        }
        public boolean isEmpty() {
            // TODO Auto-generated method stub
            return false;
        }
    }

    private File workingDirectory; //stores absolute path of current directory
    private PrintManager printManager = new PrintManager();
    private static parser parser ;
    public Terminal()
    {
        workingDirectory = new File(System.getProperty("user.home") + "\\");
        parser = new parser();

    }

    //given a relative path changed into absolute, if directory exists
    public File makeAbsolute(String sourcePath){
        File f = new File(sourcePath);
        if(!f.isAbsolute()) {
            f = new File(workingDirectory.getAbsolutePath(), sourcePath);
        }
        return f.getAbsoluteFile();
    }
////////////////////////////////////////////////////////////
    //1-Takes 1 argument and prints it
    public void echo(String s)
    {
        System.out.print(s + " ");
        System.out.println(System.in);
    }
    //2-prints working directory
    public void pwd()
    {
        printManager.println(workingDirectory.getAbsolutePath());
    }

    //3-changes the current directory to the given one
    public void cd(String sourcePath)throws NoSuchFileException,IOException{
        if(sourcePath.equals("..")){
            String parent = workingDirectory.getParent();
            File f = new File(parent);
            workingDirectory = f.getAbsoluteFile();
        }
        else{
            File f = makeAbsolute(sourcePath);
            if(!f.exists()){
                throw new NoSuchFileException(f.getAbsolutePath(),null,"does not exist");
            }
            if(f.isFile()){
                throw new IOException("Can't cd into file");
            }
            else workingDirectory = f.getAbsoluteFile();
        }
    }
    public void cd_r()
    {
        workingDirectory = new File(System.getProperty("user.home"));
    }

    //4-lists all files and directory in given directory
    public void ls()throws NoSuchFileException
    {
        String sourcePath = null;
        File f = makeAbsolute(sourcePath);
        if(!f.exists()){
            throw new NoSuchFileException(f.getAbsolutePath(),null, "does not exist.");
        }
        String[] arr =  f.list();
        int n = arr.length;
        for(int i = 0;i < n;i++){
            printManager.println(arr[i]);
        }
    }

    //5-lists all files in current working directory
    public void lsr() {
        String sourcePath = null;
        File f = makeAbsolute(sourcePath);
        ArrayList<String> arr = null;
        if (!f.exists()) {

            arr = new ArrayList<String>(Arrays.asList(f.list()));
            Collections.sort(arr, Collections.reverseOrder());
        } else {
            //String[] arr = f.list();
            //assert arr != null;
            Collections.sort(arr, Collections.reverseOrder());

            int n = arr.size();
            for (int i = 0; i < n; i++) {
                printManager.println(arr.get(i));
            }
        }
    }

    //6-creates a new directory with the given name in a given directory
    public void mkdir(String newDir)throws NoSuchFileException,IOException{
        File f1 = makeAbsolute(newDir);
        File f2 = makeAbsolute(workingDirectory+newDir);

        if(!f1.getParentFile().exists())
            throw new NoSuchFileException(newDir,null,"does not exist.");
        else if(f1.exists())
            throw new IOException("Directory already exists.");
        else if (f1.mkdir())
        {
            throw new IOException("Created Successfully.");
        }
        else if(f2.exists())
            throw new IOException("Directory already exists.");
        else if (f2.mkdir())
        {
            throw new IOException("Created Successfully.");
        }
        boolean created = f2.mkdir();
        if(!created)
            throw new IOException("Cannot create directory.");
    }
    //7-deletes empty directory
    public void rmdir(String sourcePath)throws DirectoryNotEmptyException,NoSuchFileException,IOException {
        if (sourcePath.equals("*")) {
            File dir = new File(String.valueOf(workingDirectory));
            String[] items = dir.list();
            for (String item : items) {
                File path1 = new File(workingDirectory + "\\" + item);
                String[] paths = path1.list();
                if (paths != null && paths.length == 0) {
                    path1.delete();
                }
            }
            System.out.print("Deleted Successfully");
        } else {
            File f = makeAbsolute(sourcePath);
            File fileSameDir = new File(workingDirectory+sourcePath);
            String [] f1 = f.list();
            String [] f2 = fileSameDir.list();
            if (!fileSameDir.exists())
            {
                if(f1.length<=0)
                {
                    fileSameDir.delete();
                    System.out.println(sourcePath + "Deleted. ");
                }
                else
                {
                    throw new IOException("Cannot delete file");
                }

            }
            if (!fileSameDir.exists()) {
                if (f2.length <= 0) {
                    fileSameDir.delete();
                    System.out.println(sourcePath + "Deleted. ");

                } else {
                    throw new IOException("Cannot delete file");
                }

            }
            else if (!f.delete())
                throw new DirectoryNotEmptyException("Cannot delete non-empty directory.");
        }
    }
    //8-full path or the relative (short) path that ends with a file name and creates this file.
    public void touch(String sourcePath) throws IOException
    {
        File f1 = makeAbsolute(sourcePath);
        File f2 = makeAbsolute(workingDirectory+sourcePath);
        if(f1.isAbsolute())
        {
            if(f1.exists())
            {
                System.out.println(sourcePath + "Is already exist.");
            }
            else if(f1.createNewFile())
            {
                System.out.println(sourcePath + "Created Successfully.");
            }
        }
        else
        {
            if(f2.exists())
            {
                System.out.println(sourcePath + "Is already exist.");
            }
            else if(f2.createNewFile())
            {
                System.out.println(sourcePath + "Created Successfully.");
            }
        }


    }
    //9-to remove an exist file
    public void cp(String sourcePath, String destinationPath )throws IOException,FileNotFoundException{
        File src = makeAbsolute(sourcePath);
        if(!src.exists())
            throw new NoSuchFileException(src.getAbsolutePath(),null,"does not exist");
        File dst = makeAbsolute(destinationPath);
        if(!dst.exists()){
            if(dst.isDirectory())
                throw new NoSuchFileException(dst.getAbsolutePath(),null,"does not exist");
        }
        else
            Files.copy(src.toPath(),dst.toPath().resolve(src.toPath().getFileName()),StandardCopyOption.REPLACE_EXISTING);
    }
    //11-deletes file given specific path
    public void rm(String sourcePath) throws IOException,NoSuchFileException{
        File f1 = makeAbsolute(sourcePath);
        File f2 = makeAbsolute(workingDirectory+sourcePath);
        if(f1.exists())
        {
            if(f1.delete())
                System.out.println("Deleted.");
            else
                System.out.println("Not Deleted.");
        }
        else if(f2.exists())
        {
            if(f2.delete())
                System.out.println("Deleted.");
            else
                System.out.println("Not Deleted.");
        }
    }

    //12-Takes 1 argument and prints the file’s content or takes 2 arguments
    //and concatenates the content of the 2 files and prints it

    public void cat(String source,String dest)throws IOException
    {
        FileInputStream instream = null;
        FileOutputStream outstream = null;

        File infile = makeAbsolute(source);
        File outfile = makeAbsolute(dest);
        if(!infile.exists() || !outfile.exists())
            throw new IOException("No such file exists.");
        instream = new FileInputStream(infile);
        outstream = new FileOutputStream(outfile,true);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = instream.read(buffer)) > 0)
        {
            outstream.write(buffer, 0, length);
        }
        try (BufferedReader br = new BufferedReader(new FileReader(outfile)))
        {
            String line;
            while ((line = br.readLine()) != null) {
                printManager.println(line);
            }
        }
        instream.close();
        outstream.close();
    }

    //-concatenates files(?) and displays their content
    public void cat(String f1) throws NoSuchFileException,IOException {
        File file = makeAbsolute(f1);
        if(file.exists()) {
            BufferedReader in = new BufferedReader(new FileReader(file.getAbsolutePath()));
            String line;
            while ((line = in.readLine()) != null) {
                printManager.println(line);
            }
            in.close();
        }
        else
            throw new NoSuchFileException(file.getAbsolutePath(),null,"does not exist");
    }
    public void exit()
    {
        System.exit(0);

    }
    //*************************************************//
    public static void main (String[] args)
    {
        Terminal mainTerminal = new Terminal();
        mainTerminal.prompt();

    }
    private void prompt()
    {
        Scanner scann = new Scanner(System.in);
        while (true)
        {
            printManager.setPrintStream(System.out);
            if(printManager.isEmpty())
                printManager.print(workingDirectory.getAbsolutePath() + ">");
            try {
                chooseCommandAction(scann.nextLine());
            }
            catch(Exception e){
                printManager.println(e.getClass().getName() + ": " + e.getMessage());
            }
         }}
    public void chooseCommandAction(String input) throws Exception
    {
        if(input.trim().isEmpty()){
            printManager.end();
            return;
        }
        String[] commands = input.trim().split("\\|");
        for (int i = 0; i < commands.length; i++){
            printManager.setPrintStream(System.out);
            String command = commands[i].trim();
            if (command.charAt(0) == '>'){ //Handle reading commands from a file
                String filePath = command.substring(1).trim();
                File file = makeAbsolute(filePath);
                BufferedReader br = new BufferedReader(new FileReader(file));
                command = br.readLine();
                while (command != null){
                    chooseCommandAction(command);
                    command = br.readLine();
                }
                br.close();
            }
          
            if (!parser.parse(command)){
                throw new Exception("Error: Command not recognized.");
            }
            else{
                switch(parser.getCmd())
                {
                    case "echo":
                        rm(parser.getArgs()[0]);
                        break;
                    case "cp":
                        cp(parser.getArgs()[0], parser.getArgs()[1]);
                        break;
                    case "cd":
                        if (parser.getArgsCo() == 0)
                            cd_r();
                        else cd(parser.getArgs()[0]);
                        break;
                    case "ls":
                        if (parser.getArgsCo() == 0)
                            ls();
                        else lsr();
                        break;
                    case "pwd":
                        pwd();
                        break;
                    case "rm":
                        rm(parser.getArgs()[0]);
                        break;
                    case "mkdir":
                        mkdir(parser.getArgs()[0]);
                        break;
                    case "rmdir":
                        rmdir(parser.getArgs()[0]);
                        break;
                    case "cat":
                        if (parser.getArgsCo() == 1)
                            cat(parser.getArgs()[0]);
                        else cat(parser.getArgs()[0], parser.getArgs()[1]);
                        break;
                    case "exit":
                        exit();
                        break;
                }}}}
    private File getParentFile()
    {
        return null;
    }
  
}