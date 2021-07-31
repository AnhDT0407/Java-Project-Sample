import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        String dir = "D:\\IMG\\";
        String domain = "https://sonrau.vn/";
        String pattern = "https://sonrau.vn/gallery";

        // https://chromedriver.storage.googleapis.com/index.html
        String browserVersion = "92.0.4515.107";
        String binary = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";

        String browserVersionDev = "93.0.4577.15";
        String binaryDev = "C:\\Program Files\\Google\\Chrome Dev\\Application\\chrome.exe";

        WebDriver driver = configWebDriver(browserVersion, binary);

        ArrayList<String> imgURLs = new ArrayList<>();
        System.out.println("\nStart getting a list src IMG: [" + domain + "]");
        try {
            driver.get(domain);
            Document documentImg = Jsoup.parse(driver.getPageSource());
            imgURLs = getImgURLs(documentImg, "fs_slide", "data-src");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Get the list of src IMG done!\n");

        String folderName = "Home";
        String patch = dir + folderName;
        File theDir = new File(patch);
        if (theDir.isDirectory()) {
            System.out.println("Directory already exists!");

            //Maybe remove folder
            //FileUtils.deleteDirectory(file);

            //Create new folder copy
            System.out.println("Create new folder copy...\n");
            patch += " - " + System.currentTimeMillis();
            theDir = new File(patch);
        }

        boolean isCreated = theDir.mkdirs();
        if (isCreated) {
            System.out.println("Create patch: [" + patch + "] done!");
            System.out.println("Images will be saved in the folder: [" + folderName + "]\n");
            downloadImages(imgURLs, patch);
        } else {
            System.out.println("Can't create patch: [" + patch + "] done!");
        }
        driver.close();
        driver.quit();
    }

    private static WebDriver configWebDriver(String browserVersion, String binary) {
        // https://chromedriver.storage.googleapis.com/index.html
        // Setup ChromeDriver
        WebDriverManager.chromedriver().browserVersion(browserVersion).setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("start-maximized");
        options.addArguments("enable-automation");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-browser-side-navigation");
        options.addArguments("--disable-gpu");
        options.setBinary(binary);
        return new ChromeDriver(options);
    }

    private static ArrayList<String> getURLs(Document document, String pattern) {
        Elements elms = document.select("a[href]");
        ArrayList<String> listOfURLs = new ArrayList<>();
        for (int i = 0; i < elms.size(); i++) {
            String url = elms.get(i).absUrl("href");
            if (url.equals("")) {
                continue;
            }

            // filters increase accuracy
            if (url.contains(pattern)) {
                listOfURLs.add(url);
            }
        }
        return listOfURLs;
    }

    private static ArrayList<String> getImgURLs(Document document, String className, String attr) {
        Elements elms = document.getElementsByClass(className);
        ArrayList<String> listOfURLs = new ArrayList<>();
        for (int i = 0; i < elms.size(); i++) {
            String url = elms.get(i).absUrl(attr);
            if (url.equals("")) {
                continue;
            }
            listOfURLs.add(url);
        }
        return listOfURLs;
    }

    private static String getImgName(String url) {
        String[] str = url.toLowerCase().split("/");
        for (int i = 0; i < str.length; i++) {
            if (str[i].endsWith("jpg") || str[i].endsWith("png")) {
                return str[i].toUpperCase();
            }
        }
        return null;
    }

    // Java IO
    private static void saveImg(String srcImage, String name, String patch) {
        try {
            URL url = new URL(srcImage);
            InputStream in = url.openStream();
            OutputStream out = new BufferedOutputStream(new FileOutputStream(patch + "\\" + name));
            for (int b; (b = in.read()) != -1; ) {
                out.write(b);
            }
            out.close();
            in.close();
            System.out.println("Download image: [" + name + "] successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Apache Common IO
    private static void saveImgCommonIO(String srcImage, String name, String patch) {
        try {
            FileUtils.copyURLToFile(
                    new URL(srcImage),
                    new File(patch + "\\" + name)
            );
            System.out.println("Download image: [" + name + "] successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void downloadImages(ArrayList<String> imgURLs, String patch) {
        int count = 0;
        System.out.println("Start download image...");
        for (String src : imgURLs) {
            saveImgCommonIO(src, getImgName(src), patch);
            ++count;
        }
        System.out.println("Download " + count + " images done!\n");
    }

    private static String createFolderId(int n) {
        String str = "" + n;
        int length = 3 - str.length();

        while (length > 0) {
            str = "0" + str;
            --length;
        }
        return str;
    }
}
