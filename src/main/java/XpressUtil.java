import com.jcraft.jsch.*;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class XpressUtil {
    static Properties env = new Properties();

    static {
        try {
            env.load(new FileInputStream("credentials.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String makeXpressSSHCommunication(String command, String fileToUpload, String fileToDownload) {
        Session session = null;
        ChannelExec channel = null;

        try {
            session = new JSch().getSession(env.getProperty("xpress.user"), env.getProperty("xpress.host"), 22);
            session.setPassword(env.getProperty("xpress.passwd"));
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            Channel sftp = session.openChannel("sftp");
            // 5 seconds timeout
            sftp.connect(5000);
            ChannelSftp channelSftp = (ChannelSftp) sftp;
            // transfer file from local to remote server
            channelSftp.put(fileToUpload, fileToUpload);
            channelSftp.exit();

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            channel.setOutputStream(responseStream);
            channel.setErrStream(responseStream);
            channel.connect();
            while (channel.isConnected()) {
                Thread.sleep(100);
                System.out.println("waited");
            }
            String responseString = responseStream.toString();
            System.out.println(responseString);

            sftp = session.openChannel("sftp");
            // 5 seconds timeout
            sftp.connect(5000);
            channelSftp = (ChannelSftp) sftp;
            // download file from remote server to local
            channelSftp.get(fileToDownload, fileToDownload);
            channelSftp.exit();
            return responseString;
        } catch (JSchException | InterruptedException | SftpException e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.disconnect();
            }
            if (channel != null) {
                channel.disconnect();
            }
        }
        return "";
    }

    public static Map<String, Integer> loadVariablesFromSlx(String fileName) {
        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(Paths.get(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, Integer> variables = new HashMap<>();
        try {
            for (String line : lines) {
                String[] arr = line.trim().split(" +");
                if (arr.length == 3) {
                    variables.put(arr[1], Integer.parseInt(arr[2]));
                }
            }
        } catch (NumberFormatException e) {
            variables.clear();
        }
        return variables;
    }
}
