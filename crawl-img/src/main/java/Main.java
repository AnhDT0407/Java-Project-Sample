import io.github.bonigarcia.wdm.WebDriverManager;
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
        String domain = "https://sonrau.vn/customer-work/";
        String domain2 = "https://sonrau.vn/personal-work/";
        String pattern = "https://sonrau.vn/gallery";

        String browserVersion = "93.0.4577.15";
        String binary = "C:\\Program Files\\Google\\Chrome Dev\\Application\\chrome.exe";
        // https://chromedriver.storage.googleapis.com/index.html
        String browserVersion = "92.0.4515.107";
        String binary = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";

        String browserVersionDev = "93.0.4577.15";
        String binaryDev = "C:\\Program Files\\Google\\Chrome Dev\\Application\\chrome.exe";

        WebDriver driver = configWebDriver(browserVersion, binary);

        driver.get(domain);
        Document documentGallery = Jsoup.parse(driver.getPageSource());
        ArrayList<String> listOfURLs = getURLs(documentGallery, pattern);
        System.out.println("\nGet URLs gallery done!\n");

        for (int i = 0; i < listOfURLs.size(); i++) {
            System.out.println("INDEX    : " + i);
            System.out.println("FOLDER ID: " + "[" + createFolderId(i + 1) + "]");

            String url = listOfURLs.get(i);
            ArrayList<String> imgURLs = new ArrayList<>();
            System.out.println("Start getting a list src IMG: [" + url + "]");
            try {
                driver.get(url);
                Document documentImg = Jsoup.parse(driver.getPageSource());
                imgURLs = getImgURLs(documentImg, "fs_slide", "data-src");
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Get the list of src IMG done!\n");

            String[] temp = url.split("/");
            String folderName = "[" + createFolderId(i + 1) + "] " + temp[temp.length - 1];

            String patch = dir + folderName;
            File theDir = new File(patch);
            boolean isCreated = theDir.mkdirs();
            if (isCreated) {
                System.out.println("Create patch: [" + patch + "] done!");
                System.out.println("Images will be saved in the folder: [" + folderName + "]\n");

                downloadImages(imgURLs, patch);
            } else {
                System.out.println("Can't create patch: [" + patch + "] done!");
            }
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
            if (str[i].endsWith("jpg")) {
                return str[i].toUpperCase();
            }
        }
        return null;
    }

    private static void saveImg(String srcImage, String name, String dir) {
        try {
            URL url = new URL(srcImage);
            InputStream in = url.openStream();
            OutputStream out = new BufferedOutputStream(new FileOutputStream(dir + "\\" + name));
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

    private static void downloadImages(ArrayList<String> imgURLs, String patch) {
        int count = 0;
        System.out.println("Start download image...");
        for (String src : imgURLs) {
            saveImg(src, getImgName(src), patch);
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
