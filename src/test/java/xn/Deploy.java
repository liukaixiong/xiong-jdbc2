package xn;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * 上传依赖到 Maven 私服
 *
 * @author liuzenghui
 * @since 2017/7/31.
 */
public class Deploy {

    private final static String repositoryURL = "http://192.168.0.11:8081/nexus/content/repositories/elab/";
    private final static String repositoryId = "releases";


    /**
     * mvn -s F:\.m2\settings.xml
     * org.apache.maven.plugins:maven-deploy-plugin:2.8.2:deploy-file
     * -Durl=http://IP:PORT/nexus/content/repositories/thirdpart
     * -DrepositoryId=thirdpart
     * -Dfile=antlr-2.7.2.jar
     * -DpomFile=antlr-2.7.2.pom
     * -Dpackaging=jar
     * -DgeneratePom=false
     * -Dsources=./path/to/artifact-name-1.0-sources.jar
     * -Djavadoc=./path/to/artifact-name-1.0-javadoc.jar
     */
    public static final String BASE_CMD = "cmd /c mvn " +
            "deploy:deploy-file " +
            "-Durl=" + repositoryURL +
            " -DrepositoryId=" + repositoryId +
            " -DgeneratePom=false";

    public static final Pattern DATE_PATTERN = Pattern.compile("-[\\d]{8}\\.[\\d]{6}-");

    public static final Runtime CMD = Runtime.getRuntime();

    public static final Writer ERROR;

    public static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(10);

    static {
        Writer err = null;
        try {
            err = new OutputStreamWriter(new FileOutputStream("deploy-error.log"), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        ERROR = err;
    }

    public static void main(String[] args) {
        deploy(new File("E:\\me\\webres").listFiles());
//        if(checkArgs(args)){
//            File file = new File(args[0]);
//            deploy(file.listFiles());
//        }
        EXECUTOR_SERVICE.shutdown();
        try {
            ERROR.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void error(String error) {
        try {
            System.err.println(error);
            ERROR.write(error + "\n");
            ERROR.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkArgs(String[] args) {
        if (args.length != 1) {
            System.out.println("用法如： java -jar Deploy D:\\some\\path\\");
            return false;
        }
        File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println(args[0] + " 目录不存在!");
            return false;
        }
        if (!file.isDirectory()) {
            System.out.println("必须指定为目录!");
            return false;
        }
        return true;
    }

    public static void deploy(File[] files) {
        if (files.length == 0) {
            //ignore
        } else if (files[0].isDirectory()) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deploy(file.listFiles());
                }
            }
        } else if (files[0].isFile()) {
            File pom = null;
            File jar = null;
            File source = null;
            File javadoc = null;
            //忽略日期快照版本，如 xxx-mySql-2.2.6-20170714.095105-1.jar
            for (File file : files) {
                String name = file.getName();
                if (DATE_PATTERN.matcher(name).find()) {
                    //skip
                } else if (name.endsWith(".pom")) {
                    pom = file;
                } else if (name.endsWith("-javadoc.jar")) {
                    javadoc = file;
                } else if (name.endsWith("-sources.jar")) {
                    source = file;
                } else if (name.endsWith(".jar")) {
                    jar = file;
                }
            }
            if (pom != null) {
                if (jar != null) {
                    deploy(pom, jar, source, javadoc);
                } else if (packingIsPom(pom)) {
                    deployPom(pom);
                }
            }
        }
    }

    public static boolean packingIsPom(File pom) {
        BufferedReader reader = null;
        try {
            reader =
                    new BufferedReader(new InputStreamReader(new FileInputStream(pom)));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().indexOf("<packaging>pom</packaging>") != -1) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
            }
        }
        return false;
    }

    public static void deployPom(final File pom) {
        EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                StringBuffer cmd = new StringBuffer(BASE_CMD);
                cmd.append(" -DpomFile=").append(pom.getName());
                cmd.append(" -Dfile=").append(pom.getName());
                try {
                    final Process proc = CMD.exec(cmd.toString(), null, pom.getParentFile());
                    InputStream inputStream = proc.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    String line;
                    StringBuffer logBuffer = new StringBuffer();
                    logBuffer.append("\n\n\n==================================\n");
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("[INFO]") || line.startsWith("Upload")) {
                            logBuffer.append(Thread.currentThread().getName() + " : " + line + "\n");
                        }
                    }
                    System.out.println(logBuffer);
                    int result = proc.waitFor();
                    if (result != 0) {
                        error("上传失败：" + pom.getAbsolutePath());
                    }
                } catch (IOException e) {
                    error("上传失败：" + pom.getAbsolutePath());
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    error("上传失败：" + pom.getAbsolutePath());
                    e.printStackTrace();
                }
            }
        });
    }

    public static void deploy(final File pom, final File jar, final File source, final File javadoc) {
        EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                StringBuffer cmd = new StringBuffer(BASE_CMD);
                cmd.append(" -DpomFile=").append(pom.toString());
                if (jar != null) {
                    //当有bundle类型时，下面的配置可以保证上传的jar包后缀为.jar
                    cmd.append(" -Dpackaging=jar -Dfile=").append(jar.toString());
                } else {
                    cmd.append(" -Dfile=").append(pom.getName());
                }
                if (source != null) {
                    cmd.append(" -Dsources=").append(source.getName());
                }
                if (javadoc != null) {
                    cmd.append(" -Djavadoc=").append(javadoc.getName());
                }


                System.out.println(cmd.toString());
                try {
                    final Process proc = CMD.exec(cmd.toString(), null, pom.getParentFile());
                    InputStream inputStream = proc.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    String line;
                    StringBuffer logBuffer = new StringBuffer();
                    logBuffer.append("\n\n\n=======================================\n");
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("[INFO]") || line.startsWith("Upload")) {
                            logBuffer.append(Thread.currentThread().getName() + " : " + line + "\n");
                        }
                    }
                    System.out.println(logBuffer);
                    int result = proc.waitFor();
                    if (result != 0) {
                        error("上传失败：" + pom.getAbsolutePath());
                    }
                } catch (IOException e) {
                    error("上传失败：" + pom.getAbsolutePath());
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    error("上传失败：" + pom.getAbsolutePath());
                    e.printStackTrace();
                }
            }
        });
    }
}