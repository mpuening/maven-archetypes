package ${groupId};

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jboss.arquillian.graphene.page.Location;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@Location("hello")
public class HelloPage {

	@FindBy(id = "app-name")
	private WebElement banner;

	public void assertPageLoaded() {
		assertEquals("App Name", banner.getText());
	}

}
