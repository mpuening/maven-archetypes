package ${groupId};

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlStartingWith;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.openqa.selenium.By.id;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.codeborne.selenide.WebDriverRunner;

@ExtendWith(ArquillianExtension.class)
public class HelloServletTest {

	@Deployment
	public static WebArchive createTestDeployment() {		
		return new WebAppWarBuilder("${artifactId}.war")
				.packages("${groupId}")
				.jsps()
				.tags()
				.tlds()
				.persistenceXml()
				.beansXml()
				.webXml()
				.mavenDependencies()
				.build();
	}

	@ArquillianResource
	private URL baseURL;

	@BeforeAll
	public static void setup() {
		WebDriverRunner.setWebDriver(new HtmlUnitDriver());
	}

	@Test
	public void testArquillianBootedUp() {
		assertNotNull(baseURL);
	}

	@Test
	public void testHomePage() {
		open(baseURL);
		webdriver().shouldHave(urlStartingWith(baseURL.toString()));
		// System.out.println(WebDriverRunner.source());
		$(id("header")).shouldHave(text("App Name"));
		$(id("body")).shouldHave(text("Say Hello"));
		
		$(id("hello-link")).shouldHave(text("Say Hello"));
		$(id("hello-link")).click();

		webdriver().shouldHave(urlStartingWith(baseURL + "hello"));
		$(id("header")).shouldHave(text("App Name"));
		$(id("body")).shouldHave(text("Say Hello"));
	}
}
