package edu.uclm.esi.videochat;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
public class TestRegistro {
	private WebDriver chrome;
	int numeroDeUsuarios = 100;

	ArrayList<WebDriver> drivers = new ArrayList<>();

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
		for (int i = 1; i <= this.numeroDeUsuarios; i++) {
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

	@After
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

		for (int i = 1; i <= numeroDeUsuarios; i++) {
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

			cajaNombre.sendKeys("a" + i);
			cajaEmail.sendKeys("a" + i + "@gmail.com");
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

}