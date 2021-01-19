package edu.uclm.esi.videochat;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.timeout;

import java.io.File;
import java.io.FileOutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uclm.esi.videochat.springdao.UserRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestLogin {
    private WebDriver chrome;
    int numeroDeUsuarios = 3;

    ArrayList<WebDriver> drivers = new ArrayList<>();
    String nombres[] = { "pepe", "ana", "lucas" };

    @Autowired
    UserRepository usersRepo;

    @Before
    public void setUp() throws Exception {
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        System.setProperty("webdriver.gecko.driver", "geckodriver.exe");

        cargarCaras();
    }

    private void cargarCaras() throws Exception {
        String outputFolder = System.getProperty("java.io.tmpdir");
        if (!outputFolder.endsWith("/"))
            outputFolder += "/";

        CloseableHttpClient client = HttpClients.createDefault();
        for (int i = 0; i < this.numeroDeUsuarios; i++) {
            System.out.println("Bajando foto " + i + "/" + numeroDeUsuarios);
            HttpGet get = new HttpGet("https://thispersondoesnotexist.com/image");
            CloseableHttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            byte[] image = EntityUtils.toByteArray(entity);
            try (FileOutputStream fos = new FileOutputStream(outputFolder + "cara" + i + ".jpeg")) {
                fos.write(image);
            }
        }
        client.close();
    }

    public void eliminarUsuarioRegistrados() {
        for (int i = 0; i < nombres.length; i++) {
            usersRepo.deleteUser(nombres[i]);
        }

    }

    // @After
    public void tearDown() {
        for (int i = 0; i < drivers.size(); i++)
            drivers.get(i).close();
    }

    @Test
    @Order(1)
    public void registrar() {
        chrome = new ChromeDriver();

        chrome.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        chrome.get("https://localhost:7500/");
        chrome.manage().window().setSize(new Dimension(1161, 977));
        chrome.manage().window().setPosition(new Point(0, 0));

        eliminarUsuarioRegistrados();

        try {
            chrome.findElement(By.id("details-button")).click();
            chrome.findElement(By.id("proceed-link")).click();
        } catch (NoSuchElementException e) {
            System.out.println(e);
        }

        String inputFolder = System.getProperty("java.io.tmpdir");
        if (!inputFolder.endsWith("/"))
            inputFolder += "/";

        String picturePath;
        String script = "window.scrollTo(0,1000)";
        JavascriptExecutor je = (JavascriptExecutor) chrome;

        for (int i = 0; i < nombres.length; i++) {
            chrome.findElement(By.linkText("Crear cuenta")).click();

            WebElement cajaNombre = chrome
                    .findElement(By.xpath("//*[@id=\"globalBody\"]/oj-module/div[1]/div[2]/div/div/div/input[1]"));
            WebElement cajaEmail = chrome
                    .findElement(By.xpath("//*[@id=\"globalBody\"]/oj-module/div[1]/div[2]/div/div/div/input[2]"));
            WebElement cajaPwd1 = chrome
                    .findElement(By.xpath("//*[@id=\"globalBody\"]/oj-module/div[1]/div[2]/div/div/div/input[3]"));
            WebElement cajaPwd2 = chrome
                    .findElement(By.xpath("//*[@id=\"globalBody\"]/oj-module/div[1]/div[2]/div/div/div/input[4]"));
            RemoteWebElement inputFile = (RemoteWebElement) chrome
                    .findElement(By.xpath("//*[@id=\"globalBody\"]/oj-module/div[1]/div[2]/div/div/div/input[5]"));

            cajaNombre.sendKeys(nombres[i]);
            cajaEmail.sendKeys(nombres[i] + "@gmail.com");
            cajaPwd1.sendKeys("pepe");
            cajaPwd2.sendKeys("pepe");

            LocalFileDetector detector = new LocalFileDetector();
            picturePath = inputFolder + "cara" + i + ".jpeg";
            File file = detector.getLocalFile(picturePath);
            inputFile.setFileDetector(detector);
            inputFile.sendKeys(file.getAbsolutePath());

            je.executeScript(script);

            WebElement botonCrearCuenta = chrome.findElement(By.id("btnCrearCuenta"));
            botonCrearCuenta.click();

            new WebDriverWait(chrome, 60).ignoring(NoAlertPresentException.class)
                    .until(ExpectedConditions.alertIsPresent());

            assertThat(chrome.switchTo().alert().getText(), is("Registrado correctamente"));
            chrome.switchTo().alert().accept();
        }

        chrome.quit();
    }

    @Test
    @Order(2)
    public void login() {

        ChromeDriver driver[] = new ChromeDriver[3];

        int filas = (int) Math.sqrt(nombres.length);
        int columnas = filas;

        int ancho = 1920 / columnas, alto = 1200 / filas;
        int posX = 0, posY = 0;

        for (int i = 0; i < driver.length; i++) {
            ChromeOptions options = new ChromeOptions();

            options.addArguments("--use-fake-ui-for-media-stream");
            options.addArguments("--use-fake-device-for-media-stream");

            driver[i] = new ChromeDriver(options);
            driver[i].manage().window().setSize(new Dimension(ancho, alto));
            driver[i].manage().window().setPosition(new Point(posX, posY));
            driver[i].manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
            driver[i].get("https://localhost:7500");
            try {
                driver[i].findElement(By.id("details-button")).click();
                driver[i].findElement(By.id("proceed-link")).click();
            } catch (NoSuchElementException e) {
                System.out.println(e);
            }
            WebElement cajaNombre = driver[i].findElement(
                    By.xpath("//*[@id=\"globalBody\"]/oj-module/div[1]/div[2]/div/div/div/div[1]/div[1]/input"));
            WebElement cajaPwd = driver[i].findElement(
                    By.xpath("//*[@id=\"globalBody\"]/oj-module/div[1]/div[2]/div/div/div/div[1]/div[2]/input"));
            WebElement btnEntrar = driver[i].findElement(
                    By.xpath("//*[@id=\"globalBody\"]/oj-module/div[1]/div[2]/div/div/div/div[1]/div[3]/button"));

            cajaNombre.clear();
            cajaPwd.clear();
            cajaNombre.sendKeys(nombres[i]);
            cajaPwd.sendKeys("pepe");
            btnEntrar.click();

            drivers.add(driver[i]);
            posX = posX + ancho;
            if ((i + 1) % columnas == 0) {
                posX = 0;
                posY = posY + alto + 1;
            }
            timeout(5000);

        }
        WebElement llamarAna = driver[0].findElement(
                By.xpath("/html/body/div/oj-module/div[1]/div[2]/div/div/div[2]/div[1]/div/div[2]/div/div/button"));

        llamarAna.click();
        timeout(3000);
        // Ana rechaza
        Alert alert = driver[1].switchTo().alert();
        alert.dismiss();

        // Pepe acepta el mensaje de rechazo
        alert = driver[0].switchTo().alert();
        timeout(3000);
        alert.accept();
        // Ana llama a lucas
        WebElement llamarLucas = driver[1].findElement(
                By.xpath("/html/body/div/oj-module/div[1]/div[2]/div/div/div[2]/div[1]/div/div[3]/div/div/button"));
        llamarLucas.click();
        // Lucas acepta
        alert = driver[2].switchTo().alert();
        timeout(3000);
        alert.accept();
    }

}