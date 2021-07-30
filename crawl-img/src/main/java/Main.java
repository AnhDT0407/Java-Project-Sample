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

        // https://chromedriver.storage.googleapis.com/index.html
        // Setup ChromeDriver
        WebDriverManager.chromedriver().browserVersion("93.0.4577.15").setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("start-maximized");
        options.addArguments("enable-automation");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-browser-side-navigation");
        options.addArguments("--disable-gpu");
        options.setBinary("C:\\Program Files\\Google\\Chrome Dev\\Application\\chrome.exe");
        WebDriver driver = new ChromeDriver(options);

        driver.get(domain2);
        Document documentGallery = Jsoup.parse(driver.getPageSource());
        ArrayList<String> listSrc = listUrl(documentGallery);
        System.out.println("Get list src gallery done!\n");

        for(int i = 0; i < listSrc.size(); i++) {
            System.out.println("INDEX:  " + i);
            System.out.println("FOLDER: " + (i + 1));
            String strUrl = listSrc.get(i);
            System.out.println("Start get list URL: [" + strUrl + "]");

            ArrayList<String> listSrcImg = new ArrayList<>();
            try {
                driver.get(strUrl);
                Document documentImg = Jsoup.parse(driver.getPageSource());
                listSrcImg = listUrlImg(documentImg);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Get list src   IMG: [" + strUrl + "] done!");

            String temp[] = strUrl.split("/");
            String folder =  dir + "[" + folderId(i + 1) + "] " + temp[temp.length - 1];
            File theDir = new File(folder);
            theDir.mkdirs();
            System.out.println("Create folder [" + folder + "] done!");

            int count = 0;
            for (String src : listSrcImg) {
                saveImg(src, getImgName(src), folder);
                ++count;
            }
            System.out.println("Download " + count + " images done!\n");
        }
        driver.close();
        driver.quit();
    }

    private static String getImgName(String url) {
        String str[] = url.toLowerCase().split("/");
        for (int i = 0; i < str.length; i++) {
            if(str[i].endsWith("jpg")) {
                return str[i].toUpperCase();
            }
        }
        return null;
    }

    private static ArrayList<String> listUrl(Document document) {
        Elements elms = document.select("a[href]");
        ArrayList<String> listUrl = new ArrayList<>();
        for (int i = 0; i < elms.size(); i++) {
            String url = elms.get(i).absUrl("href");
            if (url.equals("")) {
                continue;
            }
            if (url.contains("https://sonrau.vn/gallery")) {
                listUrl.add(url);
            }
        }
        return listUrl;
    }

    private static ArrayList<String> listUrlImg(Document document) throws IOException {
        Elements elms = document.getElementsByClass("fs_slide");
        ArrayList<String> listUrl = new ArrayList<>();
        for (int i = 0; i < elms.size(); i++) {
            String url = elms.get(i).absUrl("data-src");
            if (url.equals("")) {
                continue;
            }
            listUrl.add(url);
        }
        return listUrl;
    }

    private static void saveImg(String srcImage, String name, String dir) {
        try {
            URL url = new URL(srcImage);
            InputStream in = url.openStream();
            OutputStream out = new BufferedOutputStream(new FileOutputStream(dir + "\\" + name));
            for (int b; (b = in.read()) != -1;) {
                out.write(b);
            }
            out.close();
            in.close();
            System.out.println("Download image: [" + name + "] successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String folderId(int n) {
        String strId = "" + n;
        int length = 3 - strId.length();

        while (length > 0) {
            strId = "0" + strId;
            --length;
        }
        return strId;
    }
}
